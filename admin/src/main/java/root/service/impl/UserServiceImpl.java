package root.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import root.common.convention.BaseException;
import root.dao.entity.UserDO;
import root.dao.mapper.UserMapper;
import root.dto.req.UserLoginReqDTO;
import root.dto.req.UserRegisterDTO;
import root.dto.req.UserUpdateDTO;
import root.dto.resp.UserLoginRespDTO;
import root.dto.resp.UserRespDTO;
import root.service.UserService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Override
    public UserRespDTO getUserByUsername(String username) {
        UserDO userDO=lambdaQuery()
                .eq(UserDO::getUsername, username)
                .one();
        if (userDO==null){
            throw new BaseException("用户不存在");
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO,result);
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        if(hasUsername(userRegisterDTO.getUsername())){
            throw new BaseException("用户已存在");
        }
        //使用这种分布式锁的机制来防止短时间大量相同用户名的注册
        RLock lock = redissonClient.getLock("short-link:lock_user-register:" + userRegisterDTO.getUsername());
        try {
            if (lock.tryLock()) {
                UserDO userDO = new UserDO();
                BeanUtils.copyProperties(userRegisterDTO,userDO);
                boolean saveResult = save(userDO);
                if(!saveResult){
                    throw new BaseException("用户注册失败");
                }
                userRegisterCachePenetrationBloomFilter.add(userRegisterDTO.getUsername());
                return;
            }
            throw new BaseException("用户已经存在");
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void update(UserUpdateDTO userUpdateDTO) {
        //TODO 验证当前用户名是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper= Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, userUpdateDTO.getUsername());
        baseMapper.update(BeanUtil.toBean(userUpdateDTO,UserDO.class),updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, userLoginReqDTO.getUsername())
                .eq(UserDO::getPassword, userLoginReqDTO.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if(userDO==null){
            throw new BaseException("用户不存在");
        }
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey("login_" + userLoginReqDTO.getUsername()))){
            throw new BaseException("用户已经登录");
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_"+userLoginReqDTO.getUsername(),"token", JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_"+userLoginReqDTO.getUsername(),30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username,String token) {
        return stringRedisTemplate.opsForHash().get("login_"+username,"token")!=token;
    }
}

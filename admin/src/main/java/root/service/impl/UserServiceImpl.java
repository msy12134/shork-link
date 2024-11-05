package root.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.common.convention.BaseException;
import root.dao.entity.UserDO;
import root.dao.mapper.UserMapper;
import root.dto.req.UserRegisterDTO;
import root.dto.resp.UserRespDTO;
import root.service.UserService;
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    @Autowired
    private RedissonClient redissonClient;
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
}

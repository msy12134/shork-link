package root.service;
import com.baomidou.mybatisplus.extension.service.IService;
import root.dao.entity.UserDO;
import root.dto.req.UserRegisterDTO;
import root.dto.req.UserUpdateDTO;
import root.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {
    /**
     *
     * @param username
     * @return
     */
    UserRespDTO getUserByUsername(String username);

    Boolean hasUsername(String username);

    void register(UserRegisterDTO userRegisterDTO);

    void update(UserUpdateDTO userUpdateDTO);
}

package root.dto.req;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;

    // 密码
    private String password;

    // 真实姓名
    private String realName;

    // 手机号
    private String phone;

    // 邮箱
    private String mail;
}

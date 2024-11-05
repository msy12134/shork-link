package root.dto.resp;

import lombok.Data;
import root.util.DealSensitiveDataAOP.Mask;
import root.util.DealSensitiveDataAOP.MaskType;

@Data
public class UserRespDTO {
    private Long id;

    // 用户名
    private String username;

    // 真实姓名
    private String realName;

    // 手机号
    @Mask(type = MaskType.PHONE)
    private String phone;

    // 邮箱
    @Mask(type = MaskType.EMAIL)
    private String mail;


}

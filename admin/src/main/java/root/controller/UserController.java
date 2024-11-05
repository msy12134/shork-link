package root.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import root.common.convention.Result;
import root.dto.req.UserRegisterDTO;
import root.dto.resp.UserRespDTO;
import root.service.UserService;
import root.util.DealSensitiveDataAOP.SensitiveData;

@Slf4j
@RestController
@RequestMapping("/api/short-link/v1")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @SensitiveData
    @GetMapping("/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        log.info("getUserByUsername: " + username);
        return Result.success(userService.getUserByUsername(username));
    }

    /**
     * 查询用户无脱敏信息
     * @param username
     * @return
     */
    @GetMapping("/actual/user/{username}")
    public Result<UserRespDTO> getActualUserByUsername(@PathVariable String username) {
        log.info("getUserByUsername: " + username);
        return Result.success(userService.getUserByUsername(username));
    }


    /**
     * 查询用户是否存在
     * @param username
     * @return
     */
    @GetMapping("/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        log.info("hasUsername: " + username);
        return Result.success(userService.hasUsername(username));
    }

    @PostMapping("/user")
    public Result<Void> register(@RequestBody UserRegisterDTO userRegisterDTO){
        log.info("register: " + userRegisterDTO);
        userService.register(userRegisterDTO);
        return Result.success();
    }


}

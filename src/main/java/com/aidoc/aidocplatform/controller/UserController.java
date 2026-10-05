package com.aidoc.aidocplatform.controller;

import com.aidoc.aidocplatform.common.Result;
import com.aidoc.aidocplatform.common.utils.UserContext;
import com.aidoc.aidocplatform.entity.User;
import com.aidoc.aidocplatform.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心控制器：提供当前登录用户信息查询。
 */
@Tag(name = "用户中心", description = "当前登录用户信息")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 获取当前登录用户信息（优先走 Redis 缓存，并抹除密码字段）。
     *
     * @return 当前用户实体（不含密码）
     */
    @Operation(summary = "获取当前登录用户信息（走 Redis 缓存）")
    @GetMapping("/me")
    public Result<User> me() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserWithCache(userId);
        if (user != null) {
            user.setPassword(null);   // 敏感字段不外泄
        }
        return Result.success(user);
    }
}

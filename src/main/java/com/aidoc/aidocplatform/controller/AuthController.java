package com.aidoc.aidocplatform.controller;

import com.aidoc.aidocplatform.common.Result;
import com.aidoc.aidocplatform.dto.LoginDTO;
import com.aidoc.aidocplatform.dto.RegisterDTO;
import com.aidoc.aidocplatform.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器：提供用户注册与登录接口。
 * <p>此两个接口无需 Token（已在 {@code WebMvcConfig} 中放行 {@code /api/auth/**}）。
 */
@Tag(name = "认证管理", description = "注册/登录接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * 用户注册。
     *
     * @param dto 注册信息（用户名、密码、邮箱），经 @Valid 校验
     * @return 统一结果，成功时 code=200
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    /**
     * 用户登录，成功后返回 JWT Token。
     *
     * @param dto 登录信息（用户名、密码）
     * @return data 为签发后的 JWT Token
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginDTO dto) {
        String token = authService.login(dto);
        return Result.success(token);
    }
}
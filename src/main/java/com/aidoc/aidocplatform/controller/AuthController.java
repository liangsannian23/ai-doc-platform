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

@Tag(name = "认证管理", description = "注册/登录接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody @Valid RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginDTO dto) {
        String token = authService.login(dto);
        return Result.success(token);
    }
}
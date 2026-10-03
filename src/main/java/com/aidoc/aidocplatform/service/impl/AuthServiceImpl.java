package com.aidoc.aidocplatform.service.impl;

import com.aidoc.aidocplatform.common.utils.JwtUtil;
import com.aidoc.aidocplatform.dto.LoginDTO;
import com.aidoc.aidocplatform.dto.RegisterDTO;
import com.aidoc.aidocplatform.entity.User;
import com.aidoc.aidocplatform.service.IAuthService;
import com.aidoc.aidocplatform.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void register(RegisterDTO dto) {
        // 1. 唯一性校验
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userService.exists(wrapper)) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 密码加密 + 入库
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // BCrypt加密
        user.setEmail(dto.getEmail());
        userService.save(user);
    }

    @Override
    public String login(LoginDTO dto) {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userService.getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 签发JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 4. 预热缓存：用户登录成功后，主动将用户信息写入Redis缓存
        userService.getUserWithCache(user.getId());

        return token;
    }
}
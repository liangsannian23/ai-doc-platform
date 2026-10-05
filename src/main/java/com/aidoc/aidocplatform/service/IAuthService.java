package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.dto.LoginDTO;
import com.aidoc.aidocplatform.dto.RegisterDTO;

/**
 * 认证服务接口：负责用户注册与登录。
 */
public interface IAuthService {

    /**
     * 注册新用户，校验用户名唯一并对密码做 BCrypt 加密。
     *
     * @param dto 注册信息（用户名、密码、邮箱）
     * @throws RuntimeException 用户名已存在时抛出
     */
    void register(RegisterDTO dto);

    /**
     * 用户登录，校验密码后签发 JWT Token。
     *
     * @param dto 登录信息（用户名、密码）
     * @return 签名后的 JWT Token
     * @throws RuntimeException 用户名或密码错误时抛出
     */
    String login(LoginDTO dto);
}
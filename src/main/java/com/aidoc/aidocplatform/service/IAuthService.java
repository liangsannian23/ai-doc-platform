package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.dto.LoginDTO;
import com.aidoc.aidocplatform.dto.RegisterDTO;

public interface IAuthService {
    void register(RegisterDTO dto);
    String login(LoginDTO dto);
}
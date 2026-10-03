package com.aidoc.aidocplatform.service.impl;

import com.aidoc.aidocplatform.entity.User;
import com.aidoc.aidocplatform.mapper.UserMapper;
import com.aidoc.aidocplatform.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final com.aidoc.aidocplatform.util.RedisCacheUtil cacheUtil;

    private static final long USER_TTL = 30 * 60L; // 30分钟

    @Override
    public User getUserWithCache(Long userId) {
        return cacheUtil.get("user:info:" + userId, User.class,
                () -> this.getById(userId), USER_TTL);
    }

    @Override
    public void evictUserCache(Long userId) {
        cacheUtil.evict("user:info:" + userId);
    }
}
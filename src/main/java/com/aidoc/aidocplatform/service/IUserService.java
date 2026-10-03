package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserService extends IService<User> {

    // ===== 新增：带缓存的用户读取 =====
    /** 读取用户，优先走 Redis 缓存，未命中回源 MySQL 并回写缓存 */
    User getUserWithCache(Long userId);

    /** 主动失效用户缓存（更新/删除用户后调用） */
    void evictUserCache(Long userId);
}
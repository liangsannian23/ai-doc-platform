package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口：在基础 CRUD 之上提供带 Redis 缓存的用户读取与缓存失效。
 * <p>继承 MyBatis-Plus {@link IService} 以获得基础 CRUD 能力。
 */
public interface IUserService extends IService<User> {

    // ===== 新增：带缓存的用户读取 =====
    /** 读取用户，优先走 Redis 缓存，未命中回源 MySQL 并回写缓存 */
    User getUserWithCache(Long userId);

    /** 主动失效用户缓存（更新/删除用户后调用） */
    void evictUserCache(Long userId);
}
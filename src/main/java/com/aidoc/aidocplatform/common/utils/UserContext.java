package com.aidoc.aidocplatform.common.utils;

/**
 * 用户上下文 - 通过ThreadLocal传递当前登录用户ID
 * 拦截器写入，Controller/Service读取
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private UserContext() {}

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new RuntimeException("未获取到当前用户信息，请重新登录");
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
package com.example.springboot.security;

import com.example.springboot.common.BizException;

public class AuthContext {
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_STAFF = "staff";

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public record CurrentUser(Long id, String role, String name) {}

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static CurrentUser require() {
        CurrentUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(401, "未登录");
        }
        return u;
    }

    public static CurrentUser requireStaff() {
        CurrentUser u = require();
        if (!ROLE_STAFF.equals(u.role())) {
            throw new BizException(403, "需要销售员权限");
        }
        return u;
    }
}

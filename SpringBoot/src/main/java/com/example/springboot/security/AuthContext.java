package com.example.springboot.security;

import com.example.springboot.common.BizException;

/**
 * 当前登录用户上下文（请求级）。
 * {@link JwtAuthFilter} 解析 token 后将用户信息写入 {@link ThreadLocal}，
 * 控制器再通过 {@link #require()} / {@link #requireStaff()} 取用并做权限校验。
 * <p>⚠ 由于线程池会复用线程，过滤器必须在请求结束后调用 {@link #clear()}，
 * 否则会发生用户身份串号。
 */
public class AuthContext {
    /** 角色常量：客户 */
    public static final String ROLE_CUSTOMER = "customer";
    /** 角色常量：销售员 */
    public static final String ROLE_STAFF = "staff";

    /** 线程隔离地保存当前请求的登录用户 */
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    /**
     * 当前用户快照。
     * @param id   用户主键（customer_id 或 staff_id）
     * @param role customer 或 staff
     * @param name 显示名
     */
    public record CurrentUser(Long id, String role, String name) {}

    /** 写入当前线程的登录用户（过滤器解析 token 后调用） */
    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    /** 读取当前用户，未登录返回 null */
    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 清除当前线程上下文，必须在过滤器 finally 中调用以防线程复用串号 */
    public static void clear() {
        HOLDER.remove();
    }

    /** 要求已登录，否则抛 401 */
    public static CurrentUser require() {
        CurrentUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(401, "未登录");
        }
        return u;
    }

    /** 要求销售员身份，否则抛 403（用于商品/客户管理、发货、代客操作等） */
    public static CurrentUser requireStaff() {
        CurrentUser u = require();
        if (!ROLE_STAFF.equals(u.role())) {
            throw new BizException(403, "需要销售员权限");
        }
        return u;
    }
}

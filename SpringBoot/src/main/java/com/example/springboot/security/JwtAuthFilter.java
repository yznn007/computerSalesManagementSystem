package com.example.springboot.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器。
 * 每个请求执行一次：从 {@code Authorization: Bearer <token>} 解析出用户身份并写入
 * {@link AuthContext}。本过滤器只负责"解析并存放"，不负责拦截——具体接口是否要求
 * 登录/角色由控制器内的 {@code require()/requireStaff()} 决定。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // 去掉 "Bearer " 前缀
            try {
                Claims claims = jwtUtil.parse(token);
                Long id = Long.valueOf(claims.getSubject());
                String role = claims.get("role", String.class);
                String name = claims.get("name", String.class);
                AuthContext.set(new AuthContext.CurrentUser(id, role, name));
            } catch (Exception ignored) {
                // token 无效则保持匿名，由后续 require() 拦截
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            AuthContext.clear(); // ⚠ 必须清理，防止线程池复用导致身份串号
        }
    }
}

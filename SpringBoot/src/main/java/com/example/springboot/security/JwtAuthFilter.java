package com.example.springboot.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
            String token = header.substring(7);
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
            AuthContext.clear();
        }
    }
}

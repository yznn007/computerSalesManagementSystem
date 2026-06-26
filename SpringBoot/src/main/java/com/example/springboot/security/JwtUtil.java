package com.example.springboot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析工具。
 * 使用 HMAC-SHA 对称密钥签名，payload 携带用户 id（subject）、role、name，
 * 供 {@link JwtAuthFilter} 解析后还原登录态。
 */
@Component
public class JwtUtil {

    /** 签名密钥（Base64 编码），来自配置 app.jwt.secret */
    @Value("${app.jwt.secret}")
    private String secret;

    /** token 有效期（小时），默认 24 */
    @Value("${app.jwt.expire-hours:24}")
    private long expireHours;

    /** 由 secret 派生出的 HMAC 密钥，init() 阶段构建一次 */
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] bytes = java.util.Base64.getDecoder().decode(secret);
        // HMAC-SHA256 要求密钥至少 256 位（32 字节），不足则补零填充以满足强度要求
        if (bytes.length < 32) {
            bytes = (new String(bytes) + "00000000000000000000000000000000").getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /** 签发 token：subject=用户id，附带 role/name 声明与过期时间 */
    public String generate(Long id, String role, String name) {
        long now = System.currentTimeMillis();
        long exp = now + expireHours * 3600_000L; // 过期时间 = 当前 + expireHours 小时
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("role", role)
                .claim("name", name)
                .issuedAt(new Date(now))
                .expiration(new Date(exp))
                .signWith(key)
                .compact();
    }

    /** 验签并解析 token，返回 Claims；签名错误或过期会抛异常 */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

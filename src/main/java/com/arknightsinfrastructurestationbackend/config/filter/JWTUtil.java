package com.arknightsinfrastructurestationbackend.config.filter;

import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.service.user.SelectUserService;
import io.jsonwebtoken.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.function.Function;


@Component
public class JWTUtil {
    private final String secretKey = System.getenv("ARKNIGHTS_JWT_SECRET_KEY"); // 从环境变量获取密钥，需要管理员权限
    @Resource
    private final SelectUserService selectUserService; // 用于检查Token是否唯一

    public JWTUtil(SelectUserService selectUserService) {
        this.selectUserService = selectUserService;
    }


    /**
     * 生成Token，从头到尾，uid都不会被返回给前端，generateToken方法由后端调用
     * @param uid 用户id
     * @return token
     */
    public String generateToken(Long uid) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + 2592000000L; // Token有效期，一个月
        Date exp = new Date(expMillis);

        // 生成16位长的随机字符串
        String randomString = generateRandomString(16);

        // 组合uid、时间戳和随机字符串
        String subject = uid.toString() + "-" + nowMillis + "-" + randomString;

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        while (length-- > 0) {
            int index = (int) (Math.random() * characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

    /**
     * 确保生成唯一的Token
     * @return 唯一Token
     */
    public String generateUniqueToken(Long uid) {
        String token;
        int maxAttempts = 50; // 限制重试次数以避免潜在的无限循环
        int attempts = 0;

        do {
            token = generateToken(uid);
            attempts++;
        } while (selectUserService.getUserByToken(token) != null && attempts < maxAttempts);

        if (attempts >= maxAttempts) {
            throw new IllegalStateException("无法生成唯一的Token");
        }

        return token;
    }

    public Long extractUid(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        String uidPart = subject.split("-")[0];
        return Long.parseLong(uidPart);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws SignatureException, ExpiredJwtException {
        // 解析Token，如果有任何问题会抛出异常
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean validateToken(String token, User user) {
        final Long uid = extractUid(token);
        return (uid.equals(user.getId()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean isTokenExpiringWithin(String token, Duration duration) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        long diff = expiration.getTime() - System.currentTimeMillis();
        return diff < duration.toMillis();
    }
}

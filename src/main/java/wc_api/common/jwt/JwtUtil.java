package wc_api.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRED_MS = 1000L * 60 * 60; // 1 시간
    private static final long REFRESH_TOKEN_EXPIRED_MS = 1000L * 60 * 60 * 24 * 7; // 7 일

//    private static final long ACCESS_TOKEN_EXPIRED_MS = 1000L * 20; // 20초
//    private static final long REFRESH_TOKEN_EXPIRED_MS = 1000L * 40; // 40초

    // accessToken 생성
    public String createAccessToken(String loginEmail) {
        return createJwt(loginEmail, ACCESS_TOKEN_EXPIRED_MS);
    }

    // refreshToken 생성
    public String createRefreshToken(String loginEmail) {
        return createJwt(loginEmail, REFRESH_TOKEN_EXPIRED_MS);
    }

    // 토큰 생성 로직
    public String createJwt(String loginEmail, Long expiredMs) {
        Claims claims = Jwts.claims().setSubject(loginEmail);
        claims.put("loginEmail", loginEmail);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 토큰 유효성 검사
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    // 토큰에서 loginEmail 추출
    public String extractLoginEmail(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return claims.get("loginEmail", String.class);
    }
}

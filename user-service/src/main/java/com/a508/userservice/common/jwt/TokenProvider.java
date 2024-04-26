package com.a508.userservice.common.jwt;

import com.a508.userservice.common.exception.CustomException;
import com.a508.userservice.common.exception.ErrorCode;
import com.a508.userservice.login.data.OauthTokenRes;
import com.a508.userservice.user.domain.User;
import com.a508.userservice.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.sql.Timestamp;
import java.util.Date;

import static java.time.LocalDateTime.now;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "bearer";
    private static final int ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final int REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일
    private static final String AUD = "https://k10a508.p.ssafy.io/";
    private static final String ISS = "https://k10a508.p.ssafy.io/";
    private final Key key;

    @Autowired
    private UserRepository userRepository;


    public TokenProvider(@Value("${spring.security.oauth2.jwt.secret}") String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public OauthTokenRes generateTokenDto(User user) {

        long now = (new Date()).getTime();
        //ACCESSTOKEN 생성
        String accessToken = Jwts.builder()
                .claim(AUTHORITIES_KEY, user.getRoles())
                .signWith(key, SignatureAlgorithm.HS512)
                .setAudience(AUD) //식별가능해야한다.
                .setSubject(String.valueOf(user.getGoogleId()))
                .setIssuer(ISS)
                .setIssuedAt(Timestamp.valueOf(now()))
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME))
                .compact();

        //REFRESHTOKEN 생성
        String refreshToken = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS512)
                .setAudience(AUD)
                .setSubject(String.valueOf(user.getGoogleId()))
                .setIssuer(ISS)
                .setIssuedAt(Timestamp.valueOf(now()))
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .compact();


        return OauthTokenRes.builder()
                .memberId(user.getId())
                .tokenType(BEARER_TYPE)
                .accessToken(accessToken)
                .expiresIn(ACCESS_TOKEN_EXPIRE_TIME - 1)
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn(REFRESH_TOKEN_EXPIRE_TIME)
                .build();
    }

    //복호화
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    //검증된 refreshToken 복호화해서 User 정보 가져오기
    public String getUserByRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);

        String googleId = claims.getSubject();

        User user = userRepository.findByGoogleId(googleId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND_ERROR);
        }
        return googleId;
    }

}

package kr.co.antoon.oauth.application;

import kr.co.antoon.error.dto.ErrorMessage;
import kr.co.antoon.error.exception.common.NotExistsException;
import kr.co.antoon.error.exception.oauth.TokenExpiredException;
import kr.co.antoon.oauth.dto.TokenResponse;
import kr.co.antoon.user.domain.User;
import kr.co.antoon.user.domain.vo.Role;
import kr.co.antoon.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;

    public TokenResponse refresh(String refreshToken) {
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotExistsException(ErrorMessage.NOT_EXIST_USER));

        String redisRT = (String) redisTemplate.opsForValue().get("RT: " + userId); //redis refreshToken
        // (추가) 로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
        if (ObjectUtils.isEmpty(refreshToken)) {
            throw new TokenExpiredException(ErrorMessage.EXPIRED_TOKEN);
        }
        //redisRT와 받은 refreshToken이 동일한지 유효성 검사 -> 헤더에서 rt 가져와
        if (!jwtTokenProvider.validate(redisRT)) { //만료 검사
            throw new TokenExpiredException(ErrorMessage.EXPIRED_TOKEN);
        } else if (!redisRT.equals(refreshToken)) { //유효성 검사(사용자 일치 여부 판단)
            throw new TokenExpiredException(ErrorMessage.NOT_VALIDATE_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()), Role.USER.getKey());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()));

        //redis refreshToken 갱신
        redisTemplate.opsForValue().set("RT: " + user.getId(), newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpireTime(), TimeUnit.MILLISECONDS);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void revokeToken(String access, String refresh) {
        if (!jwtTokenProvider.validate(access)) { //유효성 검사
            throw new TokenExpiredException(ErrorMessage.NOT_VALIDATE_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(access);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotExistsException(ErrorMessage.NOT_EXIST_USER));

        if (redisTemplate.opsForValue().get("RT: " + userId) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT: " + userId);
        }
        Long expiration = jwtTokenProvider.getExpiration(access);
        redisTemplate.opsForValue()
                .set(access, "logout", expiration, TimeUnit.MILLISECONDS);

    }
}

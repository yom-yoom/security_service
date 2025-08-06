package school.faang.springsecuritydemo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.springsecuritydemo.domain.RefreshToken;
import school.faang.springsecuritydemo.repository.RefreshTokenRedisRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public void save(RefreshToken token) {
        refreshTokenRedisRepository.saveToken(token);
    }

    public void deleteByToken(String token) {
        refreshTokenRedisRepository.deleteByToken(token);
    }

    public boolean existsByToken(String token) {
        return refreshTokenRedisRepository.existsByToken(token);
    }
}

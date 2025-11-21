package com.foodcom.firstpro.auth.repository;

import com.foodcom.firstpro.auth.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenValue(String tokenValue);
}

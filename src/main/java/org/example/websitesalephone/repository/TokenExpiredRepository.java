package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.ExpiredToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenExpiredRepository extends JpaRepository<ExpiredToken, Long> {

    /**
     * Find by access token
     *
     * @param accessToken access token
     * @return true if present, otherwise false
     */
    Boolean findByAccessToken(String accessToken);

    /**
     * Find all record by token access expired.
     * @param token The access token
     * @return List of access token was expired
     */
    List<ExpiredToken> findAllByAccessToken(String token);

}
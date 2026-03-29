package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findAllByUser(User user);
}


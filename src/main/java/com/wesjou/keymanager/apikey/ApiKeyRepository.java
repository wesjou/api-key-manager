package com.wesjou.keymanager.apikey;

import com.wesjou.keymanager.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByPublicId(String publicId);
    List<ApiKey> findAllByUser(User user);
}


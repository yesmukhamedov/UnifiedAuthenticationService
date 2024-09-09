package com.project.unifiedauthenticationservice.repositories;

import com.project.unifiedauthenticationservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

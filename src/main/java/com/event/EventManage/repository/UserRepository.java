package com.event.EventManage.repository;

import com.event.EventManage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.event.EventManage.model.Role;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    long countByRole(Role role);
}

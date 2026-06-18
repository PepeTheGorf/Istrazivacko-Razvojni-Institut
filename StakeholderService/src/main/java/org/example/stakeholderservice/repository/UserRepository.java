package org.example.stakeholderservice.repository;

import org.example.stakeholderservice.dto.UserDTO;
import org.example.stakeholderservice.model.Role;
import org.example.stakeholderservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> getAllByRole(Role role);
}

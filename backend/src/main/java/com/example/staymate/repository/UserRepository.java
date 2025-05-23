
package com.example.staymate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.staymate.entity.enums.UserRole;
import com.example.staymate.entity.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find user by email
    Optional<User> findByEmail(String email);

    // Find users by role
    List<User> findByRole(UserRole role);

    List<User> findByLastNameContaining(String lastName);

    User findByVerificationToken(String verificationToken);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAllActiveUsers();
}

package com.shiptrack.repository;
// Handles database operations for users.
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shiptrack.model.Role;
import com.shiptrack.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods for finding users based on various criteria
    Optional<User> findByUsername(String username);
    Optional<User> findByIdNumber(String idNumber);

    // Check if a user with the given username or ID number already exists
    boolean existsByUsername(String username);
    boolean existsByIdNumber(String idNumber);

    long countByRole(com.shiptrack.model.Role role); // Count the number of users with a specific role

    List<User> findByRole(Role role); // Find users by their role (ex., all drivers or all dispatchers)

    long countByLocked(boolean locked); // Count the number of users based on their locked status (useful for admin dashboard statistics)

}
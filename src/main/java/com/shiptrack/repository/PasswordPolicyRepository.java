package com.shiptrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shiptrack.model.PasswordPolicy;

public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {
}
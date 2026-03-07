package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {
}

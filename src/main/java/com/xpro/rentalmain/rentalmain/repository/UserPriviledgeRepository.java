package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.UserPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPriviledgeRepository extends JpaRepository<UserPrivilege, UUID> {
}

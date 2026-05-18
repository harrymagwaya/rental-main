package com.xpro.rentalmain.rentalmain.repository;

import com.xpro.rentalmain.rentalmain.entity.LoanAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanAdminRepository extends JpaRepository<LoanAdmin, UUID> {
}

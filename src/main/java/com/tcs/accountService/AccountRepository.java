package com.tcs.accountService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
	@Query(value = "SELECT a FROM Account a WHERE a.id = :id")
	Account findByAccountId(@Param("id") Long id);
}
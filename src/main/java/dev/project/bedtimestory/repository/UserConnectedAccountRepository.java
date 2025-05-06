package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.entity.UserConnectedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserConnectedAccountRepository extends JpaRepository<UserConnectedAccount, Long> {
    Optional<UserConnectedAccount> findByProviderAndProviderId(String provider, String providerId);
}


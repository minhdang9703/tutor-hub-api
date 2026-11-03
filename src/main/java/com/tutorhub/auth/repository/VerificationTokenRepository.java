package com.tutorhub.auth.repository;
import com.tutorhub.auth.entity.VerificationToken;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from VerificationToken t join fetch t.user where t.tokenHash = :hash") Optional<VerificationToken> findForUpdate(@Param("hash") String hash);
}

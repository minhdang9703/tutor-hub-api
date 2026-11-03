package com.tutorhub.auth.repository;
import com.tutorhub.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from RefreshToken t join fetch t.user where t.tokenHash = :hash") Optional<RefreshToken> findForUpdate(@Param("hash") String hash);
    @Modifying @Query("update RefreshToken t set t.revokedAt = CURRENT_TIMESTAMP where t.user.id = :userId and t.revokedAt is null") void revokeAllByUserId(@Param("userId") Long userId);
}

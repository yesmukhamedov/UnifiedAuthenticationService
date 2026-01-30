package men.yeskendyr.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import men.yeskendyr.auth.entity.IdentifierType;
import men.yeskendyr.auth.entity.UserIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentifierRepository extends JpaRepository<UserIdentifier, Long> {
    Optional<UserIdentifier> findByTypeAndValue(IdentifierType type, String value);

    boolean existsByTypeAndValue(IdentifierType type, String value);

    List<UserIdentifier> findAllByUserId(UUID userId);
}

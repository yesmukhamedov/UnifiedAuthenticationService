package men.yeskendyr.auth.repository;

import java.util.UUID;
import men.yeskendyr.auth.entity.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {
}

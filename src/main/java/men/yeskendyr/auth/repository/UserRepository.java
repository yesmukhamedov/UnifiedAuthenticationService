package men.yeskendyr.auth.repository;

import java.util.UUID;
import men.yeskendyr.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
}

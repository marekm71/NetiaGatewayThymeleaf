package pl.novaris.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.novaris.gateway.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}

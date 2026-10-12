package dev.leonardo.bvgtransit.commute.repository;

import dev.leonardo.bvgtransit.commute.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

package io.github.js.batch.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByEmailIn(Collection<String> emails);

    @Query("SELECT COALESCE(MIN(u.id), 0) FROM User u")
    long findMinId();

    @Query("SELECT COALESCE(MAX(u.id), 0) FROM User u")
    long findMaxId();

}
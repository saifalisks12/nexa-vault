package com.nexavault.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexavault.model.User;

public interface UserDao extends JpaRepository<User, String> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

}

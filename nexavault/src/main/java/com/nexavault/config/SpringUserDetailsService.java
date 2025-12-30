package com.nexavault.config;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.nexavault.dao.UserDao;
import com.nexavault.exception.NotFoundException;
import com.nexavault.model.User;

import lombok.RequiredArgsConstructor;

/**
 * Custom implementation of Spring Security's UserDetailsService. This service
 * is responsible for loading user details from the database.
 */
@Component
@RequiredArgsConstructor
public class SpringUserDetailsService implements UserDetailsService {

	private final UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		Optional<User> userInfo = userDao.findByEmail(email);
		return userInfo.map(SpringUserDetails::new)
				.orElseThrow(() -> new NotFoundException("Email_Id", "Constant.USER_NOT_FOUND" + email));
	}

}

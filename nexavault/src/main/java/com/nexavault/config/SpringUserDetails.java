package com.nexavault.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nexavault.model.User;

/**
 * Custom UserDetails implementation for Spring Security.
 */
public class SpringUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private String email;
	private String password;
	private String userId;
	private List<GrantedAuthority> authorities;

	public SpringUserDetails(User userInfo) {
		email = userInfo.getEmail();
		password = userInfo.getPassword();
		userId = userInfo.getId();
		authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(userInfo.getRole().getUserType().toUpperCase()));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public String getUserId() {
		return userId;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}

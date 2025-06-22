package com.roome.admin.roomeadminbe.global.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AdminDetails implements UserDetails {

	private final Long adminId;
	private final String email;
	private final String password;
	private final Collection<? extends GrantedAuthority> authorities;

	public AdminDetails(Long adminId, String email, String password, Collection<? extends GrantedAuthority> authorities) {
		this.adminId = adminId;
		this.email = email;
		this.password = password;
		this.authorities = authorities;
	}

	public Long getAdminId() {
		return adminId;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
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

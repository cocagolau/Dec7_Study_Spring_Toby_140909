package me.dec7.user.service;

import me.dec7.user.domain.User;

public interface UserService {
	
	void add(User user);
	void upgradeLevels();
}

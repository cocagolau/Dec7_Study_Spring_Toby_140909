package me.dec7.user.service;

import me.dec7.user.domain.User;

public interface UserLevelUpgradePolicy {

	void upgradeLevel(User user);
	boolean canUpgradeLevel(User user);

}
package me.dec7.user.service;

import me.dec7.user.domain.User;


public class TestUserService extends UserServiceImpl {
	
	private String id;
	
	// 예외를 발생시킬 User 오브젝트의 id를 지정할 수 있도록 함
	public TestUserService(String id) {
		this.id = id;
	}
	
	protected void upgradeLevel(User user) {
		if (user.getId().equals(this.id)) {
			throw new TestUserServiceException();
		}
		
		super.upgradeLevel(user);
	}
	
	


}

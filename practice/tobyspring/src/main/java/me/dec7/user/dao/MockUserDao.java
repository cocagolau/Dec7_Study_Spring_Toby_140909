package me.dec7.user.dao;

import java.util.ArrayList;
import java.util.List;

import me.dec7.user.domain.User;

public class MockUserDao implements UserDao {
	
	// 레벨 업그레이드 후보 User 오브젝트 목록
	private List<User> users;
	
	// 업그레이드 대상 오브젝트를 저장해 둘 목록
	private List<User> updated = new ArrayList<User>();
	
	public MockUserDao(List<User> users) {
		this.users = users;
	}
	
	public List<User> getUpdated() {
		return updated;
	}


	@Override
	public List<User> getAll() {
		
		return this.users;
	}
	
	@Override
	public void update(User user) {
		updated.add(user);
	}
	
	// 관련없는 메소드는 UnsupportedOperationException을 던지는 편이 나음
	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(User user) throws DuplicateUserIdException {
		throw new UnsupportedOperationException();
	}

	@Override
	public User get(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCount() {
		throw new UnsupportedOperationException();
	}


}

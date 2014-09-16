package me.dec7.user.dao;

import java.util.List;

import me.dec7.user.domain.User;

public interface UserDao {
	
	/*
	 * setDataSource는
	 * dao마다 달라질 수 있는 부분이므로 추가하지 않음
	 */

	void deleteAll();

	void add(User user) throws DuplicateUserIdException;

	User get(String id);

	int getCount();

	List<User> getAll();

	void update(User user);
	
	/*
	void add(Connection c, User user);

	User get(Connection c, String id);

	void update(Connection c, User user);
	*/

}
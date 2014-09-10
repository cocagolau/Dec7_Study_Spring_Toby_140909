package me.dec7.user.dao;


/*
 * UserDao의 생성할 역할을 맡음
 * 
 * DaoFactory를 분리했을 때 장점
 *  - 어플리케이션의 컴포넌트 역할을 하는 오브젝트와 어플리케이션의 구조를 결정하는 오브젝트를 분리했다는 것이 가장 중요
 */

/*
 * 1.4.2 Object Factory의 활용
 * 
 * 만약 DaoFactory가 UserDao 외에도 다양한 Dao를 생성한다면 ...
 * 문제 발생
 *  1. ConnectionMaker 구현 클래스의 Object를 생성하는 코드가 method마다 반복됨
 *     (어떤 ConnectionMaker를 사용할지 결정하는 코드)
 *   --> 중복을 분리 --> 별도의 method로 구성
 * 
 */
public class DaoFactory {

	public UserDao userDao() {
		// Factory의 UserDao를 어떻게 만들지 알고 있음
		// return new UserDao(new SimpleConnectionMaker());
		
		return new UserDao(connectionMaker());
	}
	
	public AccountDao accountDao() {
		// return new AccountDao(new SimpleConnectionMaker());
		
		return new AccountDao(connectionMaker());
	}
	
	public MessageDao messageDao() {
		// return new MessageDao(new SimpleConnectionMaker());
		
		return new MessageDao(connectionMaker());
	}
	
	
	// 분리해서 중복을 제거한 connectionMaker()
	public ConnectionMaker connectionMaker() {
		
		return new SimpleConnectionMaker();
	}
	
}

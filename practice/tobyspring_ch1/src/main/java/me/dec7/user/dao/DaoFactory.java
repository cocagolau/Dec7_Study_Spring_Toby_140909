package me.dec7.user.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration 
public class DaoFactory {
	
	/*
	 * Object 생성을 담당하는 IoC용 Method라는 의미
	 *  - userDao() method는 UserDao 타입의 Object를 생성하고 초기화하여 돌려주기 때문
	 * 
	 * method 이름은 Bean의 이름이 됨 
	 */
	@Bean
	public UserDao userDao() {
		
		return new UserDao(connectionMaker());
	}
	
	@Bean
	public AccountDao accountDao() {
		
		return new AccountDao(connectionMaker());
	}
	
	@Bean
	public MessageDao messageDao() {
		
		return new MessageDao(connectionMaker());
	}
	
	@Bean
	public ConnectionMaker connectionMaker() {
		
		return new SimpleConnectionMaker();
	}
	
}

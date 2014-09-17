package me.dec7.user.service;

import me.dec7.user.domain.User;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/*
 * 비지니스 트랜잭션 처리를 담은 UserServiceTx
 *  - 적어도 비지니스 로직에 대해서 UserServiceTx가 아무런 관여하지 않음
 */
public class UserServiceTx implements UserService {
	
	UserService userService;
	PlatformTransactionManager transactionManager;
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public void add(User user) {
		userService.add(user);
	}
	
	/*
	 * UserService에서 트랜잭선 처리 메소드와 비지니스 로직 메소드를 분시리
	 * 트랜잭션을 담당한 메소드와 거의 한 메소드가 됨
	 * 
	 * @see me.dec7.user.service.UserService#upgradeLevels()
	 */
	@Override
	public void upgradeLevels() {
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			userService.upgradeLevels();			
			this.transactionManager.commit(status);
			
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
	}

}

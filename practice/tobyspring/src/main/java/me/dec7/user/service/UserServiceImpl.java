package me.dec7.user.service;

import java.util.List;

import me.dec7.user.dao.UserDao;
import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;


/*
 * 어노테이션 트랜잭션 속성 / 포인트컷
 * 	- 메소드 이름 패턴을 사용해 일괄적 트랜잭션 속성 부여방법이 부적합
 *  	- 클래스/메소드에 따라 제각각 속성이 다른, 세밀하게 튜닝된 트랜잭션 속성 적용해야하는 경우
 *  	- 어노테이션 방법 사용
 *  
 * @Transactional

 package org.springframework.transaction.annotation;
 
 // 어노테이션을 사용할 대상(메소드 타입) 지정, 한개이상 가능
 // 따라서 메소드, 클래스, 인터페이스에 사용할 수 있음
 @Target ({ElementType.METHOD, ElementType.TYPE})
 
 // 어노테이션 정보가 언제까지 유지되는지 지정.  runtime시도 어노테이션 정보를 reflection을 사용해 얻을 수 있음 
 @Retention (RetintionPolicy.RUNTIME)
 
 // 상속을 통해서도 어노테이션 정보를 얻을 수 있음
 @Inherited
 
 @Documented
 
 public @interface Transactional {
 	String vlaue() 					default "";
 	Propagation propagation() 		default Propagation.REQUIRED;
 	Isolation isolation() 			default Isolation.DEFAULT;
 	int timeout() 					default TransactionDefinition.TIMEOUT_DEFUALT;
 	boolean readOnly()				default false;
 	Class<? extends Throwable>[] rollbackFor() 	default {};
 	String[] rollbackForClassName()				default {};
 	Class<? extends Throwable>[] noRollbackFor() default {};
 	String[] noRollbackForClassName() 			default {};
 }
 
 
 *  - 스프링은 @Transactional 이 부여된 모든 오브젝트를 target 오브젝트로 인식
 *  - TransactionAttributeSourcePointcut 사용됨
 *  	- 스스로 표현식과 같은 선정기준을 가지고 있지 않음
 *  	- 동시에 포인트컷 자동등록에 사용
 *		  	- 대신 @Transactional이 타입/메소드 레벨이든 관계없이 부여된 bean 오브젝트를 모두 찾아
 *			  포인트컷 선정결과로 돌려줌
 *
 *  - Advisor 동작방식
 *  	- AnnotationTransactionAttributeSource를 사용해 @Transactional 어노테이션의 엘리먼트에서 트랜잭션 속성을 가져옴
 *  		- TransactionInterceptor 사용안함
 *  	- 따라서 메소드마다 다르게 설정 가능
 *  
 * 대체정책
 *  - 어노테이션 적용단위는 메소드로 메소드마다 어노테이션과 속성이 부여되면 유연할 수 있지만 코드는 중복/더러워질 수 있음
 *  - @Transactional을 4단계의 fallback(대체정책)을 이용해줌
 *  	- target method, target class, declare method, declare type(class, interface) 순서로
 *  	  @Transactional 적용 확인
 *  	- 먼저 발견되는 속성정보를 사용함
 *  	- 끝까지 찾지 못하면 해당 메소드는 적용대상이 아니라 판단

	ex)
	
	// 4.
	public interface Service {
		// 3.
		void method1();
		// 3.
		void method2();
	}
	
	// target class 
	// 2. 어노테이션 발견시, 해당 클래스의 모든 메소드는 공통적으로 적용되는 속성
	 * 특정 메소드만 적용되지 않을 경우 @Transactional 부여
	public class ServiceImpl implements Service {
		
		// 1. 어노테이션 발견시 속성을 해당 메소드 트랜잭션 속성 사용
		public void method1() { }
		
		// 1.
		public void method2() { }
	}
	
	- 올바른 적용위치
		- @Transactional을 대체정책을 사용해 효율적, 세밀 제어 가능
		- 먼저 타입레벨에서 정의하고 공통속성을 따르지 않는 메소드에 대해서만 메소드 레벨에서 재정의하는 방법으로 사용
		- 기본적으로 @Transactional 적용대상은 클라이언트가 사용하는 interface가 정의한 메소드
		  - 따라서, target class보다 interface에 두는게 바람직
		  - 하지만, Proxy 방식 AOP가 아닌 다른 방식으로 트랜잭션을 적용하면
		  		  interface에서 정의한 어노테이션은 무시됨
		  		  따라서, 안전하게 target class에 두는 방법을 권장
		  - interface에 @Transactional 적용시 구현 클래스가 바뀌어도 트랜잭션 속성 유지할 수 있는 장점 


 */
public class UserServiceImpl implements UserService {
	
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
	
	protected UserDao userDao;
	private MailSender mailSender;
		
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
		
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	protected void upgradeLevel(User user) {
		user.upgradeLevel();
		
		// 수정된 사용자 정보를 DB에 반영
		userDao.update(user);
		sendUpgradeEmail(user);
	}
	
	private void sendUpgradeEmail(User user) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("useradmin@ksug.org");
		mailMessage.setSubject("Upgrade 안내");
		mailMessage.setText("사용자님의 등급이 " + user.getLevel().name() + "로 업그레이드되었습니다.");
		
		mailSender.send(mailMessage);
	}

	public void upgradeLevels() {
		
		List<User> users = userDao.getAll();
		
		for (User user : users) {
			// upgrade 가능시
			if (canUpgradeLevel(user)) {
				// upgrade 해라
				upgradeLevel(user);
			}
		}
	}

	protected boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();
		
		// level별로 구분해 조건을 판단
		switch (currentLevel) {
			case BASIC	: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
			case SILVER	: return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
			case GOLD	: return false;
			default		: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
		}
	}

	public void add(User user) {
		if (user.getLevel() == null) {
			user.setLevel(Level.BASIC);
		}
		
		userDao.add(user);
	}

	@Override
	public User get(String id) {
		
		return userDao.get(id);
	}

	@Override
	public List<User> getAll() {
		
		return userDao.getAll();
	}

	@Override
	public void deleteAll() {
		userDao.deleteAll();
	}

	@Override
	public void update(User user) {
		userDao.update(user);
	}

}

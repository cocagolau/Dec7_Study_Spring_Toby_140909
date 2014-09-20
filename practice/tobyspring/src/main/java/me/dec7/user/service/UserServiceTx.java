package me.dec7.user.service;

import me.dec7.user.domain.User;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/*
 * 6.3.1, 프록시, 프록시 패턴, 데코레이터 패턴
 * 
 * 1. 전략패턴 적용
 * 	- 단순 확장성 고려시
 *  - 전략 패턴은 단순히 트랜잭션 기능의 구현내용을 분리했을 뿐
 *  - 구체적 구현코는 제거했지만, 위임을 통해 사용하는 코드는 핵심 코드와 함께 남아있음
 *  
 *  
 * 2. UserServiceTx
 *  - 트랜잭션 기능을 비지니스 로직과 성격이 다르므로 적용 사실 자체를 밖으로 분리
 *  - UserServiceImpl에는 트랜잭션 코드가 완전히 사라짐
 *  - 중요한 특징
 *  	- 부가기능 외의 나머지 모든 기능은 원래의 핵심기능 가진 클래스로 위임해야함
 *  	- 핵심기능은 부가기능을 가진 클래스 존재를 모름
 *  		따라서, 부가기능이 핵심기능을 사용하는 구조가 됨
 *  - 문제점
 *  	- 클라이언트가 핵심기능을 가진 클래스를 직접 사용시 부가기능이 적용될 기회가 사라짐
 *  		- 따라서, 부가기능은 마치 자신이 핵심기능을 가진 클래스처럼 꾸며,
 *  		  클라이언트가 자신을 거쳐 핵심기능을 사용하도록 해야함
 *  		- 이를위해, 클라이언트를 인터페이스를 통해서만 핵심기능 사용
 *     		  부가기능도 같은 인터페이스를 구현한 뒤 자신이 그 사이에 낌
 *     		- 클라이언트는 인터페이스만 보고 사용
 *     		  자신은 핵심기능을 사용한다고 생각하지만 사실은 부가기능을 통해 핵심기능 사용
 *     
 * 2-1) 프록시(proxy, 대리자)
 *  - 클라이언트가 사용하려는 실제 대상인 것처럼 위장ㄴ 후 클라이언트의 요청을 받아주는 것
 *  - target, 실체
 *  	- 프록세를 통해 최종적으로 요청을 위임받아 처리하는 실제 오브젝트
 *  - 특징
 *  	- 타깃과 같은 interface 구현
 *  	- 프록시가 타깃을 제어할 수 있는 위치
 *  - 구분
 *  	1) 클라이언트가 타깃에 접근하는 방법을 제어하기 위함
 *  	2) 타깃에 부가적인 기능을 부여하기 위함
 *  
 *  
 * 3. 데코레이터 패턴
 *  - 타깃에 부가적인 기능을 런타임시 다이나믹하게 부여하기 위해 프록시를 사용하는 패턴 (컴파일시 방법, 순서 결정 안됨)
 *  - 프록시가 꼭 한개로 제한되지 않음
 *  	- 같은 인터페이스를 구현한 타겟과 여러개의 프록시를 사용할 수 있음
 *  - 프록시로 동작하는 각 데코레이터는 위임하는 대상도 인터페이스로 접근하므로 자신이 최종타깃인지 다음단계의 데코레이터 프록시로 위임하는지 모름
 *  	- UserServiceImpl, UserServiceTx
 *  
 *  
 * 4. 프록시 패턴
 *  - 프록시 용어와 디자인 패턴의 프록시 패턴은 구분할 필요 있음
 *  	- 프록시
 *  		- 클라이언트와 사용 대상 사이의 대리 역할 맡은 오브젝트
 * 		- 프록시 패턴
 * 			- 프록시를 사용하는 방법 중 타깃에 접근하는 방식을 제어하려는 목적을 가진 경우
 * 	- 프록시 패턴의 프록시는 타깃의 기능을 확장하거나 추가 않음
 *  - 대신 클라이언트가 타깃에 접근하는 방식을 변경  
 * 
 */

/*
 * 6.3.2, 다이내믹 프록시
 *  - 프록시는 기존 코드에 영향을 미치지 않으며넛 타깃의 기능을 확장 / 접근방법을 제어할 수 있는 유용한 방법
 *  - 하지만 프록시를 만드는 일을 번거롭다고 생각
 *  - java.lang.reflect 패키지 안에 프록시를 쉽게 만들 수 있는 지원 클래스 존재
 *  
 * 구성 기능
 *  1. 타깃과 같은 메소드 구현하고, 메소드가 호출시 타깃 오브젝트로 위임
 *  2. 지정된 요청에 대해서 부가기능 수행
 *  
 * 프록시 만들기가 번거로운 이유
 *  1. 타깃의 인터페이스를 구현하고 위임하는 코드를 작성하기 번거로움
 *     부가기능이 필요없는 메쇠드도 구현해서 타깃으로 위임하는 코드를 일일이 생성
 *     타깃 인터페이스의 메소드 추가, 변경될 때 함께 수정해줘야함
 *  2. 부가기능 코드가 중복될 가능성이 많음
 *      - DB를 사용하는 메소드는 대부분의 로직에 적용될 필요 있고 그 과정에서 코드가 중복될 가능성이 높음
 *      
 *  - 중복코드는 어떻게든 해결하겠지만
 *    인터페이스 메소드 구현, 위임은 어려움
 *    	- 이를 해결하는데 유용한 것이 JDK의 다이내믹 프록시임
 *    
 *    
 * 리플렉션
 *  - 자바의 코드 자체를 추상화해서 접근하도록 만든 것
	
	String name = "String";
	
	name의 길이를 알려면 length() 메소드를 호출하면 됨
	
	자바의 모든 클래스는 클래스 그 자체 구성정보를 담은 Class 타입의 오브젝트를 가지고 있음
		- 클래스이름.class / 오브젝트의 getClass() 호출시 Class타입의 오브젝트를 가져올 수 있음
	
	클래스 오브젝트를 이용시
		1. 클래스 코드에 대한 메타정보를 자져오거나
		2. 오브젝트를 조작할 수 있음
		- ex) 클래스이름, 상속된 클래스, 구현된 인터페이스, 가진 필드, 각각의 타입,
		      정의된 메소드, 메소드의 파라미터와 리턴타입,
		      오브젝트 필드의 값을 읽고수정, 원하는 파라미터값을 이용해 메소드 호출
		      
	lenth() 메소드를 실행하는 방법
		- 특징
			- String이 가진 메소드 중 "length"라는 이름
			- 파라미터 없는 메소드 정보
		- 실행
			- java.lang.reflect.Method interface에 정의된 invoke() 메소드 사용
		- 실행방법
			- public Object invoke(Object obj, Object... args)
				- 실행시킬 대상 오브젝트와 파라미터 목록을 받아 
				  메소드를 호출한 뒤
				  결과를 Object 타입으로 돌려줌
			- int length = lengthMethod.invoke(name);
			
	me.dec7.learningtest.jdk 참고

 */

public class UserServiceTx implements UserService {
	
	// 타킷 오브젝트
	UserService userService;
	
	PlatformTransactionManager transactionManager;
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	// 메소드 구현과 위임
	@Override
	public void add(User user) {
		// 위임
		userService.add(user);
	}
	
	// 메소드 구현
	@Override
	public void upgradeLevels() {
		// 부가기능 수행
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			// 위임
			userService.upgradeLevels();
			
		// 부가기능 수행
			this.transactionManager.commit(status);
			
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
	}

}

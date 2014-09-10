package me.dec7.user.dao;

import java.sql.SQLException;

import me.dec7.user.domain.User;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		/*
		 * DaoFactory와 Spring ApplicationContext를 사용시 중요한 차이점
		 *  - 호출시 반환되는 Object는 동일한가?
		 *  
		 *  - DaoFactory는 호출할 때마다 새로운 UserDao object 생성
		 *  - Spring ApplicationContext는 여러번 호출하더라도 동일한 object 반환
		 */
		
		/*
		 * Object의 동일성과 동등성
		 *  - 두 Object가 같은가?
		 *  - 동일성
		 *  	- ==
		 *  	- 두 Object가 완전히 동일할 때
		 *  	- 하나의 Object만 존재, 두 개의 Object reference 변수를 가지고 있을 때
		 *  	
		 *  - 동등성
		 *  	- equals()
		 *  	- 두 Object가 동일한 정보를 담고 있을 때
		 *  	- 두 개의 서로 다른 Object가 메모리상에 존재, 동등성 규칙에 따라 동등하다고 판별한 뿐 
		 */
		ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
		UserDao dao = context.getBean("userDao", UserDao.class);
		
		/*
		 * ApplicationContext
		 *  - IoC container
		 *  - singleton registry
		 *  	- 싱글톤을 저장하고 관리
		 *  	- spring은 기본적으로 내부에서 생성하는 bean object를 모두 싱글톤으로 생성
		 *  	- singleton pattern과 그 구현법이 다름
		 */
		
		/*
		 * Server Application에서 Singleton
		 *  - 대규모 서버환경은 서버 한 대, 초당 수백번의 브라우져 요청을 처리
		 *  - 그 때마다 각 로직을 담당하는 Object를 생성시 큰 부하가 발생
		 *  - enterpise 분야에서 서비스 object 개념을 사용
		 *  	- 따라서 servlet은 대부분 멀티스레드 환경에서 singleton으로 동작
		 *  	- servlet class 당 하나의 object만 생성하고 요청을 담당하는 여러 스레드에서 하나의 object공유해 사용
		 */
		
		/*
		 * Singleton Pattern
		 *  - 어떤 class를 어플리케이션 내에서 제한된 instance 개수 혹은 하나만 존재하도록 강제하는 패턴
		 *  - 이 class의 object는 어플리케이션 내에서 전역적으로 접근 가능
		 *  - 단일 object만 존재하야하고, 이를 어플리케이션 여러 곳에서 공유하는 경우 사용됨
		 *  
		 * 한계
		 *  - priavte 생성자로 상속 불가
		 *  	- 상속, 다형성 등 객체지향의 특성 이용 불가
		 *  	- 스태틱 필드와 메소드를 사용
		 *  - 테스트가 힘듦
		 *  	- 초기화시 오브젝트 주입하기 힘듦 
		 *  	- 만들어지는 방식이 제한적이라 테스트용 목 오브젝트로 대체 힘듦
		 *  - 서버환경에서 싱글톤이 하나만 만들어지는 것을 보장하기 어려움 
		 *  	- jvm이 분산되어있을 경우 독립적으로 object가 생성됨
		 *  - 전역상태를 만들 수 있으므로 좋지 않음 
		 *  	- static method를 이용하고 어플리케이션 모든 공간에서 접근 가능하여 자연스럽게 전역 상태로 사용되기 쉬움
		 */
		
		/*
		 * Singleton Registry
		 *  - spring이 singleton 형태의 오브젝트를 생성, 관리하는 singleton container
		 *  - private 생성자를 사용하지 않고 평범한 자바 클래스를 singleton으로 활용
		 *  - 객체지향적 설계방식, 원칙, 디자인 패턴을 적용하는데 제약이 없다는 점이 가장 중요
		 */
		
		/*
		 * 1.6.3 / Spring bean의 scope
		 * 
		 * bean의 scope
		 *  - bean이 생성, 존재, 적용되는 범위
		 *  - 기본 scope는 singleton
		 *  - prototype
		 *  	- container에 bean을 요청할 때마다 새로운 object를 생성
		 *  - request
		 *  	- http요청이 생길 때마다 생성
		 *  - session
		 *  	- web의 session scope와 유사
		 * 
		 */
		User user = new User();
		user.setId("dec7");
		user.setName("동규");
		user.setPassword("127");
		
		dao.add(user);
		System.out.println(user.getId() + "등록 성공");
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
		System.out.println(user2.getPassword());
		
		System.out.println(user2.getId() + "조회 성공");

	}
	
}

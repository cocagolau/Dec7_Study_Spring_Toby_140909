package me.dec7.user.dao;

import java.sql.SQLException;

import me.dec7.user.domain.User;

public class UserDaoTest {

	/*
	 * UserDao에서 UserDaoTest로 이동
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		/*
		 * UserDao 오브젝트가 SimpleConnectionMaker 오브젝트를 사용하게 하려면
		 * 두 클래스의 오브젝트 사이에 Runtime 사용관계 링크 (의존관계)를 맺어주면 됨
		 * 
		 * UserDao Client의 역할은 
		 * 	- Runtime 오브젝트 관계를 갖는 구조로 만들어주는 것
		 * 
		 * 새로운 관심사항을 UserDao Client로 넘기면 됨
		 * 
		 */
		SimpleConnectionMaker connectionMaker = new SimpleConnectionMaker();
		UserDao dao = new UserDao(connectionMaker);
		
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
	/*
	 * 객체지향 설계원칙 (SOLID)
	 *  - 단일 책임 원칙
	 *  - 개방 폐쇄 원칙
	 *  - 리스코프 치환 원칙
	 *  - 인터페이스 분리 원칙
	 *  - 의존관계 역전 원칙
	 */
	
	/*
	 * 1.3.4 원칙과 패턴
	 * 
	 * 개발 폐쇄 원칙 (OCP, Open-Closed Principle)
	 *  - 깔끔한 설계를 위해 적용 가능한 객체지향 설계원칙 중 한가지
	 *  - 정의: 클래스나 모듈은 확장에 열려있고 변경에는 닫혀있어야 한다.
	 * 
	 * 
	 * 높은 응집도와 낮은 결합도
	 *  - 높은 응집도: 하나의 모듈/클래스가 하나의 책임/관심사에 집중됨
	 *  	- 작업은 항상 전체적으로 일어남
	 *  	- 무엇을 변경할 지 명확 
	 *  	- 기능에 영향을 주지 않음
	 *  - 낮은 결합도
	 *  	- 책음/관심사가 다른 오브젝트/모듈과는 느슨하게 연결되어야 함
	 *  	- 결합도가 낮아지면 변화 대응 속도는 빨라지고, 구성이 깔끔하며, 확장도 용이함
	 *  
	 * 전략패턴
	 *  - 개선한 UserDaoTest - UserDao - ConnectionMaker 구조에 해당함
	 *  - 자신의 기능 맥락에서 필요에 따라 변경이 필요한 알고리즘 인터페이시를 통해 통째로 외부로 분리 후
	 *    이를 구현한 구체적인 알고리즘 클래스를 필요에 따라 바꾸서 사용할 수 있게 한 패턴
	 *    
	 *  UserDao는 전략 패턴의 컨텍스트에 해당함
	 *   - DB 연결방식이라는 알고리즘을 ConnectionMaker라는 interface로 정의
	 *   - 이를 구현한 Class로 전략을 바꾸어가면서 사용
	 */
	
}

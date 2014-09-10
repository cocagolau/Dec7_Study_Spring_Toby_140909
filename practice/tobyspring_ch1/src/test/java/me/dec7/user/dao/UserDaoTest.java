package me.dec7.user.dao;

import java.sql.SQLException;

import me.dec7.user.domain.User;

/*
 * 1.4.1 Object Factory
 * 
 * UserDaoTest는 기존 UserDao가 직접 담당하던 기능 (ConnectionMaker 구현 클래스를 사용을 결정하는 기능)을 맡게됨.
 * UserDaoTest는 UserDao가 잘 동작하는지 Test를 위한 만들어졌으나 또 다른 책임을 맡게 되었음
 * 
 * 분리시킬 기능을 담당할 클래서 : 팩토리
 */

/*
 * 1.4.3 제어관계 역전
 * 
 * 제어의 역전
 *  - 프로그램 제어 흐름 구조가 바뀌는 것
 *  
 * 기존, main() method
 *  - 프로그램이 시작되는 시점에서
 *  	- 다음에 사용할 오브젝트 결정, 
 *  	- 결정한 오브젝트 생성
 *  	- 생성된 오브젝트에 있는 메소드 호출.. 등의 작업을 반복
 *  - 이러한 프로그램 구조에서 각 오브젝트
 *  	- 프로그램 흐름을 결정하거나 사용할 오브젝트 구성하는 작업에 능동적 참여
 *  
 * 제어의 역전
 *  - 위의 제어 흐름을 거꾸로 뒤집는 것
 *  	- 오브젝트는 자신이 사용할 오브젝트를 스스로 선택하지 않음
 *  	- 생성도 않함
 *  	- 자신이 어떻게 만들어져 어디에 사용되는지 모름
 *  - 모든 제어 권한을 자신이 아닌 다른 대상에게 위임
 *  - Servlet
 *  	- 개발자는 servlet을 개발할 수는 있지만
 *  	- 그 실행을 개발자가 직접 제어할 수는 없음
 *  	- 대신 그 제어권한을 가진 Container가 적절한 시점에 Servlet object를 만들고 내부의 method를 호출
 *  
 * 초난감 Dao
 * 	- subclass가 구현한 getConnection()도 제어의 역전 개념이 사용됨
 *  - subclass는 getConnection()이 언제 불릴지 모름
 *  
 *  
 * Library
 *  - 라이브러리를 사용하는 코드는 어플리케이션이 흐름을 직접 제어
 *  
 * Framework
 *  - 반제품, 확장해서 사용할 수 있도록 준비된 추상 라이브러리 집합이 아님 
 *  - 어플리케이션 코드가 framework에 의해 사용됨
 *  
 *  - UserDao, DaoFactory도 IoC가 적용
 *  
 * IoC
 *  - Framework, Container 같이 어플리케이션 컴포넌트의 생성, 관계설정, 사용, 생명주기 관리 등을 관리할 존재가 필요
 */
public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		/*
		 * UserDao의 오브젝트를 생성하는 부분은 DaoFactory로 옮겨감
		 * 
		SimpleConnectionMaker connectionMaker = new SimpleConnectionMaker();
		UserDao dao = new UserDao(connectionMaker);
		*/
		
		/*
		 * UserDaoTest는 더 이상 UserDao가 어떻게 만들어지는지 관심이 없음
		 * 
		 * UserDao, ConnectionMaker는 각각 핵심적 데이터 로직과 기술 로직을 담당
		 * DaoFactory는 오브젝트들을 구성, 그 관계를 정의하는 책임
		 */
		UserDao dao = new DaoFactory().userDao();
		
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

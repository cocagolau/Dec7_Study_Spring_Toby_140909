package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import me.dec7.user.domain.User;

/*
 * DAO (Data Access Object)
 *  - DB를 사용해 데이터 조회/조작하는 기능을 전담토록 만든 Obj
 */

/*
 * 지금의 Dao는 많은 문제점을 가지고 있음
 *  - p62, 관심사의 분리가 필요
 *  	- 개발의 요구사항은 끝없이 변화하므로 개발자는 객체 설계시 미래의 변화를 어떻게 대비할지 준비해야 함
 *  	- 객체지향 기술은 실세계를 가깝게 모델링한다기보다는 세계를 추상적으로 구성하고 이를 자유롭게 변경/확장할 수 있다는 것이 중요
 *  	- 미래 준비: 변화를 빠르게 대응하고 오류를 최소화
 *  		- 분리와 확장을 고려한 설계
 *  	
 *  	- AOP (Aspect Oriented Programming)
 *  		- 보통 변화는 집중된 한 가지 관심에 대해 일어남 --> 작업은 그러지 못함 (기술이 엮여있어 다른 곳에 영향을 미침)
 *  		- 즉, 한 가지 관심이 한 군데 집중되도록 하는 것
 *  
 *   	- 관심사의 분리 (Separation Of Concerns)
 *   		- 관심이 같은 것끼리 한 객체로 모으는 것.
 */

/*
 * 1.2.2
 * UserDao 관심사항
 *  1. DB와 연결을 위한 Connection을 어떻게 가져올까?
 *  	- DB 종류, 드라이버 종류, 로그인 정보, Connection 형성 방법 ... // --> DB 연결과 관련된 관심
 *  2. SQL문장을 담을 Statement를 만들고 실행하는 것
 *  3. 작업 종료시 Statement와 Connection을 종료하는 것
 */
public class UserDaoImpl extends UserDao {
	
	/*
	 * 상속을 통한 확장을 하여 UserDao의 기능을 이용할 수 있고
	 * DB가 변경되어도 연결부분만 따로 구현해주면 됨
	 * 
	 * (non-Javadoc)
	 * @see me.dec7.user.dao.UserDao#getConnection()
	 */
	@Override
	protected Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/springbook", "spring", "book");
		
		return conn;
	}
	
	/*
	 * main()을 이용한 DAO 테스트 코드
	 *  - 가장 간단한 테스트 방법으로 Obj 스스로 자신을 검증하는 방법
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		UserDaoImpl dao = new UserDaoImpl();
		
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





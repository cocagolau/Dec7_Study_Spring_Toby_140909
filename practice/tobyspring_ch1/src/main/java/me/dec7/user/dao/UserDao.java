package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.dec7.user.domain.User;


/*  
 * 1.6.2
 * Singletone과 Object 상태
 *  - 멀티스레드 환경에서 여러 스레드가 동시 접근할 수 있으므로 상태관리를 조심해야함 / 무상태 방식
 *  	- 읽지전용 
 *  	- 각 요청에 대한정보, DB, 서버의 리소스에서 생성한 정보의 처리
 *  		--> 파라미터, 로컬변수, 리턴 값 등을 활용 
 */
public class UserDao {
	/*
	 * 관계없음
	 *  1. 초기 설정 후 바뀌지 않는 읽기 전용
	 *  2. @Bean을 통해 Spring이 생성한 bean
	 *  	- 별다른 설정이 없다면 기본적으로 object 한 개만 생성됨
	 */
	private ConnectionMaker connectionMaker;
	
	/*
	 * 만약 아래처럼 매번 새로운 값으로 바뀌는 instance 변수는 심각한 문제가 발생
	 * 
	 * private Connection conn;
	 * private User user;
	 */

	public UserDao(ConnectionMaker connectionMaker) {

		this.connectionMaker = connectionMaker;
	}

	public void add(User user) throws SQLException, ClassNotFoundException {
		Connection c = this.connectionMaker.makeConnection();
		
		PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		
		ps.close();
		c.close();
	}

	public User get(String id) throws SQLException, ClassNotFoundException {
		Connection c = this.connectionMaker.makeConnection();
		
		PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		rs.next();
		
		User user = new User();
		user.setId(rs.getString("id"));
		user.setName(rs.getString("name"));
		user.setPassword(rs.getString("password"));
		
		rs.close();
		ps.close();
		c.close();
		
		return user;
	}
}









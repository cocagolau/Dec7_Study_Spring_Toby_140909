package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.dec7.user.domain.User;


/*
 * 1.7.4 / 의존관계 주입의 응용
 * 
 * 1. 기능구현의 교환
 *  - DB를 변경할 경우, 기존 DI를 사용하지 않았다면 변경할 곳은 엄청 많을 것임 / 수정 후 오류관리도 필요
 *  - DI를 이용했다면, ConnectionMaker interface를 구현한 새로운 class로만 교체
 *  
 * 2. 부가기능 추가
 *  - 만약 Dao아 DB 연결횟수를 세야할 경우, connection을 연결하는 부분에 코드를 추가, 종료 후 제거하기 보다
 *    container가 사용하는 설정정보만 수정하여 런타임 의존관계만 새롭제 정의
 *    
 *    me.dec7.user.dao.CountingConnectionMaker 참조
 *    me.dec7.user.dao.DaoFactory 참조
 * 
 */
public class UserDao {
	private ConnectionMaker connectionMaker;

	/*
	 * 1.7.2 / Runtime 의존관계 설정
	 * 
	 * 의존관계
	 *  - A가 B에 의존하고 있다는 것은 B가 변경될 때 그것이 A에 영향을 미친다는 것
	 *  - 의존관계는 반드시 방향성이 있음
	 *  	- B는 A에 의존하지 않음
	 */
	
	/*
	 * UML에서 말하는 의존관계 / 설계모델 관점
	 *  - UserDao는 ConnectionMaker interface에 의존
	 *  	- interface가 변경시 그 영향을 UserDao가 받음
	 *  	- ConnectionMaker를 구현한 Class를 다른 것으로 바뀌어도 UserDao에는 영향을 주지 않음
	 *  		- interface에 대해서만 의존관계 형성시 구현 class와 관계가 느슨해지며 변화에 덜 영향받음
	 *  		- 결합도가 낮은 상태
	 *  
	 * Runtime시 Object사이에서 만들어지는 의존관계
	 *  - 설계시점의 의존관계가 실체화된 것
	 *  - 모델링 시점의 의존관계와 성격이 다름
	 *  - 실제 사용대상인 오브젝트 (dependent object, 의존 오브젝트)
	 *  
	 * 의존관계 주입
	 *  - 구체적의 의존 오브젝트, 그것을 사용할 주체를 client가 runtime시 연결해주는 작업
	 *   1. class, model에는 runtime시점의 의존관계가 드러나 있지 않음 / interface에만 의존
	 *   2. runtime 시점의 의존관계는 제3의 존재가 결정 (container, factory)
	 *   3. 의존관계는 사용할 오브젝트에 대한 레퍼런스를 외부에서 제공함으로 이루어짐
	 *   
	 * DI설계의 핵심
	 *  - 설계시점에 몰랐던 두 오브젝트의 관계를 runtime시 도와주는 제3의 존재가 있음.
	 *    ApplicationContext, BeanFactory, IoC container...
	 */
	
	// 생성자를 통한 DI
	/*
	public UserDao(ConnectionMaker connectionMaker) {
		this.connectionMaker = connectionMaker;
	}
	*/
	
	/*
	 *  setter method DI 방식을 적용
	 *  DaoFactory도 함께 수정해야함
	 */
	public void setConnectionMaker(ConnectionMaker connectionMaker) {
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









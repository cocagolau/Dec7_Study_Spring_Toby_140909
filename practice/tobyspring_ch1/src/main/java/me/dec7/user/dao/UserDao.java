package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.dec7.user.domain.User;

/*
 * 1.3.1
 * 
 * 추상클래스와 서브클래스와 구분을 통해
 * 변화의 성격이 다른 관심사를 분리했지만 단점이 많은 '상속' 방법을 이용했다는 점이 문제
 * 
 * 관심사가 다르고 변화의 셩격이 다른 두 코드를 Class를 분리하여 분리
 * 
 * DB connection을 사용하는 코드를
 * SimpleConnectionMaker라는 Object를 만들어 저장
 * 따라서 UserDao는 더 이상 abstract일 필요가 없음
 */
public class UserDao {
	
	/*
	 * p75, 하지만 SimpleConnectionMaker를 만든 후 새로운 문제가 발생함
	 *  1. SimpleConnectionMaker는 특정 DB에 종속됨
	 *  2. UserDao는 SimpleConnectionMaker라는 특정 Class에 종속됨
	 *  	- UserDao 코드 수정 없이 DB connection 생성 기능을 변경할 수 없음
	 *  
	 *  이렇게 클래스를 분리한 후에 상속처럼 자유로운 확장이 가능토록하기 위해 두 가지 문제를 해결해야함
	 *  	1. SimpleConnectionMaker의 makeNewConnection() method가 문제
	 *  		- 다른 SimpleConnectionMaker에서 connection을 가져오는 이름이 다른 경우
	 *  		- conneciton을 가져오는 코드를 사용하는 get, add 등 수 많은 method를 수정해야함
	 *  	2.  UserDao가 DB connection을 제공하는 Class를 구체적으로 알고 있어야 하는 문제 
	 *  		- 만약 SimpleConnectionMaker가 아닌 다른 클래스를 구현시 UserDao를 수정해야함
	 *  
	 *  해결: interface 도입
	 *   - 두 개의 Class가 서로 긴밀하게 연결되지 않도록 중간에 추상적인 느슨한 연결고리 (interface)를 만들어 줌
	 *   - 추상화는 어떤 공통적인 성격을 뽑아내 이를 따로 분리하는 작업
	 *   - interface는 자신을 구현한 클래스에 대한 구체적인 정보를 모두 감추어 버림
	 *   - 결국 구체적 Class 하나를 선택해야겠지만 추상화 (interface)를 사용하는 쪽에서는 사용할 Class가 무엇인지 알 필요 없음   
	 */
//	private SimpleConnectionMaker simpleConnectionMaker;
//	private ConnectionMaker simpleConnectionMaker;
	
//	interface를 통해 Object에 접근하므로 구체적인 Class 정보를 알 필요 없음 
	private ConnectionMaker connectionMaker;

	
	/*
	 * 1.3.3
	 * 하지만 문제는 남아있음
	 * 
	 * DB connection을 제공하는 Class에 대한 구체적인 정보는 제거했지만
	 * 어떤 Class의 Object를 사용할지를 결정하는 코드는 남아 있음
	 * 
	 * UserDao 내부에 아직 분리되지 않은 또 다른 '관심사항'이 남아있기 때문
	 * 
	 * 분리되지 않은 새로운 관심사항
	 *  - UserDao가 사용할 ConnectionMaker의 특정 구현 Class사이의 관계를 설정해 주는 것에 대한 관심
	 *  
	public UserDao() {
		// simpleConnectionMaker는 상태를 관리하지 않으므로 instance를 한개만 만들어 저장 후 재사용
		this.connectionMaker = new SimpleConnectionMaker();
	}
	 */
	
	/*
	 * UserDao를 사용하는 Client가
	 * UserDao와 ConnectionMaker 구현 클래스 관계를 결정해주는 기능을 분리해서 두기 적절하 곳임 
	 * 
	 * Object 사이의 관게는 Runtime시 한쪽이 다른 Object의 Reference를 가지고 있는 방식으로 구현됨 
	 * 
	 * connectionMaker = new SimpleConnectionMaker();
	 * 
	 * 위 코드는 SimpleConnectionMaker Object의 레퍼런스를 connectionMaker 변수에 넣어서 사용하게 함으로써
	 * 두 Object가 '사용'이라는 관계를 맺게 해줌
	 * 
	 * UserDao의 모든 코드는 ConnecitonMaker interface 외에는 어떤 Class와 관계를 가져서는 안되게 해야함
	 * 물론 UserDao가 동작하기 위해서는 특정 Class의 오브젝트와 관계를 맺어야 함
	 *  - 클래스 사이의 관계가 아닌
	 *  - 오브젝트 사이의 다이나믹한 관계가 만들어짐
	 *  	- 중요한 것
	 *  		- 클래스 사이의 관계는 코드에 다른 클래스의 이름이 나타나기 때문에 발생됨
	 *  		- 오브젝트 사이의 관계는 코드에 특정 클래스를 전혀 모르더라도
	 *  		  해당 클래스가 구현한 인터페이스를 사용시, 클래스의 오브젝트를 인터페이스 타입으로 받아서 사용할 수 있음 (다형성) 
	 */
	
	/*
	 *  Cient가 만든 connectionMaker의 오브젝트를 전달 할 수 있도록 파라미터가 추가된 생성자 구현
	 *  
	 *  이로써 UserDao는 SQL을 생성, 실행에 집중되어짐
	 *  connection과 관련된 방법에 영향을 받지 않음
	 */
	public UserDao(SimpleConnectionMaker connectionMaker) {
		/*
		 * SimpleConnectionMaker가 사라질 수 있었던 것은
		 * connectionMaker 구현 클래스의 오브젝트 간 관계를 맺는 책임을 UserDao Client에게 넘겼기 때문
		 */
		this.connectionMaker = connectionMaker;
	}

	public void add(User user) throws SQLException, ClassNotFoundException {
		// Connection c = getConnection();
		// Connection c = this.simpleConnectionMaker.makeNewConnection();
		
		// interface에 정의된 method를 사용하므로 Class가 바뀌어도 method 이름을 걱정할 필요는 없음
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
		// Connection c = getConnection();
		// Connection c = this.simpleConnectionMaker.makeNewConnection();
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









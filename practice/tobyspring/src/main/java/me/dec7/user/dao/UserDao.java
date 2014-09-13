package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import me.dec7.user.domain.User;

import org.springframework.dao.EmptyResultDataAccessException;

/*
 * 3.5, template, callback
 * 
 * UserDao, StatementStrategy, JdbcContext를 이용해 만든 코드는
 * 전략 패턴이 적용됨. 복잡하지만 바뀌지 않는 일정한 패턴을 갖는 작업흐름이 존재
 * 
 * template/callback pattern / spring에서 부르는 이름
 *  - 이처럼 기본적인 전략 패턴의 기본 구조에 익명 내부 클래스를 활용한 방식
 *  - tempate
 *  	- 전략 패턴의 context
 *  - callback
 *  	- 익명 내부 클래스로 만들어지는 오브젝트
 *  	- 실행되는 것을 목적으로 다른 오브젝트에서 메소드에 전달되는 오브젝트를 말함
 *  	- parameter로 전달되지만 목적이 참조가 아닌 특정 로직을 담은 메소드를 실행시키는 것
 *  	- 자바에서는 메소드 자체를 patermeter로 전달할 수 없으므로
 *  	  메소드가 담긴 오브젝트를 전달
 *  	   --> functional object 라 부름
 */

/*
 * 템플릿: 고정된 작업 흐름을 가진 코드를 재사용한다는 의미
 * 콜백:  템플릿 안에서 호출되는 것을 목적으로 만들어지 오브젝트
 * 
 * 템플릿/콜백
 * 특징
 *  - 단일 메소드 인터페이스 사용 (작업 흐름 중 특정 기능을 위해)
 *  - 콜백은 일반적으로 하나의 메소드를 가진 인터페이스를 구현한 익명 내부 클래스로 됨
 *  
 * 작업흐름
 *  - Client의 역할
 *  	- 템플릿 안에서 실행될 로직을 담은 콜백 오브젝트를 생성
 *  	- 콜백이 참조할 정보를 제공
 *  	- 콜백은 Client가 템플릿 메소드를 호출할 때 파라미터로 전딸
 *  - 템플릿 역할
 *  	- 정해진 작업흐름을 따라 진행
 *  	- 내부 정보를 가지고 콜백 오브젝트 메소드 호출 
 * 		- 콜백은 client 메소드에 있는 정보 + 템플릿의 참조 정보를 이용해 작업 수행
 */
public class UserDao {
	private DataSource dataSource;
	private JdbcContext jdbcContext;

	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		
		this.jdbcContext = new JdbcContext();
		this.jdbcContext.setDataSource(dataSource);
	}

	public void add(final User user) throws SQLException {
		this.jdbcContext.workWithStatementStrategy(new StatementStrategy() {

			@Override
			public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
				PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
				
				ps.setString(1, user.getId());
				ps.setString(2, user.getName());
				ps.setString(3, user.getPassword());
				
				return ps;
			}	
		});
	}

	public User get(String id) throws SQLException, ClassNotFoundException {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			c = this.dataSource.getConnection();
			ps = c.prepareStatement("select * from users where id = ?");
			ps.setString(1, id);
			
			rs = ps.executeQuery();
			
			// user를 null로 초기화
			User user = null;
			
			// 결과 존재시 데이터를 담음
			if (rs.next()) {
				user = new User();
				user.setId(rs.getString("id"));
				user.setName(rs.getString("name"));
				user.setPassword(rs.getString("password"));			
			}
			
			// user결과가 null인 경우 exception을 던짐
			if (user == null) {
				throw new EmptyResultDataAccessException(1);
			}
			
			return user;
			
		} catch (SQLException sqle) {
			throw sqle;
			
		} finally {
			if (rs != null) {
				try {
					rs.close();
					
				} catch (SQLException sqle) { }
			}
			
			if (ps != null) {
				try {
					ps.close();
					
				} catch (SQLException sqle) { }
			}
			
			if (c != null) {
				try {
					c.close();
					
				} catch (SQLException sqle) { }
			}
		}		
	}
	
	/*
	 * 콜백의 분리와 재활용
	 *  - 변하는 부분은 sql query일 뿐
	 */
	public void deleteAll() throws SQLException {
		/*
		this.jdbcContext.workWithStatementStrategy(new StatementStrategy() {

			@Override
			public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
				PreparedStatement ps = c.prepareStatement("delete from users");
				
				return ps;
			}
		
		});
		*/
		// executeSql("delete from users");
		// jdbcContext class로 이동 후
		this.jdbcContext.executeSql("delete from users");
	}
	
	/*
	 * 변하지 않는 문장은 메소드 추출
	 *  
	 * 콜백과 템플릿의 결합
	 *  - 재활용 가능하므로 Dao가 공유할 수 있는 템플릿 클래스로 옮김

	private void executeSql(final String query) throws SQLException {
		this.jdbcContext.workWithStatementStrategy(new StatementStrategy() {

			@Override
			public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
				PreparedStatement ps = c.prepareStatement(query);
				
				return ps;
			}
		
		});
	}
	*/
	
	

	public int getCount() throws SQLException, ClassNotFoundException {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			c = this.dataSource.getConnection();
			ps = c.prepareStatement("select count(*) from users");
			
			// ResultSet도 SQLException이 발생할 수 있는 코드
			rs = ps.executeQuery();
			rs.next();
			
			return rs.getInt(1);
			
		} catch (SQLException sqle) {
			throw sqle;
			
		} finally {
			if (rs != null) {
				try {
					rs.close();
					
				} catch (SQLException sqle) { }
			}
			
			if (ps != null) {
				try {
					ps.close();
					
				} catch (SQLException sqle) { }
			}
			
			if (c != null) {
				try {
					c.close();
					
				} catch (SQLException sqle) { }
			}
		}
	}
}














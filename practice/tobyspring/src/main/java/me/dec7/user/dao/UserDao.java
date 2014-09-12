package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import me.dec7.user.domain.User;

import org.springframework.dao.EmptyResultDataAccessException;

/*
 * 3장, 템플릿
 * 
 * 개방 폐쇄 원칙
 *  - 확장 자유롭고
 *  - 변경 닫혀있고
 * 
 * 템플릿
 *  - 바뀌는 성질이 다른 코드 중 변경이 일어나지 않고 일정한 패턴으로 유지되는 부분을 독립시켜 효과적 사용방식
 */

/*
 * UserDao 문제점
 *  1. 예외처리 부재
 *  	- jdbc 코드는 제한한 db connection을 사용하여 예외발생시 반드시 리소스 반환
 */


/*
 * 3.2, 변하는 것 / 변하지 않는 것
 * 
 * JDBC try, catch 문제점
 *  - 동일한 패턴이 method마다 반복됨
 *  - 실수로 큰 에러 발생할 수도 / 폭탄같은 코드
 *  
 * 해결
 *  - 3.2.2, 분리와 재사용을 위한 디자인 패턴 적용
 * 
 */
//public abstract class UserDao {
public class UserDao {
	
	/*
	 * 리소스 반환, close()
	 *  - connection과 preparedStatement는 보통 pool 방식으로 운영
	 *  - 미리 정해진 풀 안에 제한된 수의 리소스를 만들어 놓고 필요할 때 가져다 쓴 뒤 다시 풀에 넣는 방식
	 * 
	 */
	
	private DataSource dataSource;
	
	/*
	 * spring bean
	 * 
	 * JdbcContext DI
	 *  - DI 개념에 충실히 따르면, interface를 사이에 두고 runtime시 다이나믹하게 주입하는 것
	 *  
	 * 1. JdbcContext는 spring container의 singleton registry에서 관리하는 singleton bean이 될 수 있음 
	 * 		- 상태 정보를 가지지 않음
	 * 2. JdbcContext는 DI를 통해 다른 bean에 의존
	 * 		- JdbcContext는 dataSource 프로퍼티를 통해 DataSource 오브젝트를 주입받음
	 * 		  DI를 위해서 주입되는 오브젝트 양쪽 모두 bean으로 등록되어야 함
	 * 		  spring이 관리하는 IoC 대상이어야 DI 참여 가능
	 * 
	 * interface를 사용하지 않은 이유
	 *  - UserDao와 JdbcContext가 매우 긴밀한 관계라는 의미  --> 이런경우는 수동DI를 할 수도 있음
	 *  - 만약 ORM (JPA, 하이버네이트)을 사용시 JdbcContext도 통째로 바뀌어야 함
	 * 
	 */
	/*
	 * 수동 DI
 	 *  - Dao마다 한개씩 만들면 부담 적음
	 *  - 자주 만들어지는 성격도 아니므로 GC의 부담도 없음
	 *  - 내부에 저장하는 상태정보도 없음
	 *  
	 * 문제점
	 *  - 대신 초기화할 대상이 필요
	 *  - 다른 bean의 interface를 간접적으로 의존
	 *  
	 * 해결방안
	 *  - JdbcContext의 제어권, 생성, 관리를 UserDao에 맡기기
	 */
	private JdbcContext jdbcContext;

	/*
	 * 수동 DI방식으로 변경되어
	 * setter method는 필요 없음
	 
	public void setJdbcContext(JdbcContext jdbcContext) {
		this.jdbcContext = jdbcContext;
	}
	*/
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		
		// JdbcContext의 수동 DI
		// JdbcContext 생성
		this.jdbcContext = new JdbcContext();
		// 의존 오브젝트 주입
		this.jdbcContext.setDataSource(dataSource);
		
		/*
		 * JdbcContext 수동 DI의 장점
		 *  - interface를 두지 않아도 될만큼 긴밀한 관계를 가지는 경우 굳이 어색하게 bean으로 분리하지 않고
		 *    내부에서 직접만들어 사용
		 *  - 그러면서도 다른 오브젝트에 대한 DI를 적용할 수 있다는 것
		 *  - 둘의 관계가 외부에 노출되지 않는다는 것
		 *  
		 * 단점
		 *  - JdbcContext를 여러 오브젝트가 사용하더라도 Singleton으로 만들 수 없음
		 *  - DI를 위한 부가적인 코드가 필요
		 * 
		 */
	}

	//	public void add(User user) throws SQLException {
	public void add(final User user) throws SQLException {
		/*
		Connection c = null;
		PreparedStatement ps = null;
		
		try {
			c = this.dataSource.getConnection();
			ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
			
			ps.setString(1, user.getId());
			ps.setString(2, user.getName());
			ps.setString(3, user.getPassword());
			
			ps.executeUpdate();			
			
		} catch (SQLException sqle) {
			throw sqle;
			
		} finally {
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
		*/
		
		/*
		 * 3.3.2, 문제점 발생
		 * 
		 * 문제점
		 *  1. Dao method마다 새로운 StatementStrategy를 구현해야함
		 *  	- class 개수가 많아짐, runtime시 DI할 수 있다는 장점을 제외하면 .. template method pattern보다 나을게 없음
		 *  
		 *  2. Dao method에서 StatementStrategy에 전달할 정보
		 *  	- User외 다른 부가정보는, 오브젝트를 전달받는 생성자와 instacne 변수를 계속 만들어줘야함,
		 *  
		 * 해결방법
		 *  1. 로컬 클래스
		 *  	- UserDao의 내부 클래스로 정의
		 */
		
		/*
		class InnerAddStatement implements StatementStrategy {
			/*
			 * p230, 중첩 클래스의 종류
			 *  - 다른 클래스의 내부에 정의되는 클래스
			 *  
			 *  1. static class
			 *  	- 독립적으로 오브젝트로 만들어질 수 있음
			 *  
			 *  2. inner class
			 *  	- 자신의 정의된 클래스 오브젝트 안에서만 만들어질 수 있음
			 *  	1) member inner class
			 *  		- 멤버 필드처럼, 오브젝트 레벨에서 정의됨
			 * 		2) local class
			 * 			- 메소드 레벨에서 정의
			 *  	3) anonymous inner class
			 *  		- 이름을 갖지 않는 클래스
			 *  		- 범위는 선언된 위치에 따라 다름
			 *
			
			/*
			 *내부 클래스이므로 자신이 선언된 곳의 정보에 접근 가능
			 *생성자를 통해 정보를 전달할 필요가 없음
			 *다만, 내부 클래스에서 외부 변수를 사용하려면 final로 선언해야함
			 *
			User user;
			public InnerAddStatement(User user) {
				this.user = user;
			}
			*

			@Override
			public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
				PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
				
				ps.setString(1, user.getId());
				ps.setString(2, user.getName());
				ps.setString(3, user.getPassword());
				
				return ps;
			}
		}*/
		
//		StatementStrategy stmtStrategy = new InnerAddStatement(user);		
//		StatementStrategy stmtStrategy = new InnerAddStatement();
		/*
		 * p231, 익명 내부 클래스
		 *  - 내부 클래스를 조금 더 간결하게 바꿀 수 있음
		 */
		
		// jdbcContext로 사용
		// jdbcContextWithStatementStrategy(new StatementStrategy() {
		
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
	
	public void deleteAll() throws SQLException {
		// 선정한 전략 클래스 오브젝트 생성
//		StatementStrategy stmtStrategy = new DeleteAllStatement();
		// 컨텍스트 호출, 전략 오브젝트 전달
//		jdbcContextWithStatementStrategy(stmtStrategy);
		
		// jdbcContext로 사용
		// jdbcContextWithStatementStrategy(new StatementStrategy() {
		
		this.jdbcContext.workWithStatementStrategy(new StatementStrategy() {

			@Override
			public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
				PreparedStatement ps = c.prepareStatement("delete from users");
				
				return ps;
			}
		
		});
	}
	
	

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
	
	/*
	 * 변하는 부분의 메소드 추출이 올바른가?
	 *  - 메소드 추출한 부분을 다른 곳에서 재사용할 수 있어야 하지만 sql query는 코드마다 다른 부분이므로 재사용 어려워 보임
	 
	private PreparedStatement makeStatement(Connection c) throws SQLException {
		PreparedStatement ps;
		ps = c.prepareStatement("delete from users");
		
		return ps;
	}
	*/
	
//	방법 2 -1 실패
//	protected abstract PreparedStatement makeStatement(Connection c) throws SQLException;
	
	/*
	 * 방법 2-3
	 * 메소드로 분리한 JDBC context
	 * 
	 * client가 context를 호출시 전략 interface를 넘겨줄 수 있도록 parameter로 지정
	 */
	
	/*
	 * 3.4.1, jdbcContext 분리 
	 * 
	 * 전략 패턴
	 *  - client: UserDao method
	 *  - context: jdbcContextWithStatementStrategy()
	 *  
	 * 따라서 context는 UserDao뿐 아니라 다른 곳에서도 사용 가능하므로 분리하는 것이 옮음
	 * 
	 * me.dec7.user.dao.JdbcContext로 이동
	 
	public void jdbcContextWithStatementStrategy(StatementStrategy stmtStrategy) throws SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		
		try {
			c = this.dataSource.getConnection();
		
			ps = stmtStrategy.makePrepareStatement(c);
			
			//=========================================================
			ps.executeUpdate();

		} catch (SQLException sqle) {
			throw sqle;
		
		} finally {
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
	*/
}














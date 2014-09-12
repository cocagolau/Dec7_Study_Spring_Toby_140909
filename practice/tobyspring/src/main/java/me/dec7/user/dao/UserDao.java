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

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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
		jdbcContextWithStatementStrategy(new StatementStrategy() {

			@Override
			public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
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
	 * public void deleteAll() throws SQLException {
	 * 
	 * 방법 2-3에서
	 * 변경의 규모가 커져서 method이름을 변경
	 */
	
	public void oldDeleteAll() throws SQLException {
		/*
		 * 	중복 --> 메소드 수출
		 *  변하는 것, 변하지 않는 것
		 *  
		 *  방법1. 변하는 부분을 메소드로 추출
		 *  방법2. 변하지 않는 부분을 메소드로 추출 --> template method pattern
		 */
		
		//------ 변하지 않는 부분 -----------------------------------------
		Connection c = null;
		PreparedStatement ps = null;
		
		/*
		 *  이곳에서 예외 발생시 close() method는 수행하지 못하고 종료
		 *  
		 *  어떤 상황에서든 리소스를 반환하도록 try catch finally 구문 사용
		 *  예외가 발생할 가능성이 있는 코드를 모두 try 블록으로 묶음
		 */
		try {
			c = this.dataSource.getConnection();
			
			//======= 변하는 부분 ========================================
			// ps = c.prepareStatement("delete from users");
			
			/*
			 * 
			 * 방법 1적용
			 *  - 변하는 부분을 메소드로 추출
			
			// ps = makeStatement(c);
			 * 
			 * 방법 2 -1 적용
			 *  - template method 적용
			 *  - makeStatement를 abstract method로 선언
			 *  	- 상속을 통해 기능을 확장하여 사용하는 패턴
			 *  	- 변하지 않는 부분을 super class에 두고
			 *  	  변하는 부분을 sub class에서 재정의 하여 사용
			 *  --> 실패: UserDaoDeleteAll class 참고
			 *  
			 * 방법 2 -2 적용
			 *  - 전략 패턴의 사용
			 *  - 문제점
			 *  	- 전략패턴는 context는 유지하면서 필요에 따라 전략을 바꿀 수 있다는 것이 의미
			 *  	- 하지만, context 내에서 이미 구체적인 전략 클래스가 사용되도록 고정되어 구조적인 문제를 가짐
			 *  
			 * 방법 2 -3
			 *  - 2-2의 문제점 개선
			 *  - context의 사용 전 client가 전략을 선택
			 *    client가 구체적인 전략을 선택 후 context에 전달
			 *  - 해결법
			 *  	- 전략 오브젝트의 생성과 context로 전달을 담당하는 책임을 분리
			 *  	 --> 일반화: DI
			 *  - 개선사항
			 *  	- JDBC try/catch/finally 코드를 client 코드인 StatementStrategy를 만드는 부분에서 분리
			 *  	- context 부분을 별로 메소드로 분리
			 *  		- client가 전달할 전략 interface를 parameter로 지정 
			 */
			
			StatementStrategy strategy = new DeleteAllStatement();
			ps = strategy.makePrepareStatement(c);
			
			//=========================================================
			ps.executeUpdate();
			
		/*
		 *  예외 발생시 부가적인 작업을 할 수 있는 catch 구문
		 *  현재는 예외를 밖으로 던지는 일을 함
		 */
		} catch (SQLException sqle) {
			throw sqle;
		
		/*
		 * finally 구문은
		 * 예와가 발생하든 아니든 무조건 실행되는 구간 
		 */
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
		//------------------------------------------------------------
	}
	/*
	 * 방법 2-3
	 * oldDeleteAll --> deleteAll()
	 * 
	 * client와 context를 완벽히 분리하지 않았지만, 의존관계와 책임은 분리됨
	 */
	
	/*
	 * 마이크로 DI
	 * 	- DI의 장점을 단순화하여
	 *    IoC container 도움없이 코드내에서 적용한 경우
	 *  - 코드에 의한 DI라는 의미로 '수동DI'라고도 함 
	 * 
	 *  - DI 는 제 3자의 도움으로 두 오브젝트의 사이가 유연한 관계가 되도록 설정한다는 것이 중요
	 *  - 그래서, DI는 다양한 형태로 적용될 수 있음
	 *  
	 *  - 일반적으로 DI
	 *  	- 의존관계에 있는 두 개의 오브젝트
	 *  	- 관계를 다이나믹하게 설정하는 오브젝트 팩토리 (DI Container)
	 *  	- 이를 사용하는 Client
	 *  
	 *  - 하지만 상황에 따라 변경될 수도 있음
	 *  - DI가 method사이의 작은 단위의 코드에서 일어날 수도 있음
	 */
	public void deleteAll() throws SQLException {
		// 선정한 전략 클래스 오브젝트 생성
//		StatementStrategy stmtStrategy = new DeleteAllStatement();
		// 컨텍스트 호출, 전략 오브젝트 전달
//		jdbcContextWithStatementStrategy(stmtStrategy);
		
		jdbcContextWithStatementStrategy(new StatementStrategy() {

			@Override
			public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
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
}














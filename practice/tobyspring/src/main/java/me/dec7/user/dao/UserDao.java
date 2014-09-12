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

	public void add(User user) throws SQLException {
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
			ps = c.prepareStatement("delete from users");
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
}









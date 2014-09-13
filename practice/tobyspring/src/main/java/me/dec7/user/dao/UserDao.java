package me.dec7.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import me.dec7.user.domain.User;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/*
 * 3.6, spring JdbcTemplate 적용
 * 
 * 현재 UserDao는 DataSource를 DI받아서
 * JdbcContext에 주입해 템플릿 오브젝트로 만들어 사용
 */

/*
 * 3.6.5, 재사용 가능한 콜백의 분리
 * 
 * tempate/callback pattern을 사용하면서 코드의 관심사가 분명해지고 깔끔해짐
 * 마무리 작업
 *  1. DI를 위한 코드 정리
 *   - 필요없어진 DataSource 제거
 *   
 *  2. 중복제거
 *   - get(), getAll()을 보면 RowMapper를 동일하게 사용 중
 *   - 두번만 사용되었지만 앞으로 계속 사용될 부분이므로 메소드 분리하는것이 좋음
 * 
 */

/*
 * 최종
 * 
 * UserDao는 User정보를 DB에 조작하는 핵심적인 방법만 담김
 *  - User 오브젝트, USER 테이블 사이, SQL문장 등 DB와 커뮤니케이션에 대한 최적의 코드
 *  - 테이블에 대한 정보가 바뀌면 UserDao의 거의 모든 코드가 바뀜
 *  	- 응집도 높음
 *  - JDBC api 사용, 예외처리, 리소스 반납, db연결에 대한 책임은 JdbcTemplate이 있음
 *  - jdbcTemplate이 변경되더라도 UserDao에는 아무런 영향을 주지 않음
 *  	- 책임이 다른 코드와 낮은 결합도
 *  - JdbcTemplate은 template/callback에 대한 강한 결합
 *  
 *  - JdbcTemplate을 독립적인 bean으로 등록, JdbcOperations interface를 통해 DI사용 가능
 *  	- JdbcTemplate은 DAO안에서 직접 만들어 사용하는 것에 spring의 관례
 *  
 * 그래도 개선사항
 *  1. userMapper
 *  	- 인스턴스 변수 선언, 만들어지면 변경 안됨
 *  	--> DI용 property로 선언 가능
 *  2. SQL문장
 *  	- UserDao코드가 아닌 외부 리소스에 담고 읽어와 사용하도록 하기
 *  
 * 요즘엔 SimpleJdbcTemplate을 사용함
 */
public class UserDao {
//	private DataSource dataSource;
//	private JdbcContext jdbcContext;
	private JdbcTemplate jdbcTemplate;
	
	// 중복으로 분리된 RowMapper()
	private RowMapper<User> userMapper = new RowMapper<User> () {

		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("password"));
			
			return user;
		}
		
	};

	
	public void setDataSource(DataSource dataSource) {
//		this.dataSource = dataSource;
		
		/*
		this.jdbcContext = new JdbcContext();
		this.jdbcContext.setDataSource(dataSource);
		*/
		// JdbcTemplate은 생성자 파라미터로 DataSource를 주입하면 됨
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void deleteAll() throws SQLException {

		//this.jdbcContext.executeSql("delete from users");
		
		/*
		this.jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				
				return con.prepareStatement("delete from users");
			}
			
		});
		
		 * executeSql() 처럼 update()도 내장 callback이 존재함
		 */
		
		this.jdbcTemplate.update("delete from users");
		
	}
	
	

	public void add(final User user) throws SQLException {
		/*
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
		*/
		
		/*
		 * update() 메소드는
		 * 
		 * PreparedStatement로 sql을 만들고
		 * 함께 제공하는 파라미터를 순서대로 바인딩 해주는 기능을 가짐
		 */
		
		/*
		this.jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				
				PreparedStatement ps = con.prepareStatement("insert into users(id, name, password) values(?,?,?)");
				ps.setString(1, user.getId());
				ps.setString(2, user.getName());
				ps.setString(3, user.getPassword());
				
				return ps;
			}
			
		});
		
		아래처럼 줄일 수 있음
		*/
		
		this.jdbcTemplate.update("insert into users(id, name, password) values(?,?,?)", user.getId(), user.getName(), user.getPassword());
	}

	public User get(String id) throws SQLException, ClassNotFoundException {
		/*
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
		*/
		
		/*
		 * get() 메소드는 지금까지 중 가장 복잡
		 * 
		 * 요구사항
		 *  1. SQL 바인딩이 필요한 치환자
		 *  2. 결과가 User 오브젝트를 만드는 복잡한 작업
		 *  	- ResultSet의 결과를 User 오브젝트에 넣어야함
		 *  	--> RowMapper 사용
		 *  
		 *  jdbcTemplate의 queryForObject() 사용
		 * 
		 */
		
		return this.jdbcTemplate.queryForObject(
				// praparedStatement를 만들기 위한 sql
				"select * from users where id = ?",
				
				// sql에 바인딩할 파라미터 값, 가변인자 대신 배열을 사용
				// 뒷 부분에 다른 parameter가 사용되므로 가변인자를 사용할 수 없음
				new Object[] {id},
				
				// ResultSet의 한 Row 결과를 Object롤 mapping 해주는 콜백
				/*
				 * RowMapper 중복 제거
				new RowMapper<User>() {
					
					*
					 * queryForObject는
					 * sql을 실행시 한 개의 row만 얻을 것이라 기대함.
					 * 그래서 ResultSet의 next()를 실행한 뒤 RowMapper 콜백을 호출하므로
					 * rs.next()를 호출할 필요가 없다.
					 *
					@Override
					public User mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						User user = new User();
						user.setId(rs.getString("id"));
						user.setName(rs.getString("name"));
						user.setPassword(rs.getString("password"));
						
						return user;
					}
				}
				*/
				this.userMapper
				
				/*
				 * 기존에 조회결과가 없을 때는 
				 * EmptyResutlDataAccessException을 던지도록 만듦.
				 * 
				 * spring, queryForObject()를 사용시
				 * 받은 row의 개수가 1개가 아닌 경우 자동적으로
				 * EmptyResultDataAccessException을 던지므로 신경 안써도 됨
				 */
		);
				
	}
	

	
	/*
	 * getCount()는 ResultSet으로 결과 값을 가져오는 코드
	 * 
	 * jdbcTemplate.query()사용 / 아래의 parameter를 사용
	 *  - PreparedStatementCreator 콜백
	 *  - ResultSetExtractor 콜백
	 *  	- PreparedStatement 쿼리를 실해해서 얻는 ResultSet을 전달받는 콜백
	 *  	- ResultSet을 이용해 원하는 값을 추출해서 템플릿에 전달
	 * 
	 */
	public int getCount() {
		/*
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
		*/
		
		/*
		return this.jdbcTemplate.query(
				new PreparedStatementCreator() {

					@Override
					public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
						
						return con.prepareStatement("select count(*) from users"); 
					}
					
				},
				// ResultSetExtractor는 generics 타임 파라미터를 받음
				new ResultSetExtractor<Integer>() {

					@Override
					public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
						rs.next();
						
						return rs.getInt(1);
					}
					
				}
			);
		
		다음과 같은 코드를
		jdbcTemplate의 내장 콜백인 queryForInt()을 사용해 처리할 수 있음
		 - Integer 타입 결과를 가져올 수 있는 SQL 문장만 전달하면 됨
		*/
		
		return this.jdbcTemplate.queryForInt("select count(*) from users");
	}

	public List<User> getAll() {
		
		/*
		 * query() 템플릿은 
		 * sql 쿼리를 실행 후 DB에서 가져오는 row 수만큼 호출됨
		 */
		
		return this.jdbcTemplate.query(
				"select * from users order by id",
				/*
				 * RowMapper 중복 제거
				new RowMapper<User>() {
					
					public User mapRow(ResultSet rs, int rowNum) throws SQLException {
						User user = new User();
						user.setId(rs.getString("id"));
						user.setName(rs.getString("name"));
						user.setPassword(rs.getString("password"));
						
						return user;
						
					}
				}
					*/
				this.userMapper);
	}
	
}














package me.dec7.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;
import me.dec7.user.sqlservice.SqlService;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


/*
 * SQL과 DAO 분리
 *  - 데이터 엑세스 로직이 바뀌지 않더라도 SQL 문장이 바뀔 수 있음
 * 		- 테이블/필드이름이 바뀌거나,테이블 분리, sql에 부가적인 기능 추가
 * 
 * 1. XML설정을 통한 분리
 *  -sql을 xml파일로 분리 후 bean에 값을 주입
 *  1) 개별 SQL property 방식
 *  	- setter method 설정
 *   	- spring 설정파일에 sql을 DI
 *    
 *  2) SQL 맵 프로퍼티 방식
 *  	- SQL을 하나의 컬렉션에 담아두는 방법
 *  		- sql이 많아지면 DI하기 어려움
 *  		- key를 이용해 sql을 가져올 수 있음
 *  
 */
public class UserDaoJdbc implements UserDao {
	
	/*
	 * sql이 많아지면 계속 DI해야함.
	 * map을 이용한 방법으로 변경
	private String sqlAdd;
	 */
	private Map<String, String> sqlMap;
	private SqlService sqlService;
	private JdbcTemplate jdbcTemplate;
	private RowMapper<User> userMapper = new RowMapper<User> () {

		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("password"));
			user.setEmail(rs.getString("email"));
			user.setLevel(Level.valueOf(rs.getInt("level")));
			user.setLogin(rs.getInt("login"));
			user.setRecommend(rs.getInt("recommend"));
			
			return user;
		}
		
	};
	
	/*
	public void setSqlAdd(String sqlAdd) {
		this.sqlAdd = sqlAdd;
	}
	 */
	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}
	

	/*
	public void setSqlAdd(String sqlAdd) {
		this.sqlAdd = sqlAdd;
	}
	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}
	*/
	// SqlService를 DI받을 수 있도록 setter method 정의
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see me.dec7.user.dao.UserDao#deleteAll()
	 */
	@Override
	public void deleteAll() {
		this.jdbcTemplate.update(
				//"delete from users"
				//this.sqlMap.get("deleteAll")
				/*
				 * sqlService는 모든 DAO의 Service bean에서 사용할 것이므로
				 * dao별로 이름이 중복되지 않도록 해야함
				 */
				this.sqlService.getSql("userDeleteAll")
				);
	}
	
	/* (non-Javadoc)
	 * @see me.dec7.user.dao.UserDao#add(me.dec7.user.domain.User)
	 */
	@Override
	public void add(User user) throws DuplicateKeyException {
		try {
			this.jdbcTemplate.update(
					//"insert into users(id, name, password, email, level, login, recommend) values(?,?,?,?,?,?,?)",
					//this.sqlAdd,
					//this.sqlMap.get("add"),
					this.sqlService.getSql("userAdd"),
					user.getId(),
					user.getName(),
					user.getPassword(),
					user.getEmail(),
					
					user.getLevel().intValue(),
					user.getLogin(),
					user.getRecommend()
					);
		} catch (DuplicateKeyException e) {
			// log 구성
			// 예외 전환
//			throw new DuplicateUserIdException(e);
			throw e;
		}
	}
	

	/* (non-Javadoc)
	 * @see me.dec7.user.dao.UserDao#get(java.lang.String)
	 */
	@Override
	public User get(String id) {
		
		return this.jdbcTemplate.queryForObject(
				//"select * from users where id = ?",
				//this.sqlMap.get("get"),
				this.sqlService.getSql("userGet"),
				new Object[] {id},
				this.userMapper
		);
				
	}

	/* (non-Javadoc)
	 * @see me.dec7.user.dao.UserDao#getCount()
	 */
	@Override
	public int getCount() {
		
		return this.jdbcTemplate.queryForInt(
				//"select count(*) from users"
				//this.sqlMap.get("getCount")
				this.sqlService.getSql("userGetCount")
				);
	}

	/* (non-Javadoc)
	 * @see me.dec7.user.dao.UserDao#getAll()
	 */
	@Override
	public List<User> getAll() {
	
		return this.jdbcTemplate.query(
				//"select * from users order by id",
				//this.sqlMap.get("getAll"),
				this.sqlService.getSql("userGetAll"),
				this.userMapper);
	}

	@Override
	public void update(User user) {		
		this.jdbcTemplate.update(
				//"update users set name=?, password=?, level=?, login=?, recommend=? where id=?",
				//this.sqlMap.get("update"),
				this.sqlService.getSql("userUpdate"),
				user.getName(), user.getPassword(), user.getLevel().intValue(), user.getLogin(), user.getRecommend(), user.getId());	
	}
	
}














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

public class UserDaoJdbc implements UserDao {
	
	/*
	 * sql이 많아지면 계속 DI해야함.
	 * map을 이용한 방법으로 변경
	private String sqlAdd;
	 */
	private Map<String, String> sqlMap;
	private SqlService sqlService;
	
	/*
	public void setSqlAdd(String sqlAdd) {
		this.sqlAdd = sqlAdd;
	}
	*/
	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}
	
	// SqlService를 DI받을 수 있도록 setter method 정의
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}
	
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
					
					/*
					 * Level 타입은 enum 오브젝트이므로 DB에 들어갈 수 없음
					 *  - DB에 저장될 수 있도록 intValue()메소드를 사용하여 전환
					 * 
					 * 반대로 DB에서 가져온 값은 int타입이므로 
					 * valueOf()를 사용하여 Level타입의 enum오브젝트로 변환
					 */
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














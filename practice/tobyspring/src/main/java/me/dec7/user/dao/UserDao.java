package me.dec7.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import me.dec7.user.domain.User;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/*
 * 4.2
 * 
 * JDBC 한계
 *  - JDBC는 DB에 접근하는 방식을 추상화된 API로 제공하여 DB에 관계없이 일관된 방법으로 프로그래밍 가능
 *  1. 비표준 SQL
 *  	- Dao가 특정 DB에 종속적인 코드가 될 수도
 *  2. SQLException
 *  	- DB 예외의 원인은 다양하지만 모든 예외는 SQLException으로 처리됨
 *  	- 에러 정보를 확인하기 위해 getErrorCode()를 확인해야함. 하지만 DB마다 그 코드가 다 다름
 *  		ex) if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY) { ...
 *  
 * 따라서 
 *  호환성 없는 에러코드
 *  표준을 따르지 않는 상태코드 가진
 *  SQLException만으로 DB에 독립적인 유연한 코드를 작성하는 건 불가능에 가까움
 *  
 *  
 *  DB에러 코드 맵핑을 통한 전환 
 *   - Spring은 SQLException을 대체할 수 있는 Runtime Exception을 정의
 *   	- DataAccessException
 *   		- subclass
 *   		- BadSqlGrammerException
 *   			- sql 문법
 *   		- DataAccessResourceFailureException
 *   			- db connection
 *   		- DataIntegrityViolationException
 *   			- 제약조건 위반, 일관성 지키지 못했을 때
 *   		- DuplicatedKeyException
 *   			- 중복 키
 *   		- etc...
 *   - 문제
 *   	- DB마다 error code가 다 다름
 */
public class UserDao {
	private JdbcTemplate jdbcTemplate;
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
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	/*
	 * public void deleteAll() throws SQLExceptio {
	 * 
	 * JdbcContext에서 JdbcTemplate으로 바꾸면서
	 * SQLException 예외를 던지지 않음
	
	try {
		...
	} catch (SQLException e) {
		예외를 잡고 아무것도 하지 않는 것은
		정상적인 상황처럼 다음 라인으로 넘어가겠다는 분명한 의도가 있지 않는한
		연습 중에도 절대 하지 말아야 하는 코드
		
		System.out.println(e);
		e.printStackTrace();
		
		예외발생시 화면에 출력하는 것도 나쁜 코드
		 - 다른 로그에 묻혀버리기 쉽상
		 - System.exit(1); 이것이 차라리 나은 코드
	}
	
	   예외를 잡은 뒤 아무 행동하지 않는 것은 매우 위험한 일
	   예외가 발생하는 것보다 더 나쁜 일
	   
	   원칙
	    - 모든 예외는 적절하게 복구되거나
	    - 작업을 중단시키고 운영자에게 통보
	 */
	
	/*
	 * throws
	 *  - 무의미, 무책임
	 *  - 예외에서 의미있는 정보를 얻을 수 없음
	 */
	public void deleteAll() {
		this.jdbcTemplate.update("delete from users");
	}
	
	
	/*
	 * add() 메소드 예외처리
	 * 
	 * 사용자 아이디 중복시
	 *  - DuplicateUserIdException 생성 / RuntimeException 상속
	 *  	- 필요하면 언제든 잡아서 처리할 수 있도록 별도의 예외로 정의
	 *  	- 하지만 필요없다면 신경쓰지 않도록 RuntimeException으로 상속시킴
	 *  	- DuplicateUserIdException이 중첩 예외를 만들 수 있도록 생성자 추가
	 *  
	 * 
	public void add(User user) throws DuplicateUserIdException {
		try {
			
			// JDBC 코드, SQLException을 던지는 메소드를 호출하는 코드
			
		} catch (SQLException e) {
		
			* 특별한 의미를 지니는 DuplicateUserIdException 외
			* 시스템 예외에 해당하는 SQLException은 언체크 예외가 됨
			* 따라서
			* 	SQLException은 밖으로 던질 필요가 없고
			* 	add()를 사용하는 쪽에서 ID중복 처리 하고 싶을 때 사용할 수 있도록
			* 	DuplicateUserIdException만 밖으로 던짐
 
			if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY) {
				// 예외 전환
				throw new DuplicateUserIdException(e);
			}
			else {
				// 예외 포장
				throw new RuntimeException(e);
			}
		}
		
		
		런타임 예외를 일반화해서 사용방식의 장점
		 - RuntimeException이므로 사용에 주의 기울일 필요없음
		 - 컴파일러가 예외처리를 강제하지 않음
	}
	*/
	
	/*
	 * SQLException??
	 * 
	 * 과연 복구가 가능한 예외인가?  --> 복구 불가 --> 빠른 담당자 전달
	 *  - 프로그램 오류
	 *  - 개발자의 부주의 
	 *  - 통제할 수 없는 외부상황
	 *  	- SQL문법 오류, 제약조건 위반, DB서버 다운, 네트워크 불안정, DB커넥션 풀 꽉참..
	 *  
	 * 따라서
	 *  - SQLException을 밖으로 던지는 것이 아닌
	 *    가능한 빨리 uncheck/runtime 예외로 전환해야함
	 *    
	 * Spring은?
	 *  - JdbcTemplate은 위 예외처리 전략을 사용 중
	 *  - 모든 SQLException을 Runtime예외인 DataAccessException으로 포장해줌
	 *  - Runtime예외 이므로 잡거나, 던질 의무는 사라짐
	 * 
	 */
	
	/*
	 * JdbcTemplate은 
	 * Runtime예외인 DataAccessException 계층구조의 예외로 포장
	 * 중복인경우 단지 DuplicateKeyException으로 처리하면 됨
	 * 
	 */
	/*
	 * 예외 무시
	public void add(User user) {
		this.jdbcTemplate.update("insert into users(id, name, password) values(?,?,?)", user.getId(), user.getName(), user.getPassword());
	}
	*/
	/*
	 * JdbcTemplate이 제공하는 예외 전환 기능 사용
	public void add(User user) throws DuplicateKeyException {
		try {
			this.jdbcTemplate.update("insert into users(id, name, password) values(?,?,?)", user.getId(), user.getName(), user.getPassword());
		} catch (DuplicateKeyException e) {
			// log 구성
			throw e;
		}
	}
	*/
	public void add(User user) throws DuplicateUserIdException {
		try {
			this.jdbcTemplate.update("insert into users(id, name, password) values(?,?,?)", user.getId(), user.getName(), user.getPassword());
		} catch (DuplicateKeyException e) {
			// log 구성
			// 예외 전환
			throw new DuplicateUserIdException(e);
		}
	}
	

	public User get(String id) {
		
		return this.jdbcTemplate.queryForObject(
				"select * from users where id = ?",
				new Object[] {id},
				this.userMapper
		);
				
	}

	public int getCount() {
		
		return this.jdbcTemplate.queryForInt("select count(*) from users");
	}

	public List<User> getAll() {
	
		return this.jdbcTemplate.query(
				"select * from users order by id",
				this.userMapper);
	}
	
	/*
	 * 4.2.3, Dao interface & DataAccessException 계층구조
	 * 
	 * DataAccessException은
	 * JDBC + 기타 자바 데이터 엑세트 기술에서 발생하는 예외에도 적용
	 * JDO, JAP --> Java 표준 persistant 기술
	 * 
	 * 오라클 TopLink
	 * 오픈소스 하이버네이트
	 * 
	 * iBatis
	 */
	
	/*
	 * Dao interface 구현 / 분리
	 * 
	 * Dao 분리 이유
	 *  - 데이터 접근 로직을 담은 코드를 성격이 다른 코드에서 분리 위해
	 *  - Dao 사용자는 내부 구현부를 신경쓰지 않아도 됨
	 *  
	 * 따라서 Dao는
	 *  - interface를 사용해 구체적인 클래스 정보, 구현방법을 감추고
	 *  - DI를 통해 제공되는 것이 바람직 


	// db 접근시 api가 예외를 던지므로 사용불가
	public interface UserDao {
		public void add(User user);
		
		
	// 데이터 액세스 기술의 api에 따라 예외도 다르므로 SQLException도 사용 불가
	public void add(User user) throws SQLException;
	
	public void add(User user) throws PersistentException;	// jpa
	public void add(User user) throws HibernateException;	// hibernate
	public void add(User user) throws JdoException;			// jdo
	
	
	// 가장 단순한 해결법?
	public void add(User user) throws Exception				// 무책임
	
	
	// 또는 jap, hibernate, jdo는 런타임 예외를 지원하므로, 포장만 잘 하면 아래처럼 쓸 수도 있음
	// 하지만 모든 예외를 무시할 수는 없는 노릇. 
	public void add(User user);
	
	 */
	
	/*
	 * 위와 같은 문제로 데이터 엑세스시 발생하는 예외를 추상화함
	 *  - 데이터 엑세스 예외 추상화, DataAccessException 계층구조
	 *  
	 *  DataAccessException은 자바 주요 데이터 액세스 기술에서 발생할 수 있는 대부분 예외를 추상화.
	 *   - InvalidDataAccessResourceUsageException
	 *   	- 데이터 액세스 기술을 부정확하게 사용시
	 *   - ObjectOptimisticLockingFailureException
	 *   	- jdo, jpa, 하이버네이트처럼 오브젝트/엔티티 단위로 업데이트시 낙관적인 락킹이 걸릴 수 있음
	 *   		- 두 명이상 사용자가 동시에 조회, 순차적 업데이트시 뒤늦게 업데이트 한 것이 먼저 업데이트 한 것을 덮어쓰지 않도록 막아주는 기능
	 *   		- 적절한 메시지 지공 필요
	 *   - IncorrectResultSizeDataAccessException
	 *   	- sql 잘못 작성시 / jdbc는 예외 발생 안함 / jdbcTemplate은 발생시켜줌
	 *   	- subtype/ EmptyResultDataAccessException
	 *   
	 *  
	 * 따라서,
	 * Spring 데이터 액세스 지원기술을 사용해 Dao 구현시
	 *  - 기술에 독립적인 일관성 있는 예외를 던질 수 있음
	 */
	
	/*
	 * 이상적인 Dao 구현
	 *  - interface 사용
	 *  - runtime exception 전환
	 *  - DataAccessException 예외 추상화
	 */
	
}














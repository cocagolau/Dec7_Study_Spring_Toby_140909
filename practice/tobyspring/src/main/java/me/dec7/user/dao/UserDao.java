package me.dec7.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import me.dec7.user.domain.User;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/*
 * 4.1.2 예외 종류, 특징
 * 
 * throw를 통해 발생시킬 수 있는 예외
 *  1. java.lang.Error와 subClass
 *  	- 시스템에 뭔가 비정상적인 상황 발생시
 *  	- VM에서 발생
 *  		- 어플리케이션 코드에서 잡으려 하면 안됨
 *  		- OutOfMemoryError, ThreadDeath는 catch블록에서 잡아도 대응방법 없음
 *  
 *  2. java.lang.Exception과 subClass
 *  	- 어플리케이션 코드 작업 중 예외상황 발생시
 *  	1) checked execption
 *  		- 일반적인 예외
 *  		- Exception의 subClass && RuntimeException 상속하지 않은 것
 *  		- catch or throws 하지 않을 경우 compile error
 *  	2) unckecked exception
 *  		- RuntimeException을 상속한 것
 *  		- 명시적으로 프로그램 오류시 발생되도록 의도된 것들
 *  			- NullPointerException
 *  			- IllegalArgumentException
 *  		- 피할 수 있지만 개발자가 부주의해서 발생할 수 있는 경우를 대비해 만든 것
 *  			- 따라서 예상치 못한 상황에서 발생하는 것이 아니므로 굳이 catch, throws를 사용하지 않아도 되는 것
 * 
 */

/*
 * 4.1.3, 예외처리 방법
 * 
 *  1. 예외 복구
 *  	- 예외 상황 파악 --> 정상상태 복구
 *  	- 예외 처리시 기능적으로 어플리케이션은 정상적으로 설계된 흐름을 따라야 함
 *  
 *  2. 예외 회피
 *  	- 예외 처리를 자신이 담당하지 않고 호출한 쪽으로 던지는 것
	
		// NO
		public void add() throws SQLException {
			// JDBC API
		}
		
		// YES
		public void add() throws SQLException {
			try {
				// JDBC API
			} catch (SQLException e) {
				// log 출력
				throw e;
			}
		}
		
		template/callback pattern에서
		callback object는 예외를 template쪽으로 던짐
		 - 콜백이 예외를 처리하는 역할이 아니라고 판단
		 - 이처럼 긴밀한 역할분담 관계가 아닐 경우 예외를 던지는 것은 무책임한 일
		 
 *  3. 예외 전환
 *  	- 예외를 밖으로 던지되, 
 *  	  발생한 예외가 아닌, 적절한 예외로 전환해 던짐
 *  
 *  	1) 예외의 의미를 분명히 하기 위해
			- 예외 전환 기능을 가진 add() 메소드
  			
 *  	2) 예외를 처리하기 쉽고 단순하게 만들기 위해 포장
 *  		- 중첩 예외를 이용해 새로운 예외를 만들고 원인 예외를 내부에 담아 던지는 것은 동등
 *  		- 하지만 의미를 명확히 하려는 것이 아니라
 *  		  예외처리를 강제하는 체크 예외를 언체크 예외 (런타임)로 바꾸는 것
 *
 */

/*
 * 4.1.4, 예외 처리 전략
 * 
 * 체크 예외시 복구 불가 --> 런타임 예외
 * 
 * 과거 자바로 애플릿 같은 독립 어플리케이션 환경에서
 *  - 통제 불가능한 시스템 예외라도 어플리케이션 작업이 종료되지 않도록 하고 상황을 복구해야했음.
 *  
 * 자바 엔터프라이즈 환경은 다름
 *  - 수많은 요청 중 예외가 발생한 요청만 중단하면 됨.
 *  - 오히려 서버를 중단하고 사용자와 커뮤니케이션하면서 예외상황을 복구할 수 없음
 *  - 차라리 어플리케이션 차원에서 예외상황을 미리 파악하고 예외가 발생하지 않도록 차단하는 것이 좋음
 *  - 그리고 프로그램, 외부완경 오류 발생시 빨리 작업 취소 후 담당자 통보가 좋음
 * 
 * 자바 환경이 서버로 이동하면서 체크 예외 활동도의 가치는 낮아짐
 *  - 자칫 throws Exception으로 의미없는 메소드만 낳을 뿐.
 * 
 * 차라리, 빨리 Runtime Exception으로 전환해서 던지는 것이 좋음
 * 최근엔 체크예외 대신, 언체크 예외로 정의하는 것일 일반화
 *  - 예전엔 복구할 가능성이 조금이라도 존재시 체크 예외로 만든다고 생각했음
 *  - 하지만 지금은 항상 복구할 수 있는 예외가 아니라면 일단 언체크로 만드는 경향이 있음 
 * 
 */

/*
 * 낙관적인 예외 기법
 *  - 런타임 예외 / 필요할 때 사용
 * 
 * 비관적인 예외 기법
 *  - 일반적인 예외 / 일단 잡고 봄
 * 
 * 어플리케이션 예외
 *  - 어플리케이션 자체의 로직에 의해 의도적 발생
 *  
 *  - 반환 값으로 상태 확인시
 *  	- 리턴 값을 잘 관리하기 어렵고, 혼란 발생여지 있음
 *  	- 결과 값을 확인하는 분기문 필요 (많아지면 흐름 파악 어려움)
 *  
 *  - 정상적인 흐름을 그대로 따르되 예외상에서 의미를 가진 예외를 던짐
		
		try {
			BigDecimal balance = account.withdraw(amount);
			...
			// 정상적인 처리결과 출력
		} catch (InsufficientBalanceException e) {
			// InsufficientBalanceException에 담긴 인출 가능 잔고 금액정보 가져옴
			BigDecimal availFunds = e.getAvailFunds();
			...
			// 잔고 부족 안내 메시지 출력
		}
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
	public void add(User user) {
		this.jdbcTemplate.update("insert into users(id, name, password) values(?,?,?)", user.getId(), user.getName(), user.getPassword());
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
	
}














package me.dec7.issuetracker.sqlservice.updatable;

import java.util.Map;

import javax.sql.DataSource;

import me.dec7.issuetracker.sqlservice.SqlUpdateFailureException;
import me.dec7.issuetracker.sqlservice.UpdatableSqlRegistry;
import me.dec7.user.sqlservice.SqlNotFoundException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


/*
 * 내장형 데이터베이스
 *  - 어플리케이션에 내장되어 어플리케이션과 함께 시작/종료되는 DB
 *  - IO 부하가 적어 성능 뛰어남
 *  - 컬렉션/오브젝트를 사용해 메모리에 저장하는 방법에 비해 효과적 / 안정적인 등록, 수정, 검색
 *  - 최적화된 락킹, 격리수준, 트랜잭션 모두 적용 가능
 *  - 복잡한 데이터를 효과적 분석/조작을 위해 관계형 DB만한게 없음
 *  - Derby, HSQL, H2
 *  	- 모두 JDBC 드라이버 제공
 *  	- 표준 DB와 호환
 *  	- 초기화 작업이 별도로 필요 
 *  		- 어플리케이션 내에서 DB 가정, 초기화, SQL 스트립트 실행 등
 *  		- 이후에 내장 DB용 JDBC드라이버로 일반 DB와 동등하게 접속 / 사용
 *  
 * 스프링 지원 내장형 DB
 *  - 내장형 DB 빌더로 초기화 작업
 */

/*
 * 트랜잭션 적용
 *  - 내장형 DB를 사용하므로 조회가 빈번하더라도 안전한 수정 보장
 *  - 하지만 하나 이상의 SQL을 맵으로 받아 수정시 문제발생 가능
 *  	- 트랜잭션 적용 필요
 *  - HashMap과 같은 컬렉션은 트랜잭션 개념을 적용하기 매우 어려움
 *  	- 반면, DB는 적용이 상대적으로 쉬움
 *  - spring에서 트랜잭션 경계가 dao 밖에 있고 범위가 넒은 경우 AOP가 편리
 *  	- 하지만, 제한된 오브젝트에서 특화된 서비스의 트랜잭션이라면 간단한 트랜잭션 추상화 API가 훨씬 편함
 */
public class EmbeddedDbSqlRegistry implements UpdatableSqlRegistry {
	
	SimpleJdbcTemplate jdbc;
	// JdbcTemplate과 Transaction을 동기화해주는 트랜잭션 탬플릿 / 멀티스레드 환경에서 공유 가능
	TransactionTemplate transactionTemplate;
	
	/*
	 * DataSource DI
	 *  - interface의 분리 원칙을 지키기 위함
	 *  - DataSource와 EmbeddedDatabase는 상속관계
	 *  - 클라이언트 자신이 필요로 하는 기능을 가진 interface를 통해 의존 오브젝트를 DI해야함
	 *  - SQL registry는 JDBC를 이용해 DB에 접근할 수만 있으면 되므로
	 *  - 따라서 DB 종료기능을 가진 EmbeddedDatabase 대신 DataSource interface 사용
	 * 
	 */
	public void setDataSource(DataSource dataSource) {
		jdbc = new SimpleJdbcTemplate(dataSource);
		// DataSource로 TransactionManager를 만들고 다시 TransactionTemplate을 만든다
		transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
	}
	

	@Override
	public void registerSql(String key, String sql) {
		jdbc.update("insert into sqlmap(key_, sql_) values(?,?)", key, sql);
	}

	@Override
	public String findSql(String key) throws SqlNotFoundException {
		try {
			return this.jdbc.queryForObject("select sql_ from sqlmap where key_ = ?", String.class, key);
			
		// queryForObject()는 쿼리 결과가 없는 경우 발생시키는 예외
		} catch (EmptyResultDataAccessException e) {
			throw new SqlNotFoundException(key + "에 해당하는 SQL을 찾을 수 없습니다.", e);
		}
	}
	
	@Override
	public void updateSql(String key, String sql) throws SqlUpdateFailureException {
		// update()는 SQL 실행결과로 영향받은 레코드 수를 반환함
		int affected = this.jdbc.update("update sqlmap set sql_ = ? where key_ = ?", sql, key);
		if (affected == 0) {
			throw new SqlUpdateFailureException(key + "에 해당하는 SQL을 찾을 수 없습니다.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see me.dec7.issuetracker.sqlservice.UpdatableSqlRegistry#updateSql(java.lang.String, java.lang.String)
	 * 
	 * template-callback pattern 적용
	 *  - PlatformTransactionManager를 사용해도 되나 간결하게 TransactionTemplate을 쓰는 것이 좋음
	 *  - EmbeddedDbSqlRegistry가 DataSource를 DI받고 transactionManager와 template을 만들게 함
	 *    	- 보통 AOP를 통해 만들어지는 트랜잭션 프록시가 트랜잭션 매니져를 공유해야 하나 이곳에선 필요 없음 
	 *    	- EmbeddedDbSqlRegistry에 TransactionManager를 직접 생성
	 */
	@Override
	// 익명 내부 클래스로 만들어지는 콜백 오브젝트 안에서 사용되는 것이므로 final로 선언해야함
	public void updateSql(final Map<String, String> sqlmap) throws SqlUpdateFailureException {
		
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			
			// 트랜잭션 템플릿이 만드는 트랜잭션 경계 안에서 동작할 코드를 콜백 형태로 만들고 execute 메소드에 전달 
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {

				for (Map.Entry<String, String> entry : sqlmap.entrySet()) {
					updateSql(entry.getKey(), entry.getValue());
				}
				
			}
			
		});
	}

}

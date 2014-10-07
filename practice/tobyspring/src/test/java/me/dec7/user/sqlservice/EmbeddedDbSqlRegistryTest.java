package me.dec7.user.sqlservice;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import java.util.HashMap;
import java.util.Map;

import me.dec7.issuetracker.sqlservice.SqlUpdateFailureException;
import me.dec7.issuetracker.sqlservice.UpdatableSqlRegistry;
import me.dec7.issuetracker.sqlservice.updatable.EmbeddedDbSqlRegistry;

import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

public class EmbeddedDbSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {
	
	EmbeddedDatabase db;
	
	@Override
	protected UpdatableSqlRegistry createUpdateableSqlRegistry() {
		db = new EmbeddedDatabaseBuilder()
			.setType(H2)
			.addScript("classpath:/sql/query/sqlRegistrySchema.sql")
			.build();
		
		EmbeddedDbSqlRegistry embeddedDbSqlRegistry = new EmbeddedDbSqlRegistry();
		embeddedDbSqlRegistry.setDataSource(db);
		
		return embeddedDbSqlRegistry;
	}
	
	@After
	public void tearDown() {
		db.shutdown();
	}
	
	/*
	 * 테스트를 먼저 만들어야함
	 * 트랜잭션 적용은 수동 테스트로 검증하기 어려움.
	 * 	- 특별한 예외 상황이 아닐 경우 트랜잭션 적용 여부가 결과에 영향 주지 않게 때문
	 * 	- 그러므로 트랜적션 적용시 성공, 아닐 경우 실패하는 테스트를 만들어야함
	 * 
	 * 1. 현재의 EmbeddedDbSqlRegistry 코드가 테스트를 만족하지 못하고 실패하도록 만듦
	 * 2. 이후에 테스트가 성공하도록 트랜잭션 기능 추가
	 */
	
	// 예외가 발생해야 성공
	@Test(expected=SqlUpdateFailureException.class)
	public void transactionUpdate() {
		/* 
		 * 초기상태 확인
		 * 트랜잭션 롤백 후 결과와 비교돼서 
		 * 테스트의 목적인 처음과 동일하다는 것을 보여주기 위함
		 */
		checkFind("SQL1", "SQL2", "SQL3");
		
		Map<String, String> sqlmap = new HashMap<String, String>();
		sqlmap.put("KEY1", "Modified1");
		// 해당 key가 존재하지 않으므로 테스트 실패 -> 롤백 수행 여부 확인
		sqlmap.put("KEY9999", "Modified9999");
		
		sqlRegistry.updateSql(sqlmap);
		
		// 롤백이 수행되었다면 이전 상태로 돌아와야함
		checkFind("SQL1", "SQL2", "SQL3");
	}
	

}

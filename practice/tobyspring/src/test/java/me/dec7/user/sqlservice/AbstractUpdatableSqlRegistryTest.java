package me.dec7.user.sqlservice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import me.dec7.issuetracker.sqlservice.UpdatableSqlRegistry;

import org.junit.Before;
import org.junit.Test;


/*
 * EmbeddedDbSqlRegistry도 테스트 검증 필요
 *  - ConcurrentHashMapSqlRegistry와 비슷한 내용을 담을 가능성 높음
 *  - JUnit4.x의 경우 상속구조로 테스트 가능
 */
public abstract class AbstractUpdatableSqlRegistryTest {
	// 사용할 fixture를 interface로 정의
	UpdatableSqlRegistry sqlRegistry;

	@Before
	public void setUp() {
		// 이 부분만 특정 클래스 ConcurrentHashMapSqlRegistry라는 클래스에 의존함
		sqlRegistry = createUpdateableSqlRegistry();
		sqlRegistry.registerSql("KEY1","SQL1");
		sqlRegistry.registerSql("KEY2","SQL2");
		sqlRegistry.registerSql("KEY3","SQL3");
	}
	
	// test fixture를 생성하는 부분만 추상 메소드로 만들어 subclass에서 구현
	abstract protected UpdatableSqlRegistry createUpdateableSqlRegistry();
	
	protected void checkFind(String expected1, String expected2, String expected3) {
		assertThat(sqlRegistry.findSql("KEY1"), is(expected1));
		assertThat(sqlRegistry.findSql("KEY2"), is(expected2));
		assertThat(sqlRegistry.findSql("KEY3"), is(expected3));
	}
	
	@Test
	public void find() {
		checkFindResult("SQL1", "SQL2", "SQL3");
	}
	
	@Test(expected=SqlNotFoundException.class)
	public void unknownKey() {
		sqlRegistry.findSql("SQL999");
	}
	
	@Test
	public void updateSingle() {
		sqlRegistry.updateSql("KEY2", "Modified2");
		checkFindResult("SQL1", "Modified2", "SQL3");
	}
	
	@Test
	public void updateMulti() {
		Map<String, String> sqlmap = new HashMap<String, String>();
		sqlmap.put("KEY1", "Modified1");
		sqlmap.put("KEY3", "Modified3");
		
		sqlRegistry.updateSql(sqlmap);
		checkFindResult("Modified1", "SQL2", "Modified3");
	}

	private void checkFindResult(String expected1, String expected2, String expected3) {
		assertThat(sqlRegistry.findSql("KEY1"), is(expected1));
		assertThat(sqlRegistry.findSql("KEY2"), is(expected2));
		assertThat(sqlRegistry.findSql("KEY3"), is(expected3));
	}

}












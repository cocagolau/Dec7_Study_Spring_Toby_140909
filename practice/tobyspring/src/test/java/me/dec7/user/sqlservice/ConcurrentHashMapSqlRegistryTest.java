package me.dec7.user.sqlservice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import me.dec7.issuetracker.sqlservice.UpdatableSqlRegistry;

import org.junit.Before;
import org.junit.Test;

/*
 * 동시성 문제
 *  - 운영 중인 시스템에서 사용하는 정보를 실시간으로 변경하는 작업을 만들 때 가장 고려할 점
 *  - JDK의 HashMap
 *  	- 멀티스레드 환경에서 동시 수정시 잘못된 결과가 나올 수 있음
 *  	- 안전한 조작을 위해 Collections.synchronizedMap() 등을 이용해 외부에서 동기화 필요
 *  	- 하지만 DAO 처럼 요청이 많은 경우 (고성능 서비스) 성능상 문제 발생
 *  - ConcurrentHashMap
 *  	- 동기화된 해시 데이터 조작에 최적화 되도록 만들어져 사용 권장
 *  	- 데이터 조작시 전체 데이터에 대해 락을 걸지 않고, 조회는 락을 아예 사용 안함
 *  	- 어느정도 안전 + 성능 보장
 */

/*
 * SQL 변경 검증
 *  - 기존 UserDaoTest로 불가능
 */

/*
 * EmbeddedDbSqlRegistry도 테스트 검증 필요
 *  - ConcurrentHashMapSqlRegistry와 비슷한 내용을 담을 가능성 높음
 *  - JUnit4.x의 경우 상속구조로 테스트 가능
 */
public class ConcurrentHashMapSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {
	
	/*
	 * (non-Javadoc)
	 * @see me.dec7.user.sqlservice.AbstractUpdatableSqlRegistryTest#createUpdateableSqlRegistry()
	 * 
	 * @Test가 붙은 메소드는 모두 상속받아서 자신의 메소드로 활용함
	 */
	@Override
	protected UpdatableSqlRegistry createUpdateableSqlRegistry() {
		return new ConcurrentHashMapSqlRegistry();
	}
	
	/*
	// 사용할 fixture를 interface로 정의
	UpdatableSqlRegistry sqlRegistry;

	@Before
	public void setUp() {
		// 이 부분만 특정 클래스 ConcurrentHashMapSqlRegistry라는 클래스에 의존함
		sqlRegistry = new ConcurrentHashMapSqlRegistry();
		sqlRegistry.registerSql("KEY1","SQL1");
		sqlRegistry.registerSql("KEY2","SQL2");
		sqlRegistry.registerSql("KEY3","SQL3");
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
	*/


}












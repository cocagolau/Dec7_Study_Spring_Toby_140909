package me.dec7.user.sqlservice;

import javax.annotation.PostConstruct;

/*
 * 유연성을 보장하기 위해 책임과 역할을 분리한 뒤 DI를 통해 의존관계를 자유롭게 변경할 수 있는 코드가 필요
 * 
 * 특정 의존 오브젝트가 대부분 환경에서 거의 default인 경우
 *  - default 의존관계 bean을 구성
 *  - default 의존관계	
 *  	- 외부에서 DI 받지 않는 경우 기본적으로 자동 적용되는 의존관계
 * 
 */
public class BeanSqlService implements SqlService {
	
	/*
	 * BaseSqlService는 상속을 통해 확장해서 사용하기에 적합
	 * sub class에서 접근하도록 protected로 선언
	 */
	protected SqlReader sqlReader;
	protected SqlRegistry sqlRegistry;
	
	@PostConstruct
	public void loadSql() {
		this.sqlReader.read(this.sqlRegistry);
	}
	
	public void setSqlReader(SqlReader sqlReader) {
		this.sqlReader = sqlReader;
	}

	public void setSqlRegistry(SqlRegistry sqlRegistry) {
		this.sqlRegistry = sqlRegistry;
	}

	@Override
	public String getSql(String key) throws SqlRetrievalFailureException {
		try  { return this.sqlRegistry.findSql(key); }
		catch (SqlNotFoundException e) { throw new SqlRetrievalFailureException(e); }
	}

}

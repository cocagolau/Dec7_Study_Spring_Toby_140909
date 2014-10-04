package me.dec7.user.sqlservice;



/*
 * 한가지 단점 존재
 *  - 설정을 통해 구현 오브젝트를 사용하게 하더라도
 *    DefaultSqlService는 생성자에서 default 의존 오브젝트를 다 만들어 버림
 *  - 일반 오브젝트는 큰 문제가 없으나
 *    큰 리소스를 소모하는 오브젝트의 경우 문제가 됨
 *  - PostConstructor
 *  	- 프로퍼티 설정여부 확인 후 default 적용
 */
public class DefaultSqlService extends BaseSqlService {
	
	public DefaultSqlService() {		
		setSqlReader(new JaxbXmlSqlReader());
		setSqlRegistry(new HashMapSqlRegistry());
	}
	
	/*
	@PostConstruct
	public void loadSqlService() {
		
		if (sqlReader == null) 		setSqlReader(new JaxbXmlSqlReader());
		if (sqlRegistry == null) 	setSqlRegistry(new HashMapSqlRegistry());
		
	}
	*/
}

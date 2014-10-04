package me.dec7.user.sqlservice;

import java.util.Map;


/*
 * SqlService
 *  - DAO가 SqlService를 사용하도록 변경
 *  	- 모든 DAO는 sql의 저장,제공 방법에 대해서 더 이상 신경쓰지 않아도 됨
 *  - sqlService bean에는 DAO에 전혀 영향을 주지 않은 채로
 *    다양한 방법으로 구현된 SqlService 타입 클래스를 적용할 수 있음
 */
/*
 * 
 */
public class SimpleSqlService implements SqlService {
	
	private Map<String, String> sqlMap;
	
	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}

	@Override
	public String getSql(String key) throws SqlRetrievalFailureException {
		// 내부 sqlMapㅇ네서 sql을 가져옴
		String sql = this.sqlMap.get(key);
		
		if (sql == null) throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다.");
		else return sql;
	}

}

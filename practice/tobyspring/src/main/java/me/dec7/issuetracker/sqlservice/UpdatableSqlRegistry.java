package me.dec7.issuetracker.sqlservice;

import java.util.Map;

import me.dec7.user.sqlservice.SqlRegistry;

/*
 * SqlRegistry의 SubInterface
 *  - SQL 등록+조회 + 업데이트
 */
public interface UpdatableSqlRegistry extends SqlRegistry {
	
	public void updateSql(String key, String sql) throws SqlUpdateFailureException;
	
	public void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException;

}

package me.dec7.user.sqlservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.dec7.issuetracker.sqlservice.SqlUpdateFailureException;
import me.dec7.issuetracker.sqlservice.UpdatableSqlRegistry;

public class ConcurrentHashMapSqlRegistry implements UpdatableSqlRegistry {
	
	private Map<String, String> sqlMap = new ConcurrentHashMap<String, String>();

	@Override
	public void registerSql(String key, String sql) {
		this.sqlMap.put(key, sql);
	}

	@Override
	public String findSql(String key) throws SqlNotFoundException {
		String sql = this.sqlMap.get(key);
		if (sql == null) 	throw new SqlNotFoundException(key + "를 이용해서 SQL을 찾을 수 없었습니다.");
		else 				return sql;
	}

	@Override
	public void updateSql(String key, String sql) throws SqlUpdateFailureException {
		if (this.sqlMap.get(key) == null) {
			throw new SqlUpdateFailureException(key + "에 해당하는 SQL을 찾을 수 없었습니다.");
		}
		
		this.sqlMap.put(key, sql);
	}

	@Override
	public void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException {
		for (Map.Entry<String, String> entry : sqlmap.entrySet()) {
			updateSql(entry.getKey(), entry.getValue());
		}
		
	}

}

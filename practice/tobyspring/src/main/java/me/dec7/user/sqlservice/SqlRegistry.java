package me.dec7.user.sqlservice;


/*
 * SqlRegistry는 
 *  - 등록, 검색하는 두 가지 기능이 있음
 *  
 * SqlNotFoundException
 *  - 코드에 버그, 설정문제 발생할 경우 나타남
 *  - 따라서 복구할 가능성이 낮다고 판단해 runtime exception
 * 
 */
public interface SqlRegistry {
	
	// sql을 key와 함께 등록
	void registerSql(String key, String sql);
	
	// key로 sql검색
	// 실패시 예외를 던짐
	String findSql(String key) throws SqlNotFoundException;
}

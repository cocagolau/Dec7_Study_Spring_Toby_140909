package me.dec7.user.sqlservice;


/*
 * sql 제공기능을 분리
 * 
 * SQL 서비스의 인터페이스를 설계
 *  - client인 Dao를 SQL 서비스의 구현에서 독립적으로 만들도록 인터페이스 사용 --> DI주입
 *  - client가 사용할 SqlService interface 기능
 *  	- sql에 대한 key 전달 --> sql 제공
 */
public interface SqlService {
	
	String getSql(String key) throws SqlRetrievalFailureException;

}

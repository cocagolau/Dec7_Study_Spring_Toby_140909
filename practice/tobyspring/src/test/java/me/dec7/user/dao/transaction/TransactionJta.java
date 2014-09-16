package me.dec7.user.dao.transaction;


/*
 * JTA를 이용하더라도 트랜잭션 경계를 설정하는 방법은 JDBC와 비슷
 * 
 * 문제점
 *  - Jdbc 로컬 트랜잭션을 JTA 글로벌 트랜잭션으로 바꾸려면
 *    UserService 코드를 수정해야함
 *  - 즉, 트랜잭션 API에 의존됨 
 *  	- UserService 코드가 특정 트랜잭션 방법에 의존적이지 않는 방법
 * 
 * 해결책
 *  - JDBC, JTA, 하이버네이트, JPA, JDO, JMS 모두 트랜잭션 개념을 가짐
 *  - 트랜잭션 경계 설정 방법에 공통점을 찾고 추상화
 * 
 */
public class TransactionJta {
	InitialContext ctx = new InitialContext();
	
	// JNDI를 이용해 UserTransaction 오브젝트를 가져옴
	UserTransaction tx = (UserTransaction) ctx.lookup(USER_TX_JNDI_NAME);
	tx.begin();
	
	// JNDI로 가져온 dataSource를 사용
	Connection c = dataSource.getConnection();
	
	try {
		// 데이터 엑세스 코드
		tx.commit();
	} catch (Exception e) {
		tx.rollback();
		throw e;
	} finally {
		c.close();
	}
}

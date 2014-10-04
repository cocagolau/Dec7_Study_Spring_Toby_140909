package me.dec7.user.sqlservice;

/*
 * SqlRetrievalFailureException
 *  - RuntimeException 상속
 *  - 복구 불가능한 error
 *  - 어떤 이유든 실패시 위 예외를 던짐
 *  - 예외의 원인을 구분해야할 경우 SubClass를 재정의
 */
public class SqlRetrievalFailureException extends RuntimeException {

	public SqlRetrievalFailureException(String message) {
		super(message);
	}
	
	public SqlRetrievalFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlRetrievalFailureException(Throwable e) {
		super(e);
	}
	
}

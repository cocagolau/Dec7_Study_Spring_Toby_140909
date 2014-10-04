package me.dec7.user.sqlservice;

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

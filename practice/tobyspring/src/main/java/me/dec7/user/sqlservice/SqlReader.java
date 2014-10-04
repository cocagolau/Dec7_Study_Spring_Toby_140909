package me.dec7.user.sqlservice;

public interface SqlReader {
	
	/*
	 * sql을 외부에서 가져와 SqlRegistry에 등록
	 *  - 다양한 예외 발생할 수 있으나
	 *    대부분 복구 불가능한 예외이므로 굳이 예외를 선언하지 않음
	 */
	void read(SqlRegistry sqlRegistry);
}

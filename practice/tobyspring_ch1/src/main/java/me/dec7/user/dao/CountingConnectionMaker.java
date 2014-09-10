package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.SQLException;


/*
 * CountingConnectionMaker는 ConnectionMaker interface를 구현했지만 내부적으로 DB connection를 만들지 않음
 * 대신 DB connection을 부를때마다 counter를 증가
 */
public class CountingConnectionMaker implements ConnectionMaker {

	int counter = 0;
	private ConnectionMaker realConnectionMaker;
	
	/*
	 * CountingConnectionMaker도 생성자를 통해 DI를 받음
	 */
	public CountingConnectionMaker(ConnectionMaker realConnectionMaker) {
		this.realConnectionMaker = realConnectionMaker;
	}

	@Override
	public Connection makeConnection() throws ClassNotFoundException, SQLException {
		this.counter++;
		
		return this.realConnectionMaker.makeConnection();
	}
	
	public int getCounter() {
		
		return this.counter;
	}

}

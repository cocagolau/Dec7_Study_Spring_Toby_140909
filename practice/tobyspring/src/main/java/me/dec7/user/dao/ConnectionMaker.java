package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.SQLException;


/*
 * Interface
 *  - 어떤 일을 하겠다고 기능 정의만 한 것
 *  - UserDao가 interface를 사용한다면 그 기능만 관심을 가질 뿐
 *    어떻게 구현했는지는 무관심 함
 */
public interface ConnectionMaker {
	
	public Connection makeConnection() throws ClassNotFoundException, SQLException;
}

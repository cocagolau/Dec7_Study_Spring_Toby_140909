package me.dec7.user.dao.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public class TransactionJdbc {
	
	Connection c = dataSource.getConnection();
	
	// 트랜잭션 시작
	// autoCommit을 false로 변경
	c.setAutoCommit(false);
	
	try {
		// -----------------------
		// 하나의 트랜잭션으로 묶인 단위 작업
		PreparedStatement st1 = c.prepareStatement("update users ...");
		st1.executeUpdate();
		
		PreparedStatement st2 = c.prepareStatement("delete users ...");
		st2.executeUpdate();
		
		// -----------------------
		// 트랜잭션 커밋
		c.commit();
		
	} catch (Exception e) {
		// 트랜잭션 롤백
		c.rollback();
	}
	
}

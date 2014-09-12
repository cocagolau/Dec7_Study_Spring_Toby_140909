package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;


/*
 * bean 의존관계 변경
 * 
 */
public class JdbcContext {
	
	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void workWithStatementStrategy(StatementStrategy stmtStrategy) throws SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		
		try {
			c = this.dataSource.getConnection();
		
			ps = stmtStrategy.makePreparedStatement(c);
			
			ps.executeUpdate();

		} catch (SQLException sqle) {
			throw sqle;
		
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException sqle) { }
			}
			
			if (c != null) {
				try {
					c.close();
					
				} catch (SQLException sqle) { }
			}
		}
	}
}

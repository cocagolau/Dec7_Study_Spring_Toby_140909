package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/*
 * 전략 패턴 StatementStratege interface 확장
 */
public class DeleteAllStatement implements StatementStrategy {

	@Override
	public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
		PreparedStatement ps = c.prepareStatement("delete from users");
		
		return ps;
	}

}

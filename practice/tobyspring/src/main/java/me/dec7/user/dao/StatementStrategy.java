package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementStrategy {
	
	PreparedStatement makePrepareStatement(Connection c) throws SQLException;
	
}

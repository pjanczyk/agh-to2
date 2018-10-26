package pl.edu.agh.iisg.to.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import pl.edu.agh.iisg.to.connection.ConnectionProvider;

public class QueryHelper {

	public static PreparedStatement prepareStatement(String query) throws SQLException {
		return ConnectionProvider.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
	}
	
	public static int readIdFromResultSet(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }
}

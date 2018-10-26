package pl.edu.agh.iisg.to.executor;

import pl.edu.agh.iisg.to.connection.ConnectionProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public final class QueryExecutor {

    private static final Logger LOGGER = Logger.getGlobal();

    private QueryExecutor() {
        throw new UnsupportedOperationException();
    }

    static {
        try {
            LOGGER.info("Creating table Student");
            create("CREATE TABLE IF NOT EXISTS student (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "index_number int NOT NULL, " +
                    "UNIQUE (index_number) " +
                    ");");
            LOGGER.info("Creating table Course");
            create("CREATE TABLE IF NOT EXISTS course (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "UNIQUE (name) " +
                    ");");
            LOGGER.info("Creating table Student_Course");
            create("CREATE TABLE IF NOT EXISTS student_course (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INT NOT NULL, " +
                    "course_id INT NOT NULL, " +
                    "FOREIGN KEY(student_id) references student (id), " +
                    "FOREIGN KEY(course_id) references course (id), " +
                    "UNIQUE (student_id, course_id)" +
                    ");");
            LOGGER.info("Creating table Grade");
            create("CREATE TABLE IF NOT EXISTS grade (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "grade REAL NOT NULL, " +
                    "student_id INT NOT NULL, " +
                    "course_id INT NOT NULL, " +
                    "FOREIGN KEY(student_id) references student (id), " +
                    "FOREIGN KEY(course_id) references course (id) " +
                    ");");

        } catch (SQLException e) {
            LOGGER.info("Error during create tables: " + e.getMessage());
            throw new RuntimeException("Cannot create tables");
        }
    }

    public static int createAndObtainId(final String insertSql) throws SQLException {
        try (final PreparedStatement statement = ConnectionProvider.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.execute();
            try (final ResultSet resultSet = statement.getGeneratedKeys()) {
                return readIdFromResultSet(resultSet);
            }
        }
    }

    private static int readIdFromResultSet(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public static void create(final String insertSql) throws SQLException {
        try (final PreparedStatement statement = ConnectionProvider.getConnection().prepareStatement(insertSql)) {
            statement.execute();
        }
    }

    public static ResultSet read(final String sql) throws SQLException {
        final Statement statement = ConnectionProvider.getConnection().createStatement();
        final ResultSet resultSet = statement.executeQuery(sql);
        LOGGER.info(String.format("Query: %s executed.", sql));
        return resultSet;
    }

    public static void delete(final String sql) throws SQLException {
        executeUpdate(sql);
    }

    private static void executeUpdate(final String... sql) throws SQLException {
        try (final Statement statement = ConnectionProvider.getConnection().createStatement()) {
            ConnectionProvider.getConnection().setAutoCommit(false);
            for (String s : sql) {
                statement.executeUpdate(s);
                LOGGER.info(String.format("Query: %s executed.", s));
            }
            ConnectionProvider.getConnection().commit();
            ConnectionProvider.getConnection().setAutoCommit(true);
        }
    }
}

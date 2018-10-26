package pl.edu.agh.iisg.to.model;

import pl.edu.agh.iisg.to.executor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Student {

    public static final String TABLE_NAME = "student";

    private final int id;

    private final String firstName;

    private final String lastName;

    private final int indexNumber;

    private Student(final int id, final String firstName, final String lastName, final int indexNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.indexNumber = indexNumber;
    }

    public static Optional<Student> create(final String firstName, final String lastName, final int indexNumber) {
        String insertSql = String.format("INSERT INTO %s (first_name, last_name, index_number) " +
                        "VALUES ('%s', '%s', %d);",
                TABLE_NAME, firstName, lastName, indexNumber);

        int id;
        try {
            id = QueryExecutor.createAndObtainId(insertSql);
        } catch (SQLException e) {
            return Optional.empty();
        }

        return Student.findById(id);
    }

    public static Optional<Student> findByIndexNumber(final int indexNumber) {
        String findByIndexNumberSql = String.format("SELECT * FROM student WHERE index_number = %d", indexNumber);

        try {
            ResultSet rs = QueryExecutor.read(findByIndexNumberSql);
            return Optional.of(new Student(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getInt("index_number")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Course, Float> createReport() {
        String findCoursesSql = String.format("SELECT " +
                        "  course.*, " +
                        "  (SELECT AVG(grade.grade) " +
                        "   FROM grade " +
                        "   WHERE grade.course_id = course.id AND grade.student_id = %d) AS average " +
                        "FROM course " +
                        "JOIN student_course ON course.id = student_course.course_id " +
                        "WHERE student_course.student_id = %d",
                id, id);

        Map<Course, Float> results = new HashMap<>();

        try {
            ResultSet rs = QueryExecutor.read(findCoursesSql);

            while (rs.next()) {
                Course course = new Course(rs.getInt("id"), rs.getString("name"));
                float average = rs.getFloat("average");
                results.put(course, average);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    public static Optional<Student> findById(final int id) {
        String findByIdSql = String.format("SELECT * FROM student WHERE id = %d", id);
        try {
            ResultSet rs = QueryExecutor.read(findByIdSql);
            return Optional.of(new Student(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getInt("index_number")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static List<Student> fromResultSet(ResultSet rs) throws SQLException {
        List<Student> results = new LinkedList<>();

        while (rs.next()) {
            Student student = new Student(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getInt("index_number"));
            results.add(student);
        }

        return results;
    }

    public int id() {
        return id;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public int indexNumber() {
        return indexNumber;
    }

    public static class Columns {

        public static final String ID = "id";

        public static final String FIRST_NAME = "first_name";

        public static final String LAST_NAME = "last_name";

        public static final String INDEX_NUMBER = "index_number";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Student student = (Student) o;

        if (id != student.id)
            return false;
        if (indexNumber != student.indexNumber)
            return false;
        if (!firstName.equals(student.firstName))
            return false;
        return lastName.equals(student.lastName);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + indexNumber;
        return result;
    }
}

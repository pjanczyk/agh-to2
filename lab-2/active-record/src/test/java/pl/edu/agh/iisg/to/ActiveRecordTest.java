package pl.edu.agh.iisg.to;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.edu.agh.iisg.to.connection.ConnectionProvider;
import pl.edu.agh.iisg.to.executor.QueryExecutor;
import pl.edu.agh.iisg.to.model.Course;
import pl.edu.agh.iisg.to.model.Grade;
import pl.edu.agh.iisg.to.model.Student;

public class ActiveRecordTest {

    @BeforeClass
    public static void init() {
        ConnectionProvider.init("jdbc:sqlite:active_record_test.db");
    }

    @Before
    public void setUp() throws SQLException {
        QueryExecutor.delete("DELETE FROM STUDENT_COURSE");
        QueryExecutor.delete("DELETE FROM STUDENT");
        QueryExecutor.delete("DELETE FROM COURSE");
        QueryExecutor.delete("DELETE FROM GRADE");
    }

    @AfterClass
    public static void cleanUp() throws SQLException {
        ConnectionProvider.close();
    }

    @Test
    public void createStudentTest() {
        Optional<Student> first = Student.create("Adam", "Kowalski", 100122);
        checkStudent(first);
        Optional<Student> second = Student.create("Jan", "Nowak", 100123);
        checkStudent(second);
        Assert.assertNotEquals(first.get().id(), second.get().id());
        Optional<Student> third = Student.create("Kasia", "Kowalska", 100123);
        Assert.assertTrue(!third.isPresent());
    }
    
    @Test
    public void findStudentTest() {
        Optional<Student> first = Student.create("Kasia", "Kowalska", 200124);
        checkStudent(first);
        Optional<Student> second = Student.findById(first.get().id());
        Assert.assertEquals(first.get(), second.get());
        Optional<Student> third = Student.findById(Integer.MAX_VALUE);
        Assert.assertTrue(!third.isPresent());
    }

    @Test
    public void findStudentIndexTest() {
        Optional<Student> first = Student.create("Kasia", "Kowalska", 300124);
        checkStudent(first);
        Optional<Student> second = Student.findByIndexNumber(first.get().indexNumber());
        Assert.assertEquals(first.get(), second.get());
    }

    @Test
    public void createCourseTest() {
        Optional<Course> first = Course.create("TO");
        checkCourse(first);
        Optional<Course> second = Course.create("TO2");
        checkCourse(second);
        Assert.assertNotEquals(first.get().id(), second.get().id());
        Optional<Course> third = Course.create("TO2");
        Assert.assertTrue(!third.isPresent());
    }

    @Test
    public void findCourseTest() {
        Optional<Course> first = Course.create("TK");
        checkCourse(first);
        Optional<Course> second = Course.findById(first.get().id());
        Assert.assertEquals(first.get(), second.get());
    }

    @Test
    public void enrollStudentTest() {
        Optional<Student> first = Student.create("Kasia", "Kowalska", 700124);
        checkStudent(first);
        Optional<Course> second = Course.create("MOWNIT");
        checkCourse(second);
        Assert.assertTrue(second.get().enrollStudent(first.get()));
        Assert.assertFalse(second.get().enrollStudent(first.get()));
    }

    @Test
    public void courseStudentListTest() {
        Optional<Student> first = Student.create("Adam", "Paciaciak", 800125);
        checkStudent(first);
        Optional<Student> second = Student.create("Jan", "Paciaciak", 800126);
        checkStudent(second);
        Optional<Course> third = Course.create("WDI");
        checkCourse(third);
        Assert.assertTrue(third.get().enrollStudent(first.get()));
        Assert.assertTrue(third.get().enrollStudent(second.get()));
        List<Student> students = third.get().studentList();
        Assert.assertEquals(2, students.size());
        Assert.assertTrue(students.contains(first.get()));
        Assert.assertTrue(students.contains(second.get()));
    }

    @Test
    public void cachedCourseStudentListTest() {
        Optional<Student> first = Student.create("Adam", "Paciaciak", 800125);
        checkStudent(first);
        Optional<Student> second = Student.create("Jan", "Paciaciak", 800126);
        checkStudent(second);
        Optional<Course> third = Course.create("WDI");
        checkCourse(third);
        Assert.assertTrue(third.get().enrollStudent(first.get()));
        Assert.assertTrue(third.get().enrollStudent(second.get()));
        List<Student> students = third.get().cachedStudentsList();
        Assert.assertEquals(2, students.size());
        Assert.assertTrue(students.contains(first.get()));
        Assert.assertTrue(students.contains(second.get()));
        
        List<Student> students2 = third.get().cachedStudentsList();
        Assert.assertEquals(2, students2.size());
        Assert.assertTrue(students2.contains(first.get()));
        Assert.assertTrue(students2.contains(second.get()));
    }
    
    @Test
    public void gradeStudentTest() {
        Optional<Student> first = Student.create("Kasia", "Kowalska", 900124);
        checkStudent(first);
        Optional<Course> second = Course.create("MOWNIT 2");
        checkCourse(second);
        Assert.assertTrue(Grade.gradeStudent(first.get(), second.get(), 5.0f));
    }

    @Test
    public void createReportTest() {
        Optional<Student> first = Student.create("Kasia", "Kowalska", 1000124);
        checkStudent(first);
        Optional<Course> second = Course.create("Bazy");
        checkCourse(second);
        Assert.assertTrue(Grade.gradeStudent(first.get(), second.get(), 5.0f));
        Assert.assertTrue(Grade.gradeStudent(first.get(), second.get(), 4.0f));
        Optional<Course> third = Course.create("Bazy 2");
        checkCourse(third);
        Assert.assertTrue(Grade.gradeStudent(first.get(), third.get(), 5.0f));
        Assert.assertTrue(Grade.gradeStudent(first.get(), third.get(), 3.0f));
        Map<Course, Float> report = first.get().createReport();
        Assert.assertTrue(Float.compare(4.5f, report.get(second.get())) == 0);
        Assert.assertTrue(Float.compare(4.0f, report.get(third.get())) == 0);
    }

    private void checkStudent(final Optional<Student> student) {
        Assert.assertTrue(student.isPresent());
        student.ifPresent(s -> {
            Assert.assertTrue(s.id() > 0);
            Assert.assertNotNull(s.firstName());
            Assert.assertNotNull(s.lastName());
            Assert.assertTrue(s.indexNumber() > 0);
        });
    }

    private void checkCourse(final Optional<Course> course) {
        Assert.assertTrue(course.isPresent());
        course.ifPresent(c -> {
            Assert.assertTrue(c.id() > 0);
            Assert.assertNotNull(c.name());
        });
    }

}

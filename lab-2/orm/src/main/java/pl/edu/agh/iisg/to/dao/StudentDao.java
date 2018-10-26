package pl.edu.agh.iisg.to.dao;

import java.util.*;
import java.util.stream.Collectors;

import pl.edu.agh.iisg.to.model.Course;
import pl.edu.agh.iisg.to.model.Report;
import pl.edu.agh.iisg.to.model.Student;

import javax.persistence.PersistenceException;

public class StudentDao extends GenericDao<Student> {

    public Optional<Student> create(final String firstName, final String lastName, final int indexNumber) {
        Student student = new Student(firstName, lastName, indexNumber);

        try {
            save(student);
        } catch (PersistenceException e) {
            return Optional.empty();
        }

        return Optional.of(student);
    }

    public Optional<Student> findByIndexNumber(final int indexNumber) {
        Student student = currentSession().createQuery(
                "SELECT s FROM Student s WHERE s.indexNumber = :indexNumber",
                Student.class)
                .setParameter("indexNumber", indexNumber)
                .getSingleResult();

        return Optional.ofNullable(student);
    }

    public Map<Course, Float> createReport(final Student student) {
        List<Report> reports = currentSession().createQuery(
                "SELECT NEW pl.edu.agh.iisg.to.model.Report(" +
                        "  c, " +
                        "  (SELECT AVG(g.grade) FROM Grade g WHERE g.course = c AND g.student = s) " +
                        ") " +
                        "FROM Student s " +
                        "JOIN s.courseSet c " +
                        "WHERE s.id = :studentId",
                Report.class)
                .setParameter("studentId", student.id())
                .getResultList();

        return reports.stream().collect(Collectors.toMap(
                report -> report.getCourse(),
                report -> (float) report.getGradeAverage()));
    }

}

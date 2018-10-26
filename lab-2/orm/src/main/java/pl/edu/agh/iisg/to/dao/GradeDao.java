package pl.edu.agh.iisg.to.dao;

import pl.edu.agh.iisg.to.model.Course;
import pl.edu.agh.iisg.to.model.Grade;
import pl.edu.agh.iisg.to.model.Student;

public class GradeDao extends GenericDao<Grade> {

    private final CourseDao courseDao = new CourseDao();

    public boolean gradeStudent(final Student student, final Course course, final float grade) {
        courseDao.enrollStudent(course, student);

        Grade g = new Grade(student, course, grade);
        student.gradeSet().add(g);
        course.gradeSet().add(g);

        save(g);
        return true;
    }


}

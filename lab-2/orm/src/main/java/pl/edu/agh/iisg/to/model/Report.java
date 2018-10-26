package pl.edu.agh.iisg.to.model;

public class Report {
    private final Course course;
    private final double gradeAverage;

    public Report(Course course, double gradeAverage) {
        this.course = course;
        this.gradeAverage = gradeAverage;
    }

    public Course getCourse() { return course;}

    public double getGradeAverage() {
        return gradeAverage;
    }
}

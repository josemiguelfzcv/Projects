
import java.util.List;
import java.util.Set;
//ADT interface
public interface CourseGraphADT {

    /**
     * Adds a course to the network if it does not exist.
     *
     * @param course course name/code
     */
    void addCourse(String course);

    /**
     * Add a relation of prerequisite beteween two courses.
     * course ← prereq
     *
     * @param course Course that need the prerrequisite.
     * @param prereq Course that is the prerrequisite.
     */
    void addPrerequisite(String course, String prereq);

    /**
     * Determines if a student can take a course,
     * given a set of courses they have already completed.
     *
     * @param course           Course to be taken.
     * @param completedCourses Set of courses completed by the student.
     * @return true if the prereq is checked, false if prerequisites are missing.
     */
    boolean canTakeCourse(String course, Set<String> completedCourses);

    /**
     * Returns an ordered list of courses that represent
     * a valid order to take them, respecting the prerequisites.
     * (Topological Order)
     *
     * @return Ordered list of courses.
     */
    List<String> getCourseOrder();
}

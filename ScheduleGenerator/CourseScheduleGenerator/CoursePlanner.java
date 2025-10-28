
import java.util.*;

/**
 * Class responsible for generating the 4-year plan (8 semesters)
 * for a student, respecting prerequisites, availability,
 * professor ratings, and avoiding schedule conflicts.
 */
public class CoursePlanner {
    private DAG graph;                          // Prerequisite graph
    private CourseCatalog catalog;              // Course data catalog
    private int maxCoursesPerSem;               // Maximum courses per semester

    /**
     * Constructor: receives the catalog and the course limit per semester.
     */
    public CoursePlanner(CourseCatalog catalog, int maxCoursesPerSem) {
        this.catalog = catalog;
        this.maxCoursesPerSem = maxCoursesPerSem;
        this.graph = new DAG();
        buildGraphFromCatalog();
    }

    /**
     * Builds the DAG using the prerequisites of each CourseData in the catalog.
     */
    private void buildGraphFromCatalog() {
        for (CourseData cd : catalog.getAllCourses().values()) {
            graph.addCourse(cd.courseCode);
            for (String pre : cd.prerequisites) {
                graph.addPrerequisite(cd.courseCode, pre);
            }
        }
    }

    private static class Slot {
        List<String> days;
        String start;
        String end;
    }

    /**
     * Generates the 8-semester plan given required and elective courses.
     *
     * @param requiredCourses Set of required course codes for the major.
     * @param electiveCourses Set of elective course codes for the major.
     * @param preferredSemesters Map from course code to preferred semester string ("Fall" or "Spring")
     * @return Map semester (1–8) → list of assigned course codes.
     */
    public Map<Integer, List<String>> 
    FourYearPlan(
            Set<String> requiredCourses,
            Set<String> electiveCourses,
            Map<String, String> preferredSemesters) {  // <-- nuevo parámetro

        Map<Integer, List<String>> plan = new HashMap<>();
        Set<String> completed = new HashSet<>();
        List<String> topoOrder = graph.getCourseOrder();
        Set<String> remaining = new LinkedHashSet<>(topoOrder);

        Map<Integer, List<Slot>> semesterSlots = new HashMap<>();

        for (int sem = 1; sem <= 8; sem++) {
            boolean isFall = (sem % 2 == 1);
            List<String> semCourses = new ArrayList<>();
            List<Slot> usedSlots = new ArrayList<>();

            List<CourseData> candidates = new ArrayList<>();
            for (String code : remaining) {
                CourseData cd = catalog.getCourse(code);

                if (!requiredCourses.contains(code) && !electiveCourses.contains(code))
                    continue;

                // Filtrar por oferta semestral
                if ((isFall && !cd.offeredFall) || (!isFall && !cd.offeredSpring))
                    continue;

                // NUEVO: Verificar preferencia de semestre, si existe
                if (preferredSemesters.containsKey(code)) {
                    String pref = preferredSemesters.get(code);
                    // Si la preferencia es "Fall" pero el semestre no es Fall, saltar
                    if (pref.equalsIgnoreCase("Fall") && !isFall) continue;
                    // Si la preferencia es "Spring" pero el semestre no es Spring, saltar
                    if (pref.equalsIgnoreCase("Spring") && isFall) continue;
                }

                if (!graph.canTakeCourse(code, completed))
                    continue;

                candidates.add(cd);
            }

            candidates.sort((a, b) -> {
                double ra = isFall ? a.ratingFall : a.ratingSpring;
                double rb = isFall ? b.ratingFall : b.ratingSpring;
                return Double.compare(rb, ra);
            });

            for (boolean pickRequired : new boolean[]{true, false}) {
                for (CourseData cd : candidates) {
                    if (semCourses.size() >= maxCoursesPerSem) break;

                    if (pickRequired && !requiredCourses.contains(cd.courseCode)) continue;
                    if (!pickRequired
                            && (!electiveCourses.contains(cd.courseCode)
                                || requiredCourses.contains(cd.courseCode)))
                        continue;

                    String[] daysArr = cd.days.split("/");
                    Slot slot = new Slot();
                    slot.days = Arrays.asList(daysArr);
                    slot.start = cd.startTime;
                    slot.end = cd.endTime;

                    boolean conflict = false;
                    for (Slot used : usedSlots) {
                        for (String d : slot.days) {
                            if (used.days.contains(d)
                                && !(slot.end.compareTo(used.start) <= 0
                                     || slot.start.compareTo(used.end) >= 0)) {
                                conflict = true;
                                break;
                            }
                        }
                        if (conflict) break;
                    }
                    if (conflict) continue;

                    semCourses.add(cd.courseCode);
                    usedSlots.add(slot);
                    completed.add(cd.courseCode);
                    remaining.remove(cd.courseCode);
                }
                if (semCourses.size() >= maxCoursesPerSem) break;
            }

            plan.put(sem, semCourses);
            semesterSlots.put(sem, usedSlots);
        }

        return plan;
    }
}


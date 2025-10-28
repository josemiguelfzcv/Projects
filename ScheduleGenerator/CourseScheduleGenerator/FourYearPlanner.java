
import java.util.*;

public class FourYearPlanner {
    private CourseCatalog catalog;

    public FourYearPlanner(CourseCatalog catalog) {
        this.catalog = catalog;
    }

    public Map<String, List<CourseData>> generatePlan(
            Set<String> preferFall, Set<String> preferSpring,
            Set<Integer> semestersWithFiveCourses,
            Set<String> alreadyTaken,
            int semestersCompleted) {

        Map<String, List<CourseData>> semesters = new LinkedHashMap<>();
        String[] semesterLabels = {"Fall 1", "Spring 1", "Fall 2", "Spring 2", "Fall 3", "Spring 3", "Fall 4", "Spring 4"};

        Map<String, CourseData> allCourses = new HashMap<>();
        for (CourseData course : catalog.getAllCourses().values()) {
            allCourses.put(course.courseCode, course);
        }

        Set<String> taken = new HashSet<>(alreadyTaken);
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> prereqToCourses = new HashMap<>();

        for (CourseData course : allCourses.values()) {
            String code = course.courseCode;
            List<String> prereqs = course.prerequisites != null ? course.prerequisites : new ArrayList<>();
            int count = 0;
            for (String pre : prereqs) {
                pre = pre.trim();
                prereqToCourses.computeIfAbsent(pre, k -> new ArrayList<>()).add(code);
                if (!taken.contains(pre)) count++;
            }
            inDegree.put(code, count);
        }

        List<String> ready = new ArrayList<>();
        for (String code : allCourses.keySet()) {
            if (!taken.contains(code) && inDegree.getOrDefault(code, 0) == 0) {
                ready.add(code);
            }
        }

        int semesterIndex = semestersCompleted;

        while (!ready.isEmpty() && semesterIndex < 8) {
            int limit = semestersWithFiveCourses.contains(semesterIndex) ? 5 : 4;
            String semesterLabel = semesterLabels[semesterIndex];
            List<CourseData> semesterCourses = semesters.computeIfAbsent(semesterLabel, k -> new ArrayList<>());

            boolean progress = false;

            int csCountThisSemester = 0;
            int maxCSThisSemester = (semesterIndex == 0 || semesterIndex == 1) ? 1 : 2;

            List<String> readyCS = new ArrayList<>();
            List<String> readyOthers = new ArrayList<>();
            for (String code : ready) {
                if (code.startsWith("CS ")) readyCS.add(code);
                else readyOthers.add(code);
            }
            ready.clear();

            List<String> notAssignedCS = new ArrayList<>();
            List<String> notAssignedOthers = new ArrayList<>();
            Set<String> takenBeforeSemester = new HashSet<>(taken);

            while ((readyCS.size() > 0 || readyOthers.size() > 0) && semesterCourses.size() < limit) {
                String code;
                boolean takeCS = false;

                if (!readyCS.isEmpty() && csCountThisSemester < maxCSThisSemester) {
                    code = readyCS.remove(0);
                    takeCS = true;
                } else if (!readyOthers.isEmpty()) {
                    code = readyOthers.remove(0);
                } else {
                    code = null;
                    break;
                }

                CourseData course = allCourses.get(code);
                if (course == null) continue;

                boolean offered = (semesterIndex % 2 == 0 && course.offeredFall) || (semesterIndex % 2 == 1 && course.offeredSpring);
                boolean matchesPref =
                        (preferFall.contains(code) && semesterIndex % 2 == 0) ||
                        (preferSpring.contains(code) && semesterIndex % 2 == 1) ||
                        (!preferFall.contains(code) && !preferSpring.contains(code));

                boolean prereqsCompleted = true;
                for (String pre : course.prerequisites) {
                    if (!takenBeforeSemester.contains(pre)) {
                        prereqsCompleted = false;
                        break;
                    }
                }

                if (offered && matchesPref && prereqsCompleted) {
                    semesterCourses.add(course);
                    taken.add(code);
                    progress = true;

                    if (takeCS) csCountThisSemester++;

                    List<String> dependents = prereqToCourses.getOrDefault(code, new ArrayList<>());
                    for (String dependent : dependents) {
                        int deg = inDegree.getOrDefault(dependent, 0);
                        if (deg > 0) {
                            deg--;
                            inDegree.put(dependent, deg);
                            if (deg == 0 && !taken.contains(dependent)) {
                                if (dependent.startsWith("CS ")) readyCS.add(dependent);
                                else readyOthers.add(dependent);
                            }
                        }
                    }
                } else {
                    if (takeCS) notAssignedCS.add(code);
                    else notAssignedOthers.add(code);
                }
            }

            ready.addAll(notAssignedCS);
            ready.addAll(notAssignedOthers);
            ready.addAll(readyCS);
            ready.addAll(readyOthers);

            semesterIndex++;
        }

        for (String label : semesterLabels) {
            semesters.putIfAbsent(label, new ArrayList<>());
        }

        // 🔢 Graduation requirements: division, writing intensive, DPE
        int div1Needed = 3;
        int div2Needed = 3;
        int wiNeeded = 2;
        int dpeNeeded = 1;

        // 🧮 Count already taken or planned courses
        for (List<CourseData> courseList : semesters.values()) {
            for (CourseData course : courseList) {
                if (course.division == 1) div1Needed--;
                else if (course.division == 2) div2Needed--;
                if (course.isWritingIntensive) wiNeeded--;
                if (course.isDPE) dpeNeeded--;
            }
        }
        for (String takenCode : alreadyTaken) {
            CourseData c = allCourses.get(takenCode);
            if (c != null) {
                if (c.division == 1) div1Needed--;
                else if (c.division == 2) div2Needed--;
                if (c.isWritingIntensive) wiNeeded--;
                if (c.isDPE) dpeNeeded--;
            }
        }

        // ➕ Fill empty slots with division, WI and DPE courses
        for (int i = semestersCompleted; i < semesterLabels.length; i++) {
            String semesterLabel = semesterLabels[i];
            List<CourseData> semesterCourses = semesters.get(semesterLabel);
            if (semesterCourses == null) continue;

            int limit = semestersWithFiveCourses.contains(i) ? 5 : 4;

            while (semesterCourses.size() < limit && (div1Needed > 0 || div2Needed > 0 || wiNeeded > 0 || dpeNeeded > 0)) {
                CourseData filler = null;

                for (CourseData candidate : allCourses.values()) {
                    if (taken.contains(candidate.courseCode)) continue;
                    if (semesterCourses.contains(candidate)) continue;

                    boolean offered = (i % 2 == 0 && candidate.offeredFall) || (i % 2 == 1 && candidate.offeredSpring);
                    if (!offered) continue;

                    if (div1Needed > 0 && candidate.division == 1) {
                        filler = candidate;
                        div1Needed--;
                        break;
                    }
                    if (div2Needed > 0 && candidate.division == 2) {
                        filler = candidate;
                        div2Needed--;
                        break;
                    }
                    if (wiNeeded > 0 && candidate.isWritingIntensive) {
                        filler = candidate;
                        wiNeeded--;
                        break;
                    }
                    if (dpeNeeded > 0 && candidate.isDPE) {
                        filler = candidate;
                        dpeNeeded--;
                        break;
                    }
                }

                if (filler != null) {
                    semesterCourses.add(filler);
                    taken.add(filler.courseCode);
                } else {
                    break;
                }
            }
        }

        return semesters;
    }
}




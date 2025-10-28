
import java.util.*;

/**
 * Concrete implementation of CourseGraphADT using an adjacency list.
 * This graph is directed and acyclic, designed to model prerequisite relationships.
 */
public class DAG implements CourseGraphADT {

    // Map representing the adjacency list: each course has a list of dependent courses.
    private Map<String, List<String>> adjacencyList;

    // Map to keep track of how many prerequisites each course has
    private Map<String, Integer> inDegree;

    /**
     * Constructor: Initializes empty maps.
     */
    public DAG() {
        adjacencyList = new HashMap<>();
        inDegree = new HashMap<>();
    }

    /**
     * Adds a course to the network if it is not already present.
     *
     * @param course Name of the course to be added.
     */
    @Override
    public void addCourse(String course) {
        // Only added if it does not exist
        if (!adjacencyList.containsKey(course)) {
            adjacencyList.put(course, new ArrayList<>());
            inDegree.put(course, 0); // No prerequisites at start-up
        }
    }

    /**
     * Adds a prerequisite edge to the network:
     * prereq → course
     *
     * @param course Course that depends on the prerequisite.
  *    @param prereq Course prerequisite.
     */
    @Override
    public void addPrerequisite(String course, String prereq) {
        // We ensure that both courses exist in the network.
        addCourse(course);
        addCourse(prereq);

        // We add a prereq edge to course
        if (!adjacencyList.get(prereq).contains(course)) {
            adjacencyList.get(prereq).add(course);

            // We increase the prerequisite counter of 'course'.
            inDegree.put(course, inDegree.get(course) + 1);
        }
    }

    /**
     * Checks if a student can take a course given a set of completed courses.
     * For that, all direct prerequisites must be in completedCourses.
     *
     * @param course Course to be checked.
     * @param completedCourses Courses that the student has already passed.
     * @return true if they can take it, false if at least one prerequisite is missing.
     */
    @Override
    public boolean canTakeCourse(String course, Set<String> completedCourses) {
        // We check all nodes that have ‘course’ as destination
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            String prereq = entry.getKey();
            List<String> dependents = entry.getValue();

            if (dependents.contains(course) && !completedCourses.contains(prereq)) {
                // If a prerequisite is missing, you cannot take the course.
                return false;
            }
        }
        return true; // All prerequisites are complete
    }

    /**
     * Perform a topological ordering of the graph.
     * We use Kahn's Algorithm to make sure we respect the prerequisites.
     *
     * @return Sorted list of courses in valid order.
     */
    @Override
    public List<String> getCourseOrder() {
        List<String> order = new ArrayList<>();                     // Final result
        Queue<String> queue = new LinkedList<>();                   // Queue to go through the courses

        // We copy the inDegree map so as not to alter the original one.
        Map<String, Integer> inDegreeCopy = new HashMap<>(inDegree);

        // We add to the queue courses without prerequisites
        for (String course : inDegreeCopy.keySet()) {
            if (inDegreeCopy.get(course) == 0) {
                queue.offer(course);
            }
        }

        // 
        while (!queue.isEmpty()) {
            String current = queue.poll();        // We have a course
            order.add(current);                   // We add it to the final order

            for (String neighbor : adjacencyList.get(current)) {
                // Reducing the inDegree of dependent courses
                inDegreeCopy.put(neighbor, inDegreeCopy.get(neighbor) - 1);

                // If you no longer have any pending prerequisites, we add you to the queue.
                if (inDegreeCopy.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Check if there were cycles (which should not happen in a DAG).
        if (order.size() != adjacencyList.size()) {
            throw new RuntimeException("The graph contains a cycle!");
        }

        return order;
    }

    public void removeCourse(String course) {
        if (!adjacencyList.containsKey(course)) return;

        // We remove the course from the adjacency lists of other courses.
        for (List<String> neighbors : adjacencyList.values()) {
        neighbors.remove(course);
        }

        // We remove the course from the network
        adjacencyList.remove(course);
        inDegree.remove(course);

        // We also reduced inDegree from the courses that depended on this
        for (String key : inDegree.keySet()) {
            int count = 0;
            for (String neighbor : adjacencyList.getOrDefault(key, new ArrayList<>())) {
                if (neighbor.equals(course)) count++;
            }
            inDegree.put(key, inDegree.get(key) - count);
        }
    }

    public void removePrerequisite(String course, String prereq) {
        if (adjacencyList.containsKey(prereq) && adjacencyList.get(prereq).contains(course)) {
            adjacencyList.get(prereq).remove(course);
            inDegree.put(course, inDegree.get(course) - 1);
        }
    }

    public Set<String> getAllPrerequisites(String course) {
        Set<String> visited = new HashSet<>();
        // DFS recursion
        getAllPrerequisitesHelper(course, visited);
        return visited;
    }

    private void getAllPrerequisitesHelper(String course, Set<String> visited) {
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            String prereq = entry.getKey();
            if (entry.getValue().contains(course) && !visited.contains(prereq)) {
                visited.add(prereq);
                getAllPrerequisitesHelper(prereq, visited);
            }
        }
    }   


    public List<String> getNextCourses(String course) {
        // Courses that directly require this course as a prerequisite
        return adjacencyList.getOrDefault(course, new ArrayList<>());
    }


    public int totalCourses() {
        return adjacencyList.size();
    }


    public boolean containsCourse(String course) {
        return adjacencyList.containsKey(course);
    }
    
    public boolean wouldCreateCycle(String course, String prereq) {
        Set<String> visited = new HashSet<>();
        return hasPath(prereq, course, visited);
    }
    
    private boolean hasPath(String start, String target, Set<String> visited) {
        if (start.equals(target)) return true;
        if (!adjacencyList.containsKey(start) || visited.contains(start)) return false;
    
        visited.add(start);
        for (String neighbor : adjacencyList.get(start)) {
            if (hasPath(neighbor, target, visited)) return true;
        }
        return false;
    }

}

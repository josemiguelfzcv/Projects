
import java.io.*;
import java.util.*;

public class CourseCatalog {
    private Map<String, CourseData> catalog = new HashMap<>();

    public void loadFromCSV(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                List<String> cols = parseCSVLine(line);

                if (cols.size() < 14) {
                    System.err.println("Ignored line (columns < 14): " + line);
                    continue;
                }

                String code = cols.get(0).trim();
                String name = cols.get(1).trim();
                List<String> prereqs = cols.get(2).isEmpty()
                    ? new ArrayList<>()
                    : Arrays.asList(cols.get(2).split(";"));

                int level = Integer.parseInt(cols.get(3));
                int division = Integer.parseInt(cols.get(4));
                boolean wi = Boolean.parseBoolean(cols.get(5));
                boolean dpe = Boolean.parseBoolean(cols.get(6));
                double rf = Double.parseDouble(cols.get(7));
                double rs = Double.parseDouble(cols.get(8));
                boolean fall = Boolean.parseBoolean(cols.get(9));
                boolean spring = Boolean.parseBoolean(cols.get(10));
                String days = cols.get(11);
                String startTime = cols.get(12);
                String endTime = cols.get(13);

                CourseData cd = new CourseData(
                    code, name, prereqs,
                    fall, spring, rf, rs,
                    division, wi, dpe,
                    days, startTime, endTime
                );

                catalog.put(code, cd);
            }
        }

    }

    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim()); // last field
        return fields;
    }

    public CourseData getCourse(String code) {
        return catalog.get(code);
    }

    public Map<String, CourseData> getAllCourses() {
        return catalog;
    }
}



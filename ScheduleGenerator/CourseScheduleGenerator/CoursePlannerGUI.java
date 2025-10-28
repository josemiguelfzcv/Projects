import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CoursePlannerGUI extends JFrame {
    private final CourseCatalog catalog;
    private final JTextArea outputArea;
    private final JComboBox<String> courseSelector;
    private final JComboBox<String> semesterSelector;
    private final DefaultListModel<String> preferredCoursesModel;
    private final JCheckBox fiveCoursesInSemesterCheckBox;
    private final JPanel progressPanel;

    public CoursePlannerGUI() {
        super("Course Planner");
        this.catalog = new CourseCatalog();
        try {
            catalog.loadFromCSV("sample_courses_with_seasons.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLayout(new BorderLayout());

        // Top panel for preferences
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        courseSelector = new JComboBox<>();
        catalog.getAllCourses().keySet().stream().sorted().forEach(courseSelector::addItem);

        semesterSelector = new JComboBox<>(new String[]{"Fall", "Spring"});

        JButton addPreferenceButton = new JButton("Add Preference");
        preferredCoursesModel = new DefaultListModel<>();
        JList<String> preferredCoursesList = new JList<>(preferredCoursesModel);
        addPreferenceButton.addActionListener(e -> {
            String course = (String) courseSelector.getSelectedItem();
            String semester = (String) semesterSelector.getSelectedItem();
            if (course != null && semester != null) {
                String entry = course + " - " + semester;
                if (!preferredCoursesModel.contains(entry)) {
                    preferredCoursesModel.addElement(entry);
                }
            }
        });

        fiveCoursesInSemesterCheckBox = new JCheckBox("Take 5 courses in specific semester");

        topPanel.add(new JLabel("Select Course:"));
        topPanel.add(courseSelector);
        topPanel.add(new JLabel("Preferred Semester:"));
        topPanel.add(semesterSelector);
        topPanel.add(addPreferenceButton);
        topPanel.add(fiveCoursesInSemesterCheckBox);

        add(topPanel, BorderLayout.NORTH);

        // Center panel for output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for action
        JButton generatePlanButton = new JButton("Generate 4-Year Plan");
        generatePlanButton.addActionListener(e -> generatePlan());
        add(generatePlanButton, BorderLayout.SOUTH);

        // Side panel for preferred course list and graduation progress
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        sidePanel.add(new JLabel("Preferred Courses"));
        JScrollPane courseScroll = new JScrollPane(preferredCoursesList);
        courseScroll.setPreferredSize(new Dimension(250, 200));
        sidePanel.add(courseScroll);

        // Graduation Progress
        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createTitledBorder("Graduation Progress"));
        sidePanel.add(progressPanel);

        add(sidePanel, BorderLayout.EAST);

        setVisible(true);
    }

    private void updateProgressPanel(Set<String> completedCourses) {
        int div1 = 0, div2 = 0, div3 = 0, writing = 0, dpe = 0;

        for (String code : completedCourses) {
            CourseData cd = catalog.getCourse(code);
            if (cd != null) {
                switch (cd.division) {
                    case 1:
                        div1++;
                        break;
                    case 2:
                        div2++;
                        break;
                    case 3:
                        div3++;
                        break;
                }
                if (cd.isWritingIntensive) writing++;
                if (cd.isDPE) dpe++;
            }
        }

        progressPanel.removeAll();
        progressPanel.add(new JLabel("Division I: " + div1 + "/3"));
        progressPanel.add(new JLabel("Division II: " + div2 + "/3"));
        progressPanel.add(new JLabel("Division III: " + div3 + "/3"));
        progressPanel.add(new JLabel("Writing Intensive: " + writing + "/2"));
        progressPanel.add(new JLabel("DPE: " + dpe + "/1"));
        progressPanel.revalidate();
        progressPanel.repaint();
    }

    private void generatePlan() {
        outputArea.setText("");

        String[] years = {"Freshman", "Sophomore", "Junior", "Senior"};
        String currentYear = (String) JOptionPane.showInputDialog(
                this,
                "What year are you choosing classes for?",
                "Select your year",
                JOptionPane.QUESTION_MESSAGE,
                null,
                years,
                years[0]
        );
        if (currentYear == null) return;

        int semestersCompleted;
        switch (currentYear) {
            case "Freshman":
                semestersCompleted = 0;
                break;
            case "Sophomore":
                semestersCompleted = 2;
                break;
            case "Junior":
                semestersCompleted = 4;
                break;
            case "Senior":
                semestersCompleted = 6;
                break;
            default:
                semestersCompleted = 0;
        }

        Set<String> completedCourses = new HashSet<>();
        Set<Integer> semestersWithFive = new HashSet<>();
        if (semestersCompleted > 0) {
            String[] pastLabels = {"Fall 1", "Spring 1", "Fall 2", "Spring 2", "Fall 3", "Spring 3", "Fall 4", "Spring 4"};
            JPanel panel = new JPanel(new GridLayout(semestersCompleted, 1));
            panel.setPreferredSize(new Dimension(400, semestersCompleted * 50));
            JCheckBox[] boxes = new JCheckBox[semestersCompleted];
            for (int i = 0; i < semestersCompleted; i++) {
                boxes[i] = new JCheckBox(pastLabels[i]);
                panel.add(boxes[i]);
            }
            int resp = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(panel),
                    "Select semesters where you took 5 courses",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (resp != JOptionPane.OK_OPTION) return;

            for (int i = 0; i < semestersCompleted; i++) {
                if (boxes[i].isSelected()) semestersWithFive.add(i);
            }

            int totalCourses = 0;
            for (int i = 0; i < semestersCompleted; i++) {
                totalCourses += semestersWithFive.contains(i) ? 5 : 4;
            }
            JPanel selPanel = new JPanel(new GridLayout(totalCourses, 2, 5, 5));
            selPanel.setPreferredSize(new Dimension(500, totalCourses * 35));
            List<JComboBox<String>> selectors = new ArrayList<>();
            String[] allCodes = catalog.getAllCourses().keySet().toArray(new String[0]);

            for (int sem = 0; sem < semestersCompleted; sem++) {
                int slots = semestersWithFive.contains(sem) ? 5 : 4;
                for (int j = 1; j <= slots; j++) {
                    selPanel.add(new JLabel(pastLabels[sem] + " — course " + j + ":"));
                    JComboBox<String> box = new JComboBox<>(allCodes);
                    selPanel.add(box);
                    selectors.add(box);
                }
            }
            int resp2 = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(selPanel),
                    "Select all previously taken courses",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (resp2 != JOptionPane.OK_OPTION) return;
            for (JComboBox<String> box : selectors) {
                if (box.getSelectedItem() != null) {
                    completedCourses.add(((String) box.getSelectedItem()).trim());
                }
            }
        }

        Map<String, String> prefs = new HashMap<>();
        for (int i = 0; i < preferredCoursesModel.getSize(); i++) {
            String entry = preferredCoursesModel.get(i);
            String[] parts = entry.split(" - ");
            if (parts.length == 2) prefs.put(parts[0], parts[1]);
        }
        Set<String> prefFall = new HashSet<>();
        Set<String> prefSpring = new HashSet<>();
        prefs.forEach((c, s) -> {
            if ("Fall".equalsIgnoreCase(s)) prefFall.add(c);
            else if ("Spring".equalsIgnoreCase(s)) prefSpring.add(c);
        });

        Set<Integer> extraFive = new HashSet<>();
        if (fiveCoursesInSemesterCheckBox.isSelected()) {
            String[] opts = {"Fall 1", "Spring 1", "Fall 2", "Spring 2", "Fall 3", "Spring 3", "Fall 4", "Spring 4"};
            String sel = (String) JOptionPane.showInputDialog(
                    this,
                    "Select semester for 5 courses:",
                    "Choose Semester",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opts,
                    opts[0]
            );
            if (sel != null) extraFive.add(Arrays.asList(opts).indexOf(sel));
        }

        FourYearPlanner planner = new FourYearPlanner(catalog);
        Map<String, List<CourseData>> plan = planner.generatePlan(
                prefFall, prefSpring, extraFive, completedCourses, semestersCompleted
        );

        String[] labels = {"Fall 1", "Spring 1", "Fall 2", "Spring 2", "Fall 3", "Spring 3", "Fall 4", "Spring 4"};
        for (String lbl : labels) {
            outputArea.append(lbl + ":\n");
            for (CourseData cd : plan.getOrDefault(lbl, Collections.emptyList())) {
                outputArea.append("  - " + cd.courseCode + ": " + cd.courseName + "\n");
            }
            outputArea.append("\n");
        }

        updateProgressPanel(completedCourses);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CoursePlannerGUI::new);
    }
}




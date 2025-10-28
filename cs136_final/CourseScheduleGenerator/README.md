# cs136_final

This project is an interactive tool for generating a four-year academic plan for Williams College students, with a strong focus on Computer Science (CS) course sequencing. It takes into account prerequisites, semester availability, course load limits, and graduation requirements. The tool was developed by Jane Su and Jose Miguel as the final project for CS136.

## Key Features

- **Prerequisite-aware planning**: Uses a Directed Acyclic Graph (DAG) to ensure all courses are scheduled only after their prerequisites are fulfilled.
- **Semester-aware scheduling**: Considers whether each course is offered in Fall, Spring, or both.
- **Balanced course load**: Distributes 4–5 courses per semester (user-configurable).
- **CS course prioritization**: Strategically schedules Computer Science courses, limiting early-semester overloads and respecting user preferences.
- **Graduation requirement tracking**:
  - At least 3 Division I courses
  - At least 3 Division II courses
  - Writing Intensive (W) requirement
  - Difference, Power, and Equity (DPE) requirement
- **Support for previously completed courses**: Users can indicate which courses they have already taken.
- **Preference-aware planning**: Users can specify if they prefer to take certain courses in Fall or Spring only.

## How to Run the Program

1. **Download** the files and put them in a folder, then from that folder do the next steps.

2. **Create a `bin` directory** to store compiled `.class` files:

```bash
mkdir bin
```

3. **Compile the project** using the following command:

```bash
javac *.java
```

4. **Run the GUI**:

```bash
java CoursePlannerGUI
```

## File Overview

- **CourseData.java** – Represents each course and stores attributes like prerequisites, division, DPE/W status, semester availability, etc.
- **CourseCatalog.java** – Loads and parses the CSV file to create `CourseData` objects.
- **CourseGraphADT.java** – Abstract interface for the prerequisite graph.
- **DAG.java** – Implements a Directed Acyclic Graph to model course dependencies.
- **CoursePlanner.java** – Core logic that schedules courses based on prerequisites, ratings, and conflicts.
- **FourYearPlanner.java** – Generates the eight-semester plan while checking division, W, and DPE graduation requirements.
- **CoursePlannerGUI.java** – User interface that allows course selection, displays preferences, and tracks graduation progress.

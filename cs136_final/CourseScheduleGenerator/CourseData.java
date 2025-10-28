
import java.util.List;

public class CourseData {
    public String courseCode;
    public String courseName;
    public List<String> prerequisites;
    public boolean offeredFall;
    public boolean offeredSpring;
    public double ratingFall;
    public double ratingSpring;
    public int division;
    public boolean isWritingIntensive;
    public boolean isDPE;

    // ─── NEW FIELDS ──────────────────────
    public String days;       // p.ej. "Mon/Wed/Fri"
    public String startTime;  // p.ej. "09:00"
    public String endTime;    // p.ej. "10:15"
    // ────────────────────────────────────────

    public CourseData(String courseCode, String courseName, List<String> prerequisites,
                      boolean offeredFall, boolean offeredSpring,
                      double ratingFall, double ratingSpring,
                      int division, boolean isWritingIntensive, boolean isDPE,
                      // ─── NEW PARAMETERS───────────────
                      String days, String startTime, String endTime
                      // ────────────────────────────────────
    ) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.prerequisites = prerequisites;
        this.offeredFall = offeredFall;
        this.offeredSpring = offeredSpring;
        this.ratingFall = ratingFall;
        this.ratingSpring = ratingSpring;
        this.division = division;
        this.isWritingIntensive = isWritingIntensive;
        this.isDPE = isDPE;
        // ─── ASSIGNMENTS ──────────────────────
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
        // ───────────────────────────────────────
    }

      public String getCode() {
      return courseCode;
  }

  public String getName() {
      return courseName;
  }

  public boolean isOfferedFall() {
      return offeredFall;
  }

  public boolean isOfferedSpring() {
      return offeredSpring;
  }

}

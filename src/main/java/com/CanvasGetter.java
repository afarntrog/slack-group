package com;

import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.TestLauncher;
import edu.ksu.canvas.interfaces.AccountReader;
import edu.ksu.canvas.interfaces.AssignmentReader;
import edu.ksu.canvas.interfaces.CourseReader;
import edu.ksu.canvas.interfaces.QuizReader;
import edu.ksu.canvas.interfaces.SubmissionReader;
import edu.ksu.canvas.model.Account;
import edu.ksu.canvas.model.Course;
import edu.ksu.canvas.model.assignment.Assignment;
import edu.ksu.canvas.oauth.NonRefreshableOauthToken;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.GetSingleAssignmentOptions;
import edu.ksu.canvas.requestOptions.GetSubmissionsOptions;
import edu.ksu.canvas.requestOptions.ListCourseAssignmentsOptions;
import edu.ksu.canvas.requestOptions.ListCurrentUserCoursesOptions;
import edu.ksu.canvas.requestOptions.ListUserAssignmentOptions;
import edu.ksu.canvas.requestOptions.ListCurrentUserCoursesOptions.Include;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CanvasGetter {

    private static final Logger LOG = LoggerFactory.getLogger(TestLauncher.class);

    private String canvasUrl = "https://touro.instructure.com/";
    String CANVAS_AUTH_TOKEN;
    private OauthToken oauthToken;
    private CanvasApiFactory canvasApiFactory;


    public CanvasGetter() {
        this.CANVAS_AUTH_TOKEN = System.getenv("CANVAS_AUTH_TOKEN");
        this.oauthToken = new NonRefreshableOauthToken(CANVAS_AUTH_TOKEN);
        this.canvasApiFactory = new CanvasApiFactory(canvasUrl);
    }

    
    public CanvasGetter(String canvasAuthToken) {
        this.CANVAS_AUTH_TOKEN = canvasAuthToken;
        this.oauthToken = new NonRefreshableOauthToken(CANVAS_AUTH_TOKEN);
        this.canvasApiFactory = new CanvasApiFactory(canvasUrl);
    }

    // This is not working.
    public void getRootAccount() throws IOException {
        AccountReader acctReader = canvasApiFactory.getReader(AccountReader.class, oauthToken);
        Account rootAccount = acctReader.getSingleAccount("1").get();
        LOG.info("Got account from Canvas: " + rootAccount.getName());
    }

    public List<Course> getCourses() throws IOException {
        CourseReader courseReader = canvasApiFactory.getReader(CourseReader.class, oauthToken);
        return courseReader.listCurrentUserCourses(new ListCurrentUserCoursesOptions().includes(Arrays.asList(Include.TERM)));
    }
    
    public List<Assignment> getAssignments(Course course) throws IOException {
        AssignmentReader assignmentReader = canvasApiFactory.getReader(AssignmentReader.class, oauthToken);

        String courseId = Integer.toString(course.getId());
        return assignmentReader.listCourseAssignments(new ListCourseAssignmentsOptions(courseId));
    }

    public String getOwnCourses() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        List<Course> myCourses = getCourses();
        List<Assignment> assignments;
        LOG.info("Got " + myCourses.size() + " courses back from Canvas: ");
        
        for(Course course : myCourses) {
            LOG.info("  " + course.getName());

            assignments = getAssignments(course);
            for (Assignment v : assignments) {
                LOG.info(v.getName());
                stringBuilder.append(v.getName());
                LOG.info(String.valueOf(v.getDueAt()));
                stringBuilder.append(v.getDueAt());
            }
        }
        return stringBuilder.toString();
    }

    public String getUpcomingAssignments() throws IOException {
        ArrayList<String> upcomingAssignments = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        List<Course> myCourses = getCourses();
        List<Assignment> assignments;

        LOG.info("Got " + myCourses.size() + " courses back from Canvas: ");
        
        for(Course course : myCourses) {
            LOG.info("  " + course.getName());

            assignments = getAssignments(course);
            int i = 1;
            for (Assignment as : assignments) {
                Date date = as.getDueAt();
                if (date != null) {
                    Date today = new Date();
                    if (today.before(date)) {   // not yet due.
                        if (i == 1) {           // if first in course, append course name
                            LOG.info("appending course name: " + course.getName());
                            stringBuilder.append("\n\n\n:notebook_with_decorative_cover: *"
                                    + course.getName() + ":* \n \n");
                        }
                        LOG.info("formatting assignment: " + as.getName());
                        stringBuilder.append(formatAssignment(as, i++));
                    }
                }
            }
        }

        if (stringBuilder.length() == 0) {      // if no assignments due
            stringBuilder.append(getNoAssignmentsDueString());
        }

        return stringBuilder.toString();
    }


    public String getNumberedListOfCourses() throws IOException {
        /*
            Return a formatted numbered list of courses. User can use it to choose a course.
         */
        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for (Course course : getCourses()) {
            stringBuilder.append("\n\n\n:notebook_with_decorative_cover: *)" + (i++) + course.getName() + ":* \n \n");
        }
        return stringBuilder.toString();
    }



    public String getAssignmentsForCourse(int courseNumber) throws IOException {
        /*
            Return a formatted list of assignments for an upcoming course.
         */
        Course course = getCourse(courseNumber);
        if (course != null)
            return getFormattedAssignments(getAssignments(course));
        return "";
    }

    private String getFormattedAssignments(List<Assignment> assignments) {
        if (assignments.size() == 0) return getNoAssignmentsDueString();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < assignments.size(); i++) {
            stringBuilder.append(formatAssignment(assignments.get(i), (i+1)));
        }
        return stringBuilder.toString();
    }

    public Course getCourse(int courseNumber) throws IOException {
        int i = 1;
        for (Course course : getCourses()) {
            if (i++ == courseNumber)
                return course;
        }
        return null;
    }

    private String formatAssignment(Assignment as, int i) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, hh:mm a z");
        //formatter.setTimeZone(TimeZone.getDefault());
        String formattedAssignment =
                "\t\t:memo:  *" + i + ")* " + as.getName()
                + "\n\n\t\t:stopwatch:  *Due date:* "
                + formatter.format(as.getDueAt())
                + "\n\n";
        return formattedAssignment;
    }

    private String getNoAssignmentsDueString() {
        return ":tada::balloon::confetti_ball:".repeat(4)
                + "\n:confetti_ball:" + " ".repeat(58)
                + ":tada:\n:balloon:" + " ".repeat(10)
                + "You're all caught up! :smiley:"
                + " ".repeat(7) + ":balloon:\n"
                + ":confetti_ball:" + " ".repeat(58)
                + ":tada:\n" + ":tada::balloon::confetti_ball:".repeat(4);
    }
}
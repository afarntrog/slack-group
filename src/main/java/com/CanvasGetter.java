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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> upcomingAssignments = new ArrayList<>();

        List<Course> myCourses = getCourses();
        List<Assignment> assignments;

        LOG.info("Got " + myCourses.size() + " courses back from Canvas: ");
        
        for(Course course : myCourses) {
            LOG.info("  " + course.getName());

            assignments = getAssignments(course);
            for (Assignment v : assignments) {
                Date date = v.getDueAt();
                if (date != null) {
                    Date today = new Date();
                    if (today.before(date)) {   // not yet due.
                        upcomingAssignments.add(v.getName() + "\n" + v.getDueAt() + "\n\n");
                    }
                }
            }
        }

        // Add all the assignments to the string builder.
        for (int i = 0; i < upcomingAssignments.size(); i++) {
            stringBuilder.append(i + 1).append(")").append(upcomingAssignments.get(i));
        }
        return stringBuilder.toString();
    }
}
package com;

import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.TestLauncher;
import edu.ksu.canvas.interfaces.AccountReader;
import edu.ksu.canvas.interfaces.AssignmentReader;
import edu.ksu.canvas.interfaces.CourseReader;
import edu.ksu.canvas.model.Account;
import edu.ksu.canvas.model.Course;
import edu.ksu.canvas.model.assignment.Assignment;
import edu.ksu.canvas.oauth.NonRefreshableOauthToken;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.ListCourseAssignmentsOptions;
import edu.ksu.canvas.requestOptions.ListCurrentUserCoursesOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CanvasGetter {

    private static final Logger LOG = LoggerFactory.getLogger(TestLauncher.class);

    private String canvasUrl = "https://touro.instructure.com/";
    String StringOauthToken = "";
    private OauthToken oauthToken = new NonRefreshableOauthToken(StringOauthToken);

    
    public void getRootAccount() throws IOException {
        CanvasApiFactory apiFactory = new CanvasApiFactory(canvasUrl);
        AccountReader acctReader = apiFactory.getReader(AccountReader.class, oauthToken);
        Account rootAccount = acctReader.getSingleAccount("1").get();

        LOG.info("Got account from Canvas: " + rootAccount.getName());
    }

    public String getOwnCourses() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        CanvasApiFactory apiFactory = new CanvasApiFactory(canvasUrl);
        CourseReader courseReader = apiFactory.getReader(CourseReader.class, oauthToken);
        AssignmentReader ar = apiFactory.getReader(AssignmentReader.class, oauthToken); //af

        List<Course> myCourses = courseReader.listCurrentUserCourses(new ListCurrentUserCoursesOptions());
        LOG.info("Got " + myCourses.size() + " courses back from Canvas: ");
        for(Course course : myCourses) {
            for (Assignment v : ar.listCourseAssignments(new ListCourseAssignmentsOptions(String.valueOf(course.getId())))) {
                LOG.info(v.getName());
                stringBuilder.append(v.getName());
                LOG.info(String.valueOf(v.getDueAt()));
                stringBuilder.append(v.getDueAt());
            }
            LOG.info("  " + course.getName());
        }
        return stringBuilder.toString();
    }

    private void setOAuthToken(String oAuthToken) {
        this.StringOauthToken = oAuthToken;
    }
}


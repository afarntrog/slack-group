package com;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void main(String[] args) throws Exception {
        // App expects env variables (SLACK_BOT_TOKEN, SLACK_SIGNING_SECRET)
        App app = new App();



        // name your command here.
        app.command("/helloworld", (req, ctx) -> {
            //String allCoursesDescription
            CanvasGetter launcher = new CanvasGetter();
            // read this input
            new Thread(() -> {
                try {
                   ctx.respond(launcher.getOwnCourses());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            System.out.println("THREAD+++++++ " + Thread.activeCount());
            //TimeUnit.SECONDS.sleep(1);
            //ctx.respond();
            return ctx.ack();
        });

        // name your command here.
        app.command("/up-as", (req, ctx) -> {
            CanvasGetter launcher = new CanvasGetter();
            // launch thread to get upcoming assignments.
            new Thread(() -> {
                try {
                    ctx.respond(launcher.getUpcomingAssignments());
                    System.out.println("DONE");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("THREAD+++++++ " + Thread.activeCount());
            return ctx.ack();
        });

        SlackAppServer server = new SlackAppServer(app);
        server.start(); // http://localhost:3000/slack/events
    }






}
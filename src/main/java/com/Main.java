package com;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
import com.slack.api.model.block.composition.TextObject;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

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
import java.lang.ClassNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.net.URI;
import java.sql.*;

public class Main {

    public static Map<String, String> environment = System.getenv();
    static String databaseURL  = environment.get("JDBC_DATABASE_URL");
    static String databaseUsername = environment.get("JDBC_DATABASE_USERNAME");
    static String databasePassword = environment.get("JDBC_DATABASE_PASSWORD");


    public static Connection establishConnection() {
        try {
            Class.forName("org.postgresql.Driver");    
        } catch(ClassNotFoundException e) {
            System.out.println("Something has gone wrong with the database driver");
            System.exit(1);    
        }

        try {
            return DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);
        } catch(SQLException e) {
           e.printStackTrace();
        }

        return null;
    }

    public static void saveUserInformation(String userId, String canvasAccessToken) {
        Connection conn = establishConnection();
        PreparedStatement stmt = null;

        // save user information
        try {
            String query = "INSERT INTO Users VALUES(?,?);";
            stmt = conn.prepareStatement(query);

            stmt.setString(1, userId);
            stmt.setString(2, canvasAccessToken);

            stmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCanvasTokenFromUserId(String userId) {
        Connection conn = establishConnection();
        
        String sql = "SELECT * FROM USERS WHERE userid = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            rs.next();
            return rs.getString(2); // canvas authentication token
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static CanvasGetter setupCanvasGetter(String userId) {
        String canvasAuthToken = getCanvasTokenFromUserId(userId);
        return new CanvasGetter(canvasAuthToken);
    }

    public static void main(String[] args) throws Exception {
        App app = new App();

        // display usage information and instructions here
        app.command("/helloworld", (req, ctx) -> {
            return ctx.ack(asBlocks(
                section(s -> s.text(markdownText(":wave: from SlackCan!"))),
                section(s -> s.text(markdownText("Our goal is to place Canvas's most important information at a student's fingertips, right in Slack."))),
                
                divider(),
                
                section(s -> s.text(markdownText(":memo: To setup your account, first follow our basic tutorial on getting an access token from Canvas's dashboard. Once you have it, come back here."))),
                section(s -> s.text(markdownText(":one: Ok, now you can run /canvas-authenticate <token> (put your canvas token after the slash command)"))),
                section(s -> s.text(markdownText(":two: That's all it takes! You're officially connected to Canvas now. Below are a few commands you can get started with."))),

                divider(),

                section(s -> s.text(markdownText("/*helloworld* - To get this message again"))),
                section(s -> s.text(markdownText("/*canvas-authenticate* - To connect to Canvas (you only have to do it once!)"))),
                section(s -> s.text(markdownText("/*upcoming-assignments* - get all upcoming assignments")))
            ));
        });


        app.command("/authenticate-canvas", (req, ctx) -> {
            String userId = req.getPayload().getUserId();
            String canvasAccessToken = req.getPayload().getText();

            // We need to acknowledge the user's command within 3000 ms, (3 seconds),
            // so we'll do these operations completely independent from the ctx.ack (acknowledgement)
            new Thread(() -> saveUserInformation(userId, canvasAccessToken)).start();

            return ctx.ack("We've received your token. You should be able to make requests now.");
            });
            

        app.command("/upcoming-assignments", (req, ctx) -> {
            // launch thread to get upcoming assignments.
            new Thread(() -> {
                try {
                    CanvasGetter canvasGetter = setupCanvasGetter(req.getPayload().getUserId());
                    String upcomingAssignments = canvasGetter.getUpcomingAssignments();
                    ctx.respond(asBlocks(
                            section(s -> s.text(markdownText(":clipboard: *Here are your upcoming assignments:*"))),

                            divider(),

                            section(s -> s.text(markdownText(upcomingAssignments)))
                    ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("THREAD+++++++ " + Thread.activeCount());
            return ctx.ack("We're getting the info now...");
        });


        int port = Integer.parseInt(environment.get("PORT"));
        SlackAppServer server = new SlackAppServer(app, port);
        server.start();
    }
}
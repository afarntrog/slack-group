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
import java.util.Map;
import java.net.URI;
import java.sql.*;


public class Main {
    
    public static Map<String, String> environment = System.getenv();

    public static void saveUserInformation(String userId, String canvasAccessToken) {
        // get authentication information for database
        String databaseURL  = environment.get("JDBC_DATABASE_URL");
        String databaseUsername = environment.get("JDBC_DATABASE_USERNAME");
        String databasePassword = environment.get("JDBC_DATABASE_PASSWORD");
        
        // establish connection to database
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException e) {
            System.exit(1);
        }

        try {
            conn = DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);
        } catch(SQLException e) {
            e.printStackTrace();
        }

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

    public static void main(String[] args) throws Exception {
        App app = new App();
        
        app.command("/helloworld", (req, ctx) -> {
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

        app.command("/authenticate-canvas", (req, ctx) -> {
            String userId = req.getPayload().getUserId();
            String canvasAccessToken = req.getPayload().getText();
            
            // We need to acknowledge the user's command within 3000 ms, (3 seconds), 
            // so we'll do these operations completely independent from the ctx.ack (acknowledgement)
            new Thread(() -> {
                saveUserInformation(userId, canvasAccessToken);
            }).start();

            return ctx.ack("We've received your token. You should be able to make requests now.");
            });
            
        int port = Integer.parseInt(environment.get("PORT"));
        SlackAppServer server = new SlackAppServer(app, port);
        server.start(); 
}






}
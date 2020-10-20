package com;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
import edu.ksu.canvas.TestLauncher;
import edu.ksu.canvas.oauth.NonRefreshableOauthToken;
import edu.ksu.canvas.oauth.OauthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;


public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(TestLauncher.class);

    private String canvasUrl;
    private OauthToken oauthToken;


    public static void main(String[] args) throws Exception {
        // App expects env variables (SLACK_BOT_TOKEN, SLACK_SIGNING_SECRET)
        App app = new App();


        String canvasUrl = "https://touro.instructure.com/";
        String oauthToken = "";
        OauthToken sOautToken = new NonRefreshableOauthToken(oauthToken);
        TestLauncher launcher = new TestLauncher(canvasUrl, oauthToken);
        // name your command here.
        app.command("/helloworld", (req, ctx) -> {
            // read this input
            new Thread(() -> {
                try {
                    launcher.getOwnCourses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            System.out.println("THREAD+++++++ " + Thread.activeCount());
            //TimeUnit.SECONDS.sleep(1);
            ctx.respond("dsjlf");
            return ctx.ack();
        });

        SlackAppServer server = new SlackAppServer(app);
        server.start(); // http://localhost:3000/slack/events
    }
}
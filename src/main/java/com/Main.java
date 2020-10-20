package com;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
import edu.ksu.canvas.TestLauncher;
import edu.ksu.canvas.oauth.OauthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(TestLauncher.class);

    private String canvasUrl;
    private OauthToken oauthToken;


    public static void main(String[] args) throws Exception {
        // App expects env variables (SLACK_BOT_TOKEN, SLACK_SIGNING_SECRET)
        App app = new App();


        String canvasUrl = "https://touro.instructure.com/";
        String oauthToken = "10898~w5xtL2u6BExK3Ab4KWbLKl8USzH5Mdw2lCldqbuStBZhWiRJvwOgcOz4olf9u6rY\n";
        TestLauncher launcher = new TestLauncher(canvasUrl, oauthToken);
        // name your command here.
        app.command("/helloworld", (req, ctx) -> {
            try {
                launcher.getOwnCourses();
            } catch(Exception e) {
                System.out.println("Error");
            }
            try {
                launcher.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //TimeUnit.SECONDS.sleep(1);
            ctx.respond("dsjlf");
            return ctx.ack();
        });

        SlackAppServer server = new SlackAppServer(app);
        server.start(); // http://localhost:3000/slack/events
    }
}
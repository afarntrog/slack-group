package com;

import org.junit.Test;
import static org.junit.Assert.*;

import com.slack.api.bolt.context.builtin.SlashCommandContext;

public class TestMain {
    @Test
    public void testEstablishConnectionReturnsNullConnectionForNullDatabaseURL() {
        java.sql.Connection conn = Main.establishConnection();
        assertNull(conn);
    }

    @Test
    public void testSaveUserInformationUsesCorrectResponseForSuccessfulOperation() {
        Main.saveUserInformation("U01DN1LCYEA", "10898~59dEJ3WL5f25MgGgDemzcQvvtzG23b8sDcl9stx2bI1K9Y9HzJ1WgOP21rmup0ag", new SlashCommandContext());
        assertEquals(":heavy_check_mark: We have successfully saved your token!", Main.response);
    }

    @Test 
    public void testSaveUserInformationUsesCorrectResponseForUnsuccessfulOperation() {
        Main.saveUserInformation("wrong user identifcation", "10898~59dEJ3WL5f25MgGgDemzcQvvtzG23b8sDcl9stx2bI1K9Y9HzJ1WgOP21rmup0ag", new SlashCommandContext());
        assertEquals("hmm.. we're having trouble saving your token. \n\nWe'll notify our developers immediately", Main.response);
    }

    @Test 
    public void testGetCanvasTokenFromUserIdReturnsEmptyStringForIncorrectUserId() {
        String canvasToken = Main.getCanvasTokenFromUserId("wrong user id");
        assertEquals("", canvasToken);
    }

    @Test 
    public void testGetCanvasTokenFromUserIdReturnsCorrectTokenForCorrectUserId() {
        String canvasToken = Main.getCanvasTokenFromUserId("U01DN1LCYEA");
        assertEquals("10898~59dEJ3WL5f25MgGgDemzcQvvtzG23b8sDcl9stx2bI1K9Y9HzJ1WgOP21rmup0ag", canvasToken);
    }

    @Test 
    public void testSetupCanvasGetterReturnsNullForIncorrectUserId() {
        CanvasGetter canvasGetter = Main.setupCanvasGetter("wrong user id");
        assertNull(canvasGetter);
    }
}

package de.kgs.vertretungsplan.loader.network;

import android.accounts.NetworkErrorException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import de.kgs.vertretungsplan.loader.exceptions.ContentNotProvidedException;
import de.kgs.vertretungsplan.loader.exceptions.CredentialException;
import de.kgs.vertretungsplan.loader.exceptions.DownloadException;
import de.kgs.vertretungsplan.storage.Credentials;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MoodleBridgeTest extends MoodleBridge {

    //////////////////////////////////////////
    private static final String username = "";
    private static final String password = "";
    //////////////////////////////////////////


    private final Credentials credentials = Credentials.getInstance();


    @BeforeClass
    public static void buildUp() {
    }

    @AfterClass
    public static void tearDown() {

    }

    @Before
    public void before() {
        credentials.setMoodleCookie("");
    }

    @Test
    public void testSessionTestInvalidSessionKey() {
        assertFalse(testSession("Random session key"));
    }

    @Test
    public void testSessionTestValidKey() throws CredentialException, IOException, NetworkErrorException {

        String sessionKey = performLogin(username, password);
        System.out.println("SessionKey is " + sessionKey);
        assertTrue(testSession(sessionKey));

    }

    @Ignore("Utility")
    @Test
    public void testSessionKey() {
        assertTrue(testSession("MoodleSession=31680i71ppcqdjc6na2rkrugb5"));
    }

    @Test
    public void testCreateSessionMultipleLogins() {

        try {
            createSession(username, password);
            createSession(username, password);
        } catch (IOException | CredentialException | NetworkErrorException e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void testCreateSession() {
        try {
            createSession(username, password);
        } catch (IOException | CredentialException | NetworkErrorException e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = CredentialException.class)
    public void testCreateSessionInvalidUsername() throws CredentialException {
        try {
            createSession("IrgendeinNutzer", password);
        } catch (IOException | NetworkErrorException e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = CredentialException.class)
    public void testCreateSessionInvalidPassword() throws CredentialException {
        try {
            createSession(username, "DasIstBestimmtNichtDasPasswort");
        } catch (IOException | NetworkErrorException e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testDownLoadResources() throws CredentialException, IOException, NetworkErrorException, DownloadException, ContentNotProvidedException {

        createSession(username, password);
        String s = downloadResources("https://moodle-s.kgs.hd.bw.schule.de/moodle/pluginfile.php/3054/mod_resource/content/2/subst_001.htm");
        System.out.println(s);

    }

}

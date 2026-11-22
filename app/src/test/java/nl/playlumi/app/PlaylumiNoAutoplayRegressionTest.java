package nl.playlumi.app;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

/** Guard against accidentally reintroducing forced spoken questions. */
public final class PlaylumiNoAutoplayRegressionTest {
    @Test public void noAutomaticReadLogicCanSurviveAnUpdate() throws Exception {
        Path source=Paths.get("src/main/java/nl/playlumi/app/MainActivity.java");
        if(!Files.exists(source))
            source=Paths.get("app/src/main/java/nl/playlumi/app/MainActivity.java");
        assertTrue("App source must be available for this regression gate",Files.exists(source));
        String code=new String(Files.readAllBytes(source),java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(code.contains("readThisPage("));
        assertFalse(code.contains("auto_read"));
        assertTrue(code.contains("Geluidseffecten: aan"));
        assertTrue(code.contains("effects_on\",false"));
        assertTrue(code.contains("Test de voorleesstem"));
        assertTrue(code.contains("Lees de vraag voor"));
    }
}

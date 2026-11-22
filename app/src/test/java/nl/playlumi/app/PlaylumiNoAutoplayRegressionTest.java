package nl.playlumi.app;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public final class PlaylumiNoAutoplayRegressionTest {
    @Test public void humanSpeechIsAbsentFromRunningApp() throws Exception {
        Path src=Paths.get("src/main/java/nl/playlumi/app/");
        if(!Files.exists(src))src=Paths.get("app/src/main/java/nl/playlumi/app/");
        String m=new String(Files.readAllBytes(src.resolve("MainActivity.java")),StandardCharsets.UTF_8);
        String a=new String(Files.readAllBytes(src.resolve("PlaylumiAudio.java")),StandardCharsets.UTF_8);
        assertFalse(m.contains("sound.speak("));
        assertFalse(m.contains("sound.stopSpeaking("));
        assertFalse(m.contains("auto_read"));
        assertFalse(m.contains("voorleesstem"));
        assertFalse(a.contains("TextToSpeech"));
        assertFalse(a.contains("speech.speak("));
        assertFalse(a.contains("ToneGenerator"));
    }
    @Test public void jinglesComeBackByDefault() throws Exception {
        Path src=Paths.get("src/main/java/nl/playlumi/app/MainActivity.java");
        if(!Files.exists(src))src=Paths.get("app/src/main/java/nl/playlumi/app/MainActivity.java");
        String m=new String(Files.readAllBytes(src),StandardCharsets.UTF_8);
        assertTrue(m.contains("effects_on\",true"));
        assertTrue(m.contains("Deuntjes en geluidjes: aan"));
        assertTrue(m.contains("Speel een verrassingsdeuntje"));
    }
}

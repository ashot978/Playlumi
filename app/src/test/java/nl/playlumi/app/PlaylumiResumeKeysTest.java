package nl.playlumi.app;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
public final class PlaylumiResumeKeysTest {
    @Test public void everyAgeAndGameGetsAnIndependentSnapshot(){
        Set<String> keys=new HashSet<>();
        for(int age=0;age<3;age++){
            assertTrue(keys.add(PlaylumiResumeKeys.last(age)));
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age))
                assertTrue(keys.add(PlaylumiResumeKeys.session(age,e.id)));
        }
        assertNotEquals(PlaylumiResumeKeys.session(0,"0:6:0:0"),PlaylumiResumeKeys.session(2,"0:6:0:0"));
    }
    @Test public void startedQuizIsResumableBeforeTheFirstAnswer(){
        assertTrue(PlaylumiResumeKeys.validQuizProgress(0,0,false,8));
        assertTrue(PlaylumiResumeKeys.validQuizProgress(0,1,false,8));
        assertTrue(PlaylumiResumeKeys.validQuizProgress(1,2,true,8));
        assertTrue(PlaylumiResumeKeys.validQuizProgress(7,10,false,8));
        assertFalse(PlaylumiResumeKeys.validQuizProgress(0,0,true,8));
        assertFalse(PlaylumiResumeKeys.validQuizProgress(8,8,true,8));
        assertFalse(PlaylumiResumeKeys.validQuizProgress(3,2,false,8));
        assertFalse(PlaylumiResumeKeys.validQuizProgress(-1,0,false,8));
        assertFalse(PlaylumiResumeKeys.validQuizProgress(0,-1,false,8));
        assertFalse(PlaylumiResumeKeys.validQuizProgress(0,0,false,0));
    }
    @Test public void keysDoNotChangeWhenReopeningTheSameGame(){
        assertEquals("library_resume_v2_1_0:6:0:0",PlaylumiResumeKeys.session(1,"0:6:0:0"));
    }
}

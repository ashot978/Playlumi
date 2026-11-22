package nl.playlumi.app;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/** Answer listening never selects an answer, nor reads eliminated options. */
public final class PlaylumiAnswerAudioTest {
    private static final List<String> COLORS=Arrays.asList("rood","blauw","groen","geel");

    @Test public void eachChoiceGetsAnUnambiguousLabelWithoutChangingQuestions() {
        LearningGame.Round round=new LearningGame.Round(
                "Welke kleur zie je?","●",0xffaabbcc,COLORS,
                Arrays.asList(1,2,3,4),2);
        for(int index=0;index<round.answers.size();index++) {
            assertEquals("Antwoord "+(index+1)+": "+COLORS.get(index)+".",
                    PlaylumiAnswerAudio.oneChoice(round.answers,index,0));
            assertFalse(round.isSolved());
            assertEquals(2,round.correctIndex);
        }
        assertEquals(4,round.answers.size());
        assertTrue(round.choose(2));
    }

    @Test public void readsOnlyVisibleChoicesInOriginalScreenOrder() {
        String first=PlaylumiAnswerAudio.allChoices(COLORS,0);
        assertTrue(first.indexOf("Antwoord 1: rood")<
                first.indexOf("Antwoord 4: geel"));
        String partial=PlaylumiAnswerAudio.allChoices(COLORS,(1<<0)|(1<<3));
        assertTrue(partial.contains("Antwoord 2: blauw"));
        assertTrue(partial.contains("Antwoord 3: groen"));
        assertFalse(partial.contains("rood"));
        assertFalse(partial.contains("geel"));
    }

    @Test public void supportsActualGameAnswerShapesAcrossAgeLevels() {
        LearningGame game=new LearningGame(new java.util.Random(42));
        int[] modes={0,1,2,3,4,5,7,12,21};
        for(int mode:modes)for(int level=0;level<3;level++) {
            game.configure(mode,0,level);
            LearningGame.Round round=game.round();
            String all=PlaylumiAnswerAudio.allChoices(round.answers,0);
            for(int i=0;i<round.answers.size();i++) {
                assertTrue(all.contains(
                        PlaylumiAnswerAudio.oneChoice(round.answers,i,0)));
            }
        }
    }

    @Test public void normalizesLineBreaksForSpeech() {
        assertEquals("Antwoord 1: a b.",PlaylumiAnswerAudio.oneChoice(
                Arrays.asList("a\nb","c"),0,0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void hiddenAnswerCannotBeReadAlone() {
        PlaylumiAnswerAudio.oneChoice(COLORS,1,1<<1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsBadMask() {
        PlaylumiAnswerAudio.allChoices(COLORS,1<<9);
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsOnlyOneRemainingChoice() {
        PlaylumiAnswerAudio.allChoices(COLORS,0b0111);
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsEmptyLabels() {
        PlaylumiAnswerAudio.allChoices(Arrays.asList("kat"," "),0);
    }
}

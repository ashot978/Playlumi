package nl.playlumi.app;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiPatternsTest {
    @Test public void allTiersHaveUniqueSolutionsAndDifferentLengths(){
        Random random=new Random(93);
        for(int age=0;age<3;age++)for(boolean shapes:new boolean[]{false,true}){
            Set<Integer> lengths=new HashSet<>();boolean middle=false,end=false;
            for(int i=0;i<500;i++){
                LearningGame.Round round=PlaylumiPatterns.next(age,i%2,shapes,random);
                String[] symbols=PlaylumiPatterns.tokens(round.display);
                assertTrue(PlaylumiPatterns.isVisualPattern(round.display));
                lengths.add(symbols.length);
                if(age==2)assertTrue(symbols.length>=8);
                int[] seen=new int[symbols.length];int missing=-1;
                for(int j=0;j<symbols.length;j++){
                    if("?".equals(symbols[j])){seen[j]=-1;missing=j;}
                    else seen[j]=shapes?Arrays.asList(LearningGame.SHAPE_SYMBOLS).indexOf(symbols[j]):PlaylumiPatterns.colorIndex(symbols[j]);
                }
                assertTrue(missing>=0);
                middle|=missing<symbols.length-1;end|=missing==symbols.length-1;
                String answer=round.answers.get(round.correctIndex);
                int expected=shapes?Arrays.asList(LearningGame.SHAPE_SYMBOLS).indexOf(answer):PlaylumiPatterns.colorIndex(answer);
                assertEquals(expected,PlaylumiPatterns.infer(seen,age==0?3:age==1?4:5));
                assertEquals(round.answers.size(),new HashSet<>(round.answers).size());
            }
            assertTrue(lengths.size()>1);assertTrue(end);if(age>0)assertTrue(middle);
        }
    }
    @Test public void olderPatternIsNotAlwaysTheMiddleColor(){
        Random random=new Random(19);int different=0;
        for(int i=0;i<100;i++){
            LearningGame.Round r=PlaylumiPatterns.next(2,0,false,random);
            String[] parts=PlaylumiPatterns.tokens(r.display);
            if(PlaylumiPatterns.colorIndex(parts[1])!=PlaylumiPatterns.colorIndex(r.answers.get(r.correctIndex)))different++;
        }
        assertTrue(different>30);
    }
}

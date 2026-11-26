package nl.playlumi.app;
import org.junit.Test;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public final class PlaylumiNumberChallengesTest {
    @Test public void sequencesHaveVisibleRulesAndNearbyUniqueAnswers(){
        Random random=new Random(94);
        for(int age=0;age<3;age++){
            Set<Integer> lengths=new HashSet<>();boolean interior=false,end=false;
            for(int i=0;i<500;i++){
                LearningGame.Round round=PlaylumiNumberChallenges.sequence(age,i%2==0?1:-1,i%3==0,age==2&&i%2==0,random);
                String[] terms=round.display.split("→");
                lengths.add(terms.length);
                int missing=-1;int[] values=new int[terms.length];
                for(int j=0;j<terms.length;j++){
                    String term=terms[j].trim();
                    if(term.equals("?")){assertEquals(-1,missing);missing=j;term=round.answers.get(round.correctIndex);}
                    values[j]=Integer.parseInt(term);assertTrue(values[j]>0);
                }
                assertTrue(missing>=0);interior|=missing<terms.length-1;end|=missing==terms.length-1;
                int first=values[1]-values[0],second=values[2]-values[1];
                for(int j=1;j<values.length;j++)assertEquals(j%2==1?first:second,values[j]-values[j-1]);
                assertEquals(round.answers.size(),new HashSet<>(round.answers).size());
                for(String option:round.answers)assertTrue(Math.abs(Integer.parseInt(option)-values[missing])<=round.answers.size());
            }
            assertTrue(lengths.size()>1);assertTrue(interior);assertTrue(end);
        }
    }
    @Test public void measurementUsesActualUnitsAndStableButtons(){
        Random random=new Random(1094);
        Set<String> solutions=new HashSet<>();
        for(int i=0;i<500;i++){
            LearningGame.Round r=PlaylumiNumberChallenges.measure(2,random);
            String line=r.display.split("\n")[0].replace("←","").replace("→","");
            String[] sides=line.split("\\|");
            int left=millimeters(sides[0]),right=millimeters(sides[1]);
            String expected=left>right?"Links":left<right?"Rechts":"Gelijk";
            assertEquals(expected,r.answers.get(r.correctIndex));
            assertEquals("Links",r.answers.get(0));assertEquals("Rechts",r.answers.get(1));
            assertTrue(r.display.contains("1 cm = 10 mm"));solutions.add(expected);
        }
        assertEquals(3,solutions.size());
    }
    private static int millimeters(String label){
        String[] parts=label.trim().split("\\s+");
        return Integer.parseInt(parts[0])*(parts[1].equals("cm")?10:1);
    }
}

package nl.playlumi.app;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Independent arithmetic/counting oracles plus semantic regression cases for the 0.108 routes. */
public final class ThinkingPracticeCheck {
    private static int checks,semanticChecks,restores;
    private static void require(boolean okay,String message){if(!okay)throw new AssertionError(message);}
    private static int glyphCount(String text){
        int n=0;
        for(int index=0;index<text.length();){
            int cp=text.codePointAt(index);index+=Character.charCount(cp);
            if(!Character.isWhitespace(cp)&&!Character.isSpaceChar(cp))n++;
        }
        return n;
    }
    private static int glyph(String text){
        for(int at=0;at<text.length();){int cp=text.codePointAt(at);at+=Character.charCount(cp);
            if(!Character.isWhitespace(cp)&&!Character.isSpaceChar(cp))return cp;}
        throw new AssertionError("no picture");
    }
    private static void valid(LearningGame.Round round){
        require(!round.isSolved(),"fresh question already solved");
        require(round.answers.size()==3||round.answers.size()==4,"unexpected choice count");
        require(round.answers.size()==new HashSet<>(round.answers).size(),"duplicate answers");
        require(round.answers.size()==round.answerColors.size(),"answer color count");
        require(round.correctIndex>=0&&round.correctIndex<round.answers.size(),"missing answer");
    }
    private static void saveRound(LearningGame.Round q,int mode,int variant,int age){
        LearningGame restored=new LearningGame(new Random(108));restored.configure(mode,variant,age);
        int wrong=(q.correctIndex+1)%q.answers.size();
        require(!q.choose(wrong),"wrong answer accepted");
        restored.restoreRound(q.prompt,q.display,q.displayColor,q.answers,q.answerColors,
                q.correctIndex,q.fingerCount,false,q.rejectedMask());
        LearningGame.Round fresh=restored.round();
        require(q.display.equals(fresh.display)&&q.answers.equals(fresh.answers),"restored content changed");
        require(fresh.isRejected(wrong)&&!fresh.choose(wrong),"restored rejection lost");
        require(fresh.choose(fresh.correctIndex)&&!fresh.choose(fresh.correctIndex),"restore duplicate credit");
        require(!PlaylumiQuizCoach.explain(mode,variant,fresh).isEmpty(),"missing solved explanation");
        restores++;
    }
    private static void equalGroups(){
        for(int age=0;age<3;age++){
            LearningGame game=new LearningGame(new Random(10800+age));
            game.configure(ExtraGames.COMPARE,PlaylumiThinkingPractice.EQUAL_GROUPS,age);
            Set<Integer> amounts=new HashSet<>(),shapeKinds=new HashSet<>();
            for(int i=0;i<1200;i++){
                LearningGame.Round q=game.round();valid(q);
                int expected=glyphCount(q.display),matches=0;
                require(expected>=1&&expected<=new int[]{3,6,10}[age],"quantity age bound");
                amounts.add(expected);shapeKinds.add(glyph(q.display));
                for(int k=0;k<q.answers.size();k++){
                    String answer=q.answers.get(k);
                    require(glyph(q.display)!=glyph(answer),"equivalence became identical-copy task");
                    require(answer.split("\\n").length<=2,"answer too tall");
                    if(glyphCount(answer)==expected){matches++;require(k==q.correctIndex,"wrong amount accepted");}
                }
                require(matches==1,"ambiguous equal amounts");
                require(PlaylumiQuizCoach.explain(ExtraGames.COMPARE,5,q).isEmpty(),"answer explained before solving");
                if(i%40==0)saveRound(q,ExtraGames.COMPARE,5,age);
                checks++;game.nextRound();
            }
            require(amounts.size()==new int[]{3,6,10}[age]&&shapeKinds.size()==5,"quantity coverage");
        }
    }
    private static void placeValues(){
        for(int age=0;age<3;age++){
            LearningGame game=new LearningGame(new Random(11800+age));
            game.configure(ExtraGames.MATH,PlaylumiThinkingPractice.PLACE_VALUE,age);
            Set<Integer> numbers=new HashSet<>();boolean zeroUnits=false;
            for(int i=0;i<1600;i++){
                LearningGame.Round q=game.round();valid(q);
                Matcher digits=Pattern.compile("\\d+").matcher(q.display);
                require(digits.find(),"missing tens");int tens=Integer.parseInt(digits.group());
                require(digits.find(),"missing units");int units=Integer.parseInt(digits.group());
                require(!digits.find()&&tens>=1&&tens<=new int[]{1,4,9}[age]&&units>=0&&units<=9,"invalid place value");
                // Positional notation, rather than any helper supplied by the generator, is the oracle.
                int expected=Integer.parseInt(Integer.toString(tens)+units);
                require(Integer.parseInt(q.answers.get(q.correctIndex))==expected,"tens/units composed incorrectly");
                numbers.add(expected);zeroUnits|=units==0;
                require(PlaylumiQuizCoach.explain(ExtraGames.MATH,8,q).isEmpty(),"early place-value explanation");
                if(i%40==0){
                    saveRound(q,ExtraGames.MATH,8,age);q.choose(q.correctIndex);
                    require(PlaylumiQuizCoach.explain(ExtraGames.MATH,8,q).equals(tens+" × 10 + "+units+" = "+expected+"."),"wrong place-value explanation");
                }
                checks++;game.nextRound();
            }
            require(numbers.size()==new int[]{10,40,90}[age]&&zeroUnits,"place-value coverage");
        }
    }
    private static void stateStories(){
        Set<String> fullPool=new HashSet<>();
        for(int age=0;age<3;age++){
            LearningGame game=new LearningGame(new Random(12800+age));
            game.configure(PlaylumiThinkingPractice.DAILY_THINGS,PlaylumiThinkingPractice.AFTER,age);
            Set<String> stories=new HashSet<>();
            for(int i=0;i<800;i++){
                LearningGame.Round q=game.round();valid(q);stories.add(q.display);fullPool.add(q.display);
                String answer=q.answers.get(q.correctIndex);
                require(q.display.length()<=90&&q.display.split("\\n").length==2,"story readability bound");
                for(String choice:q.answers)require(choice.length()<=27,"long story answer");
                // Noncommutative actions: these regression cases check the final state, not a copied word.
                if(q.display.contains("op de stoel, dan onder de tafel")){
                    require(answer.equals("Bal onder de tafel"),"sequence uses intermediate location");semanticChecks++;
                }else if(q.display.contains("alleen de rode bal weg")){
                    require(answer.equals("De blauwe bal blijft"),"removal forgot unchanged item");semanticChecks++;
                }else if(q.display.contains("uit en daarna weer aan")){
                    require(answer.equals("De lamp geeft licht"),"last light action ignored");semanticChecks++;
                }else if(q.display.contains("open en daarna weer dicht")){
                    require(answer.equals("Gesloten deur"),"last door action ignored");semanticChecks++;
                }else if(q.display.contains("alleen je muts af")){
                    require(answer.equals("Jas aan, muts af"),"unchanged coat was removed");semanticChecks++;
                }else if(q.display.contains("Je haalt alle blokken los")){
                    require(answer.equals("Losse blokken"),"disassembly removed material");semanticChecks++;
                }else if(q.display.contains("Je tekent er een boom naast")){
                    require(answer.equals("Huis en boom"),"adding erased existing house");semanticChecks++;
                }else if(q.display.contains("Je scheurt het in twee stukken")){
                    require(answer.equals("Twee stukken papier"),"partition wrong");semanticChecks++;
                }
                if(i%40==0)saveRound(q,19,5,age);
                checks++;game.nextRound();
            }
            require(stories.size()==new int[]{8,12,24}[age],"story age coverage");
        }
        require(fullPool.size()==24&&semanticChecks>200,"semantic story coverage");
    }
    private static void routes(){
        for(int age=0;age<3;age++){
            Set<String> routes=new HashSet<>();
            for(PlaylumiGameCatalog.Entry entry:PlaylumiGameCatalog.entries(age))require(routes.add(entry.id),"duplicate catalog ID");
            require(routes.contains("0:0:11:5"),"equal groups unavailable");
            require(routes.contains("0:0:12:8")== (age==2),"place value wrong age");
            require(routes.contains("4:0:19:5")== (age>=1),"story wrong age");
            require(PlaylumiScores.CATEGORIES==24&&LearningGame.GAME_NAMES.length==24,"category migration");
        }
    }
    public static void main(String[] args){
        equalGroups();placeValues();stateStories();routes();
        System.out.println("PASS: "+checks+" quantity/place-value/sequence questions; 24 reviewed story contexts; "+semanticChecks+" causal-state assertions; "+restores+" exact new-route restorations.");
    }
}

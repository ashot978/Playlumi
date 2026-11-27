package nl.playlumi.app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Independent counting, calendar and literal-text oracles for the 0.110 curriculum. */
public final class FoundationsCheck {
    private static final String[] DAYS={"maandag","dinsdag","woensdag","donderdag","vrijdag","zaterdag","zondag"};
    private static final String[] NEW_IDS={"0:0:0:5","0:0:16:5","1:0:6:5"};
    // Sorted id=title pairs from the released 0.109 catalog, before these routes were added.
    private static final String[] LEGACY_HASHES={
        "9ae844e52f0aef98a02744804c16a9346d381a28a0c07e7b3b9d2fdf453ba1f8",
        "e195f1c66b7bf6e886de00110c6987aebc8340d05150808dc8c9cd28435da022",
        "e738e7ca592521481beef58728f24649ff4d9c1d0dd2d5e2e7c3edb5bef6432e"
    };
    private static int counting,calendar,reading,restores,rollovers;

    private static void require(boolean okay,String message){if(!okay)throw new AssertionError(message);}
    private static void basic(LearningGame.Round q,int choices){
        require(q!=null&&!q.isSolved()&&!q.prompt.isEmpty()&&!q.display.isEmpty(),"empty or solved question");
        require(q.answers.size()==choices&&new HashSet<>(q.answers).size()==choices,"ambiguous choices");
        require(q.answerColors.size()==choices&&q.correctIndex>=0&&q.correctIndex<choices,"missing answer");
        require(q.letterBlank()==-1&&q.fingerCount==0,"unrelated renderer marker");
        for(String answer:q.answers)require(answer!=null&&!answer.trim().isEmpty(),"blank answer");
    }
    private static void restore(LearningGame.Round q,int mode,int level){
        String before=q.display;
        int wrong=(q.correctIndex+1)%q.answers.size();
        require(!q.choose(wrong)&&q.isRejected(wrong),"wrong answer accepted");
        LearningGame restored=new LearningGame(new Random(110));restored.configure(mode,5,level);
        restored.restoreRound(q.prompt,q.display,q.displayColor,q.answers,q.answerColors,q.correctIndex,0,false,q.rejectedMask());
        LearningGame.Round open=restored.round();
        require(q.prompt.equals(open.prompt)&&q.display.equals(open.display)&&q.answers.equals(open.answers),"saved content changed");
        require(open.isRejected(wrong)&&!open.choose(wrong),"rejected answer forgotten");
        require(PlaylumiQuizCoach.explain(mode,5,open).isEmpty(),"restored open answer revealed");
        require(open.choose(open.correctIndex)&&!open.choose(open.correctIndex),"duplicate answer credit");
        String explanation=PlaylumiQuizCoach.explain(mode,5,open);
        require(!explanation.isEmpty()&&open.visibleDisplay().equals(before),"solved feedback or display changed");
        restored.restoreRound(open.prompt,open.display,open.displayColor,open.answers,open.answerColors,
                open.correctIndex,0,true,open.rejectedMask());
        LearningGame.Round solved=restored.round();
        require(solved.isSolved()&&!solved.choose(solved.correctIndex)&&solved.isRejected(wrong),"solved restore state lost");
        require(PlaylumiQuizCoach.explain(mode,5,solved).equals(explanation),"restored explanation changed");
        restores++;
    }
    private static void counting(){
        for(int age=0;age<3;age++){
            Set<Integer> quantities=new HashSet<>();Set<Character> shapes=new HashSet<>();
            for(int seed=0;seed<24;seed++){
                LearningGame game=new LearningGame(new Random(0x5deece66dL*(seed+1)+age));
                game.configure(LearningGame.COUNT,5,age);
                for(int trial=0;trial<80;trial++){
                    LearningGame.Round q=game.round();basic(q,age==0?3:4);
                    Matcher target=Pattern.compile("\\(([●▲■◆★♥])\\)").matcher(q.prompt);
                    require(target.find(),"no visible target shape");char symbol=target.group(1).charAt(0);
                    require(!target.find(),"two targets");
                    String allowed=age==0?"●▲■":age==1?"●▲■◆★":"●▲■◆★♥";
                    int expected=0,total=0;
                    for(char glyph:q.display.toCharArray())if(!Character.isWhitespace(glyph)&&!Character.isSpaceChar(glyph)){
                        require(allowed.indexOf(glyph)>=0,"unexpected shape or young-tier vocabulary");
                        total++;if(glyph==symbol)expected++;
                    }
                    require(expected>= (age==0?1:0)&&expected<=new int[]{3,6,10}[age],"target quantity age bound");
                    require(total>expected&&total<=new int[]{5,9,15}[age],"missing distractors or crowded field");
                    require(q.display.split("\n").length<=new int[]{2,2,3}[age],"too many shape rows");
                    for(String row:q.display.split("\n"))
                        require(row.split("\u2003").length<=(age==0?3:5),"shape spacing/column bound");
                    require(Integer.parseInt(q.answers.get(q.correctIndex))==expected,"counted all shapes instead of target");
                    for(String answer:q.answers){
                        int value=Integer.parseInt(answer);
                        require(value>=(age==0?1:0)&&value<=new int[]{3,9,15}[age],"answer age bound");
                    }
                    require(PlaylumiQuizCoach.explain(LearningGame.COUNT,5,q).isEmpty(),"early counting explanation");
                    quantities.add(expected);shapes.add(symbol);counting++;
                    if(trial==0)restore(q,LearningGame.COUNT,age);
                    game.nextRound();
                }
            }
            require(quantities.size()==new int[]{3,7,11}[age]&&shapes.size()==new int[]{3,5,6}[age],"counting coverage");
        }
    }
    private static void calendar(){
        for(int age=0;age<3;age++){
            Set<String> questions=new HashSet<>();
            for(int seed=0;seed<24;seed++){
                LearningGame game=new LearningGame(new Random(0x6c8e9cf5L*(seed+1)+age));game.configure(16,5,age);
                for(int trial=0;trial<100;trial++){
                    LearningGame.Round q=game.round();basic(q,age==0?3:4);
                    String[] parts=q.display.split("\n");require(parts.length==2,"weekday display rows");
                    int start=Arrays.asList(DAYS).indexOf(parts[0]);require(start>=0,"unknown weekday");
                    Matcher distance=Pattern.compile("([1-6]) (dag|dagen) (eerder|later)").matcher(parts[1]);
                    require(distance.matches(),"unclear day distance");int amount=Integer.parseInt(distance.group(1));
                    require((amount==1?"dag":"dagen").equals(distance.group(2)),"Dutch day singular/plural");
                    require(age==2||amount==1,"multi-day shift for younger tier");
                    int delta=distance.group(3).equals("later")?amount:-amount;
                    // java.time's ISO week is the oracle, not the model's modulo helper.
                    String expected=DAYS[DayOfWeek.of(start+1).plus(delta).getValue()-1];
                    require(expected.equals(q.answers.get(q.correctIndex)),"weekday cycle incorrect");
                    for(String answer:q.answers)require(Arrays.asList(DAYS).contains(answer),"non-day distractor");
                    if(start+delta<0||start+delta>6)rollovers++;
                    questions.add(q.display);calendar++;
                    require(PlaylumiQuizCoach.explain(16,5,q).isEmpty(),"early calendar explanation");
                    if(trial==0)restore(q,16,age);
                    game.nextRound();
                }
            }
            require(questions.size()==(age==2?84:14),"weekday/direction/distance coverage");
        }
        require(rollovers>1000,"week boundary coverage");
    }
    private static String capitalized(String value){return value.substring(0,1).toUpperCase(Locale.ROOT)+value.substring(1);}
    /** Resolve a literal question by its grammar and the visible subject/action/location. */
    private static String[] readingFact(LearningGame.Round q){
        String result=null,support=null;
        String query=q.prompt.substring(0,q.prompt.length()-1);
        for(String sentence:q.display.split("\n")){
            require(sentence.endsWith("."),"unfinished story sentence");
            String fact=sentence.substring(0,sentence.length()-1),candidate=null;
            if(query.startsWith("Wie ")){
                int space=fact.indexOf(' ');
                if(space>0&&fact.substring(space+1).equals(query.substring(4)))candidate=fact.substring(0,space);
            }else if(query.startsWith("Wat ")){
                String[] predicate=query.substring(4).split(" ",2);
                String prefix=predicate[1]+" "+predicate[0]+" ";
                if(fact.startsWith(prefix))candidate=capitalized(fact.substring(prefix.length()));
            }else if(query.startsWith("Waar ")){
                String[] predicate=query.substring(5).split(" ",2);
                String prefix=capitalized(predicate[1])+" "+predicate[0]+" ";
                if(fact.startsWith(prefix))candidate=capitalized(fact.substring(prefix.length()));
            }else throw new AssertionError("unknown comprehension question");
            if(candidate!=null){require(result==null,"two supported reading answers");result=candidate;support=sentence;}
        }
        require(result!=null,"answer requires unstated information");return new String[]{result,support};
    }
    private static void reading(){
        Set<String> seen=new HashSet<>();int[] kinds=new int[3];
        for(int seed=0;seed<40;seed++){
            LearningGame game=new LearningGame(new Random(0x9e3779b9L*(seed+1)));game.configure(LearningGame.WORDS,5,2);
            for(int trial=0;trial<60;trial++){
                LearningGame.Round q=game.round();basic(q,4);
                require(q.display.length()<=84&&q.display.split("\n").length==2,"story readability bound");
                for(String answer:q.answers)require(answer.length()<=18,"long reading answer");
                String[] evidence=readingFact(q);
                require(evidence[0].equals(q.answers.get(q.correctIndex)),"answer disagrees with visible sentence");
                require(Collections.frequency(q.answers,evidence[0])==1,"multiple literal answers");
                require(PlaylumiQuizCoach.explain(LearningGame.WORDS,5,q).isEmpty(),"early reading explanation");
                if(seen.add(q.prompt+"\n"+q.display))kinds[q.prompt.startsWith("Wie ")?0:q.prompt.startsWith("Wat ")?1:2]++;
                if(trial==0)restore(q,LearningGame.WORDS,2);
                require(q.choose(q.correctIndex),"reading answer not accepted");
                require(evidence[1].equals(PlaylumiQuizCoach.explain(LearningGame.WORDS,5,q)),"feedback is not the supporting sentence");
                reading++;game.nextRound();
            }
        }
        require(seen.size()==24&&Arrays.equals(kinds,new int[]{8,8,8}),"literal question coverage");
    }
    private static void histories(){
        for(int[] config:new int[][]{{0,0},{16,1},{6,2}}){
            LearningGame game=new LearningGame(new Random(110+config[0]));
            QuestionHistory history=new QuestionHistory();game.configure(config[0],5,config[1]);game.setHistory(history);
            Set<String> seen=new HashSet<>();
            for(int i=0;i<8;i++){game.nextRound();require(seen.add(history.fingerprint(game.round())),"repeat inside first round");}
            require(new QuestionHistory(history.serialize()).size()==8,"question history not persisted");
        }
    }
    private static void catalog()throws Exception{
        for(int age=0;age<3;age++){
            List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.entries(age);
            require(entries.size()==new int[]{26,45,71}[age],"catalog size");
            Set<String> ids=new HashSet<>();List<String> legacy=new ArrayList<>();
            for(PlaylumiGameCatalog.Entry entry:entries){
                require(ids.add(entry.id),"duplicate route");
                if(!Arrays.asList(NEW_IDS).contains(entry.id))legacy.add(entry.id+"="+entry.title);
            }
            require(ids.contains(NEW_IDS[0])&&ids.contains(NEW_IDS[1])==(age>=1)&&ids.contains(NEW_IDS[2])==(age==2),"route age boundary");
            Collections.sort(legacy);
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(String.join("\n",legacy).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex=new StringBuilder();for(byte b:digest)hex.append(String.format(Locale.ROOT,"%02x",b&255));
            require(hex.toString().equals(LEGACY_HASHES[age]),"released route ID/title changed");
        }
        require(PlaylumiLearningProfile.minimum(2,6,5)==2,"reading difficulty dropped below approved tier");
        require(PlaylumiScores.CATEGORIES==24&&LearningGame.GAME_NAMES.length==24,"score category migration");
    }
    public static void main(String[] args)throws Exception{
        catalog();counting();calendar();reading();histories();
        System.out.println("PASS: "+counting+" selective-counting questions; every target/count and youngest spacing bounds.");
        System.out.println("PASS: "+calendar+" calendar questions; all 14/14/84 day shifts, including "+rollovers+" week-boundary crossings.");
        System.out.println("PASS: "+reading+" literal-reading questions; all 24 stories independently resolved (8 who, 8 what, 8 where).");
        System.out.println("PASS: "+restores+" exact rejected/solved restores, no early explanations, three fresh eight-question histories, and all released route IDs/titles.");
    }
}

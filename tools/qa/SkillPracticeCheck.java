package nl.playlumi.app;
import java.util.*;

public final class SkillPracticeCheck {
    private static void require(boolean good,String message){if(!good)throw new AssertionError(message);}
    public static void main(String[] args){
        Random r=new Random(1152026L);int checked=0;
        int[] modes={LearningGame.COUNT,LearningGame.NUMBERS,LearningGame.LETTERS,LearningGame.SHAPES,LearningGame.PATTERNS,19,LearningGame.COLORS};
        Map<String,String> shapes=new HashMap<>();shapes.put("●","0");shapes.put("▲","3");shapes.put("■","4");shapes.put("◆","4");shapes.put("▭","4");
        String[] locations={"Vriezer","Wasmachine","Badkamer","Brievenbus","Slaapkamer","Bibliotheek","Station","Bakkerij","Zwembad","Voetbalveld","Vuilnisbak","Koelkast","Bioscoop","Schoenmakerij","Bloemenwinkel","Klaslokaal"};
        String[] clues={"IJsblokjes","Vieze kleding","Tandenborstel","Brief","Slapen","Boeken lenen","Trein nemen","Brood kopen","Zwemles","Voetbalwedstrijd","Vuilnis","Voedsel koel bewaren","Film kijken","Schoenen","Bloemen","Schoolspullen"};
        for(int age=0;age<3;age++){
            Set<Integer> exposed=new HashSet<>();Set<String> ids=new HashSet<>();
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age)){
                require(ids.add(e.id),"Duplicate route id");
                if(e.kind==PlaylumiGameCatalog.QUIZ&&PlaylumiSkillPractice.supports(e.mode,e.variant))exposed.add(e.mode);
            }
            require(exposed.size()==(age==0?3:7),"New route count");
            for(int mode:modes){
                require(exposed.contains(mode)==PlaylumiSkillPractice.offered(age,mode),"Age boundary");
                Set<String> seen=new HashSet<>();LearningGame game=new LearningGame(r);game.configure(mode,6,age);
                for(int i=0;i<500;i++){
                    LearningGame.Round q=game.round();String answer=q.answers.get(q.correctIndex),expected;
                    require(q.answers.size()>=3&&new HashSet<>(q.answers).size()==q.answers.size(),"Unique choices");
                    require(PlaylumiQuizCoach.explain(mode,6,q).isEmpty(),"Premature answer");
                    switch(mode){
                        case LearningGame.COUNT:
                            int n=age==0?q.display.split("●",-1).length-1:Integer.parseInt(q.display);
                            expected=""+(n+(q.prompt.contains("erbij")?1:-1));break;
                        case LearningGame.NUMBERS:
                            String[] parts=q.display.trim().split("\\s+");
                            int left=Integer.parseInt(parts[0]),right=Integer.parseInt(parts[2]);
                            require(right-left==2,"Not two apart");expected=""+(left+1);break;
                        case LearningGame.LETTERS:expected=""+(char)(q.display.charAt(0)+1);break;
                        case LearningGame.SHAPES:expected=shapes.get(q.display);break;
                        case LearningGame.PATTERNS:
                            String[] notes=q.display.trim().split("\\s+");int len=(notes.length-1)/2;
                            for(int j=0;j<len;j++)require(notes[j].equals(notes[j+len]),"Broken pattern");
                            expected=notes[0];break;
                        case 19:expected=locations[Arrays.asList(clues).indexOf(q.display)];break;
                        case LearningGame.COLORS:
                            expected=q.display;
                            for(int j=0;j<q.answers.size();j++)require(q.answerColors.get(j)==LearningGame.COLOR_VALUES[Arrays.asList(LearningGame.COLOR_NAMES).indexOf(q.answers.get(j))],"Wrong swatch color");break;
                        default:throw new AssertionError();
                    }
                    require(answer.equals(expected),"Incorrect result "+q.prompt+" / "+q.display);
                    seen.add(q.prompt+"/"+q.display);
                    int wrong=(q.correctIndex+1)%q.answers.size();require(!q.choose(wrong),"Accepted a wrong answer");
                    LearningGame restored=new LearningGame(new Random(5));
                    restored.configure(mode,6,age);
                    restored.restoreRound(q.prompt,q.display,q.displayColor,q.answers,q.answerColors,q.correctIndex,q.fingerCount,false,q.rejectedMask());
                    require(restored.round().isRejected(wrong)&&restored.round().answers.equals(q.answers),"Lost rejected answer on restore");
                    require(restored.round().choose(restored.round().correctIndex),"Could not solve restored exercise");
                    require(q.choose(q.correctIndex)&&!q.choose(q.correctIndex),"Duplicate accepted answer");
                    String explanation=PlaylumiQuizCoach.explain(mode,6,q);
                    require(!explanation.isEmpty()&&explanation.equals(PlaylumiQuizCoach.explain(mode,6,restored.round())),"Missing or inconsistent explanation");
                    checked++;game.nextRound();
                }
                require(seen.size()>=3,"Insufficient question variety");
            }
        }
        System.out.println("PASS: "+checked+" new skill questions; independently checked results, choices, age boundaries, swatch colors, exact restore and feedback.");
    }
}

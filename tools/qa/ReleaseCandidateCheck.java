package nl.playlumi.app;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fast, dependency-free release gate. Run using JDK 17 before assembling Android. */
public final class ReleaseCandidateCheck {
    private static int quizzes,interactive,routes,settings;
    private static final Random RNG=new Random(20261126L);
    private static void require(boolean ok,String context){
        if(!ok)throw new AssertionError(context);
    }
    private static int minutes(String value){
        String[] parts=value.split(":");return Integer.parseInt(parts[0])*60+Integer.parseInt(parts[1]);
    }
    private static void round(LearningGame.Round q,int mode,int variant,int level,String context){
        require(q.prompt!=null&&!q.prompt.isEmpty()&&q.display!=null&&!q.display.isEmpty(),context+" empty question");
        require(q.answers.size()>=2&&q.answers.size()==q.answerColors.size()&&new HashSet<>(q.answers).size()==q.answers.size(),context+" choices");
        require(q.correctIndex>=0&&q.correctIndex<q.answers.size()&&!q.isSolved(),context+" initial state");
        String right=q.answers.get(q.correctIndex);
        require(!right.isEmpty(),context+" empty answer");
        if(mode==ExtraGames.FIRST_LAST&&variant<=1){
            require(q.letterBlank()>=0&&q.visibleDisplay().contains("_"),context+" visible letter");
            boolean last=variant==1;
            if(q.prompt.equals(PlaylumiLetterChallenges.MATCH_FIRST)||q.prompt.equals(PlaylumiLetterChallenges.MATCH_LAST)){
                int matches=0;
                for(String option:q.answers)if((last?option.substring(option.length()-1):option.substring(0,1)).equals(q.blankReplacement()))matches++;
                require(matches==1,context+" ambiguous letter match");
            }
        }
        if(mode==ExtraGames.MATH){
            Matcher digits=Pattern.compile("\\d+").matcher(q.display);
            require(digits.find(),context+" first operand");int x=Integer.parseInt(digits.group());
            require(digits.find(),context+" second operand");int y=Integer.parseInt(digits.group());
            int expected=variant==2?y-x:variant==1||q.display.contains("weg")?x-y:x+y;
            require(Integer.parseInt(right)==expected,context+" arithmetic");
        }
        if(mode==16){
            if(variant==0){
                require(q.display.startsWith("clock:")&&q.display.substring(6).equals(right),context+" clock");
                for(String value:q.answers)require(minutes(value)>=60&&minutes(value)<780,context+" ambiguous 24h face answer");
            }else if(variant==1||variant==2){
                int base=minutes(q.display.substring(q.display.lastIndexOf(' ')+1));
                require(minutes(right)==Math.floorMod(base+(variant==1?60:-60),1440),context+" clock shift");
            }
        }
        for(int i=0;i<q.answers.size();i++)if(i!=q.correctIndex)require(!q.choose(i),context+" accepted wrong option");
        require(q.choose(q.correctIndex)&&q.isSolved()&&!q.choose(q.correctIndex),context+" reward/answer once");
        if(q.letterBlank()>=0)require(!q.visibleDisplay().contains("_"),context+" fill display");
        quizzes++;
    }
    private static void nonQuiz(PlaylumiGameCatalog.Entry e,int age){
        for(int i=0;i<24;i++){
            if(e.kind==PlaylumiGameCatalog.INTERACTIVE){
                PlaylumiInteractive q=new PlaylumiInteractive(e.mode,age,RNG.nextLong(),0);
                while(!q.solved()){
                    String right=q.expected();
                    require(q.options().contains(right),e.id+" missing tile");
                    for(String value:q.options())if(!value.equals(right))require(!q.choose(value),e.id+" wrong tile");
                    require(q.choose(right),e.id+" correct tile");
                    PlaylumiInteractive restored=new PlaylumiInteractive(q.kind,q.level,q.seed,q.progress());
                    require(restored.progress()==q.progress()&&restored.expected().equals(q.expected()),e.id+" restore");
                }
                interactive++;
            }else if(e.kind==PlaylumiGameCatalog.MUSIC){
                PlaylumiMelody q=PlaylumiMelody.generate(age,e.variant,RNG);
                for(int note:q.sequence())q.tap(note);
                require(q.completed(),e.id+" melody");interactive++;
            }else if(e.kind==PlaylumiGameCatalog.MINI){
                PlaylumiMiniChallenge q=PlaylumiMiniChallenge.generate(e.mode,age,RNG);
                require(!q.choices.isEmpty(),e.id+" mini choices");
                if(e.mode==PlaylumiMiniChallenge.MEMORY){
                    for(String v:q.choices)require(java.util.Collections.frequency(q.choices,v)==2,e.id+" memory pair");
                }else require(q.correctIndex>=0&&q.correctIndex<q.choices.size(),e.id+" mini answer");
                interactive++;
            }else if(e.kind==PlaylumiGameCatalog.BUBBLES){
                PlaylumiBubbles q=PlaylumiBubbles.generate(age,RNG);
                for(int k=0;k<q.target;k++)require(q.pop(k),e.id+" bubble");
                require(q.solved()&&!q.pop(q.target),e.id+" overcredit");interactive++;
            }else if(e.kind==PlaylumiGameCatalog.STARS){
                PlaylumiStarCatch q=PlaylumiStarCatch.generate(age,RNG);
                for(int k=0;k<q.total();k++)require(q.catchStar(k)==q.isStar(k),e.id+" star target");
                require(q.finished()&&q.collected()==q.target(),e.id+" star finish");interactive++;
            }
        }
    }
    public static void main(String[] args){
        LearningGame game=new LearningGame(RNG);
        for(int age=0;age<3;age++){
            Set<String> ids=new HashSet<>();
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age)){
                require(ids.add(e.id),"duplicate route "+e.id);
                require(PlaylumiGameCatalog.find(age,e.id)!=null,"missing route "+e.id);
                routes++;
                if(e.kind!=PlaylumiGameCatalog.QUIZ){nonQuiz(e,age);continue;}
                for(int level=PlaylumiLearningProfile.minimum(age,e.mode,e.variant);level<=age;level++){
                    game.configure(e.mode,e.variant,level);settings++;
                    for(int i=0;i<120;i++){
                        round(game.round(),e.mode,e.variant,level,e.title+" age "+age+" level "+level);
                        game.nextRound();
                    }
                }
            }
            System.out.println("Age tier "+age+": "+ids.size()+" catalog routes");
        }
        // Validate legacy non-catalogue quiz configurations as well.
        for(int mode=0;mode<24;mode++)for(int variant=0;variant<5;variant++)for(int level=0;level<3;level++){
            game.configure(mode,variant,level);settings++;
            for(int i=0;i<32;i++){round(game.round(),mode,variant,level,"legacy "+mode+":"+variant+":"+level);game.nextRound();}
        }
        PlaylumiPlayDays days=new PlaylumiPlayDays(Long.MIN_VALUE,0,0,0);
        require(days.record(100)&&!days.record(100)&&days.record(101)&&days.best==2,"daily streak duplicate");
        require(days.record(103)&&days.best==2&&days.run==1,"streak record");
        int[] stats=new int[PlaylumiAchievements.METRICS];stats[0]=500;
        long earned=PlaylumiAchievements.unlock(0,stats);
        require(earned!=0&&PlaylumiAchievements.unlock(earned,new int[stats.length])==earned,"achievement persistence");
        System.out.println("PASS: "+routes+" age-specific catalog routes; "+settings+" quiz configurations; "+quizzes+" quiz rounds; "+interactive+" interactive engine sessions.");
        System.out.println("Scope: pure Java rules and state; drawing, visual Android layout, audio, and device upgrade still require manual QA.");
    }
}

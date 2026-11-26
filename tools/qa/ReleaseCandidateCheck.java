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
            int expected=variant==PlaylumiAdvancedMath.TABLES?x*y:
                    variant==PlaylumiAdvancedMath.SHARING?x/y:
                    variant==PlaylumiAdvancedMath.TO_HUNDRED?(q.display.contains("−")?x-y:x+y):
                    variant==2?y-x:variant==1||q.display.contains("weg")?x-y:x+y;
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
        int restores=0;
        for(int age=0;age<3;age++)for(int kind=0;kind<3;kind++)for(int i=0;i<180;i++){
            PlaylumiMiniChallenge mini=PlaylumiMiniChallenge.generate(kind,age,RNG);
            if(kind==PlaylumiMiniChallenge.MEMORY){
                int mate=mini.choices.lastIndexOf(mini.choices.get(0));require(mate>0,"memory pair");
                require(mini.flip(0)==1&&mini.flip(mate)==2,"memory match");
                int open=0;while(mini.matched(open))open++;
                mini.flip(open);
            }
            PlaylumiMiniChallenge restored=PlaylumiMiniChallenge.restore(kind,age,mini.target,mini.choices,mini.correctIndex,mini.matchedMask(),mini.firstOpen());
            require(restored.choices.equals(mini.choices)&&restored.correctIndex==mini.correctIndex&&restored.matchedMask()==mini.matchedMask()&&restored.firstOpen()==mini.firstOpen(),"mini restore");
            restores++;
        }
        System.out.println("PASS: "+restores+" saved minigame state round trips.");
        int searches=0;
        for(int age=0;age<3;age++)for(String query:new String[]{"","klok","rijmen","géheugen","maan","dieren","nietbestaandspel"}){
            Set<String> ids=new HashSet<>();
            for(PlaylumiGameCatalog.Entry e:PlaylumiLibrarySearch.find(age,query))
                require(ids.add(e.id)&&PlaylumiGameCatalog.find(age,e.id)!=null,"search scope");
            searches++;
        }
        require(PlaylumiLibrarySearch.find(2,"nietbestaandspel").isEmpty(),"search empty");
        require(PlaylumiLibrarySearch.find(0,"klok").isEmpty(),"search age boundary");
        require(PlaylumiLibrarySearch.find(2,"klokkijken").get(0).title.equals("Klokkijken"),"search rank");
        game.configure(ExtraGames.MATH,0,2);
        LearningGame.Round example=game.round();int wrong=(example.correctIndex+1)%example.answers.size();
        require(!example.choose(wrong)&&example.isRejected(wrong),"wrong choice stored");
        int rejected=example.rejectedMask();
        game.restoreRound(example.prompt,example.display,example.displayColor,example.answers,example.answerColors,example.correctIndex,example.fingerCount,false);
        game.round().restoreRejected(rejected);
        require(game.round().isRejected(wrong)&&!game.round().choose(wrong),"wrong choice restored");
        require(game.round().choose(game.round().correctIndex)&&!PlaylumiQuizCoach.explain(ExtraGames.MATH,0,game.round()).isEmpty(),"solved explanation");
        System.out.println("PASS: "+searches+" search scopes and rejected-answer restoration.");
        Set<String> spellingSentences=new HashSet<>();
        for(int i=0;i<2500;i++){
            LearningGame.Round r=PlaylumiSpellingPractice.next(RNG);
            String answer=r.answers.get(r.correctIndex);
            require(PlaylumiSpellingPractice.isContext(r.display)&&r.display.indexOf('…')==r.display.lastIndexOf('…'),"spelling context");
            require(answer.equals(PlaylumiSpellingPractice.answerFor(r.display))&&r.answers.size()==4&&new HashSet<>(r.answers).size()==4,"spelling options");
            require(!PlaylumiSpellingPractice.hint(r.display).isEmpty(),"spelling hint");
            require(PlaylumiQuizCoach.explain(ExtraGames.SPELLING,0,r).isEmpty(),"premature explanation");
            require(r.choose(r.correctIndex)&&!PlaylumiQuizCoach.explain(ExtraGames.SPELLING,0,r).isEmpty(),"spelling explanation");
            spellingSentences.add(r.display);
        }
        require(spellingSentences.size()==PlaylumiSpellingPractice.count(),"spelling sentence coverage");
        for(int age=0;age<3;age++)for(int variant=0;variant<2;variant++){
            Set<String> letters=new HashSet<>();
            game.configure(LearningGame.LETTERS,variant,age);
            for(int i=0;i<600;i++){
                LearningGame.Round r=game.round();
                String answer=r.answers.get(r.correctIndex);
                require(answer.equalsIgnoreCase(r.display)&&!answer.equals(r.display),"letter case");
                require(r.answers.size()==(age==0?3:age==1?4:6),"letter choices");
                letters.add(r.display);game.nextRound();
            }
            require(letters.size()==(age==0?10:age==1?18:26),"letter variety");
        }
        require(PlaylumiQuizPace.delay(PlaylumiQuizPace.MANUAL,false,"")<0,"manual pace");
        require(PlaylumiQuizPace.delay(PlaylumiQuizPace.CALM,true,"uitleg")>PlaylumiQuizPace.delay(PlaylumiQuizPace.AUTO,true,"uitleg"),"calm pace");
        System.out.println("PASS: 2,500 spelling questions across "+spellingSentences.size()+" contexts; 3,600 letter questions; quiz pace settings.");
        int advancedInteractive=0;
        for(int level=0;level<3;level++)for(int kind=0;kind<4;kind++)for(int seed=0;seed<120;seed++){
            PlaylumiInteractive old=new PlaylumiInteractive(kind,level,seed,0);
            PlaylumiInteractive legacy=new PlaylumiInteractive(kind,level,seed,0,PlaylumiInteractive.LEGACY);
            require(old.total()==legacy.total()&&old.options().equals(legacy.options()),"legacy interactive compatibility");
            PlaylumiInteractive r=PlaylumiInteractive.start(kind,level,seed);
            if(kind==PlaylumiInteractive.NUMBERS&&level>0){
                require(r.options().size()==r.total()+(level==1?1:2),"number decoys");
                for(int j=1;j<r.total();j++)require(Integer.parseInt(r.target(j))-Integer.parseInt(r.target(j-1))==r.step(),"number step");
                for(int j=0;j<r.total();j++)require(Integer.parseInt(r.target(j))>=1&&Integer.parseInt(r.target(j))<=20,"number bounds");
            }
            if(kind==PlaylumiInteractive.LETTERS&&level>0){
                require(r.wordPuzzle()&&!r.clue().isEmpty()&&!r.picture().isEmpty(),"word clue");
                require(r.example(1,false).equals("?")&&r.example(1,true).equals(r.target(1)),"hidden letters");
            }
            while(!r.solved()){
                String right=r.expected();
                require(r.options().contains(right),"new interactive answer");
                for(String value:r.options())if(!value.equals(right))require(!r.choose(value),"wrong new tile");
                require(r.choose(right),"correct new tile");
                PlaylumiInteractive restored=new PlaylumiInteractive(kind,level,seed,r.progress(),r.version);
                require(r.options().equals(restored.options())&&r.expected().equals(restored.expected())&&r.total()==restored.total()&&r.step()==restored.step()&&r.clue().equals(restored.clue()),"versioned interactive restore");
            }
            require(!r.choose(r.options().get(0)),"completed interactive cannot credit again");
            advancedInteractive++;
        }
        System.out.println("PASS: "+advancedInteractive+" versioned interactive sessions, including legacy compatibility.");
        int advancedMath=0;
        for(int age=0;age<3;age++){
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age))
                if(e.mode==ExtraGames.MATH&&e.kind==PlaylumiGameCatalog.QUIZ&&e.variant>=PlaylumiAdvancedMath.TABLES)
                    require(age==2,"advanced math age boundary");
            for(int variant=PlaylumiAdvancedMath.TABLES;variant<=PlaylumiAdvancedMath.TO_HUNDRED;variant++){
                game.configure(ExtraGames.MATH,variant,age);
                Set<String> variety=new HashSet<>();boolean forward=false,backward=false;
                for(int k=0;k<400;k++){
                    LearningGame.Round r=game.round();
                    Matcher digits=Pattern.compile("\\d+").matcher(r.display);
                    require(digits.find(),"advanced first operand");int x=Integer.parseInt(digits.group());
                    require(digits.find(),"advanced second operand");int y=Integer.parseInt(digits.group());
                    int answer=Integer.parseInt(r.answers.get(r.correctIndex));
                    require(answer>=0&&answer<=100,"advanced bounds");
                    if(variant==PlaylumiAdvancedMath.SHARING)require(y>0&&x%y==0&&answer==x/y,"exact division");
                    if(variant==PlaylumiAdvancedMath.TO_HUNDRED){
                        require(x<=100&&y<=100,"two digit bounds");
                        if(age==2)require(x>=10&&y>=10,"advanced tens operands");
                        forward|=r.display.contains("+");backward|=r.display.contains("−");
                    }
                    require(PlaylumiQuizCoach.explain(ExtraGames.MATH,variant,r).isEmpty(),"no answer before solving");
                    int mask=(1<<((r.correctIndex+1)%r.answers.size()));
                    r.restoreRejected(mask);
                    require(!r.isRejected(r.correctIndex),"restored right answer remains enabled");
                    r.restoreRejected(0);
                    round(r,ExtraGames.MATH,variant,age,"advanced arithmetic");
                    require(!PlaylumiQuizCoach.explain(ExtraGames.MATH,variant,r).isEmpty(),"advanced explanation");
                    variety.add(r.display);game.nextRound();advancedMath++;
                }
                require(variety.size()>=5,"advanced variety");
                if(variant==PlaylumiAdvancedMath.TO_HUNDRED)require(forward&&backward,"mixed addition and subtraction");
            }
        }
        System.out.println("PASS: "+advancedMath+" additional advanced arithmetic rounds.");
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

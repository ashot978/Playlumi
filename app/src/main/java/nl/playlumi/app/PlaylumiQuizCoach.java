package nl.playlumi.app;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Keep short answers together on small screens and show a concrete explanation after solving. */
public final class PlaylumiQuizCoach {
    public static final String RETRY_MESSAGE="Probeer het nog eens.";
    private PlaylumiQuizCoach(){}
    public static int columns(List<String> choices,int width,boolean split,boolean centered,boolean pattern){
        if(PlaylumiAnswerOrder.verticalPair(choices))return 1;
        if(PlaylumiAnswerOrder.horizontalPair(choices))return 2;
        if(choices.size()<5)return 2;
        int available=split?width/2:Math.min(width,760);
        int longest=0;
        for(String value:choices)longest=Math.max(longest,value.codePointCount(0,value.length()));
        if(!pattern&&longest<=6&&available>=340)return 3;
        if(centered&&longest<=10&&available>=570)return 3;
        return 2;
    }
    public static int columns(List<String> choices,int width,boolean split,boolean centered,boolean pattern,float fontScale){
        int cols=columns(choices,width,split,centered,pattern);
        if(PlaylumiAnswerOrder.verticalPair(choices)||PlaylumiAnswerOrder.horizontalPair(choices))return cols;
        int available=split?width/2:Math.min(width,760),longest=0;
        for(String value:choices)longest=Math.max(longest,value.codePointCount(0,value.length()));
        if(fontScale>=1.2f){
            if(longest>=18&&available<520)return 1;
            return Math.min(2,cols);
        }
        return !pattern&&longest>=24&&available<340?1:cols;
    }
    public static int delay(boolean retried,String feedback){
        int length=feedback==null?0:feedback.codePointCount(0,feedback.length());
        return Math.min(2300,Math.max(retried?1550:900,750+length*24));
    }
    public static String explain(int mode,int variant,LearningGame.Round round){
        if(!round.isSolved())return "";
        if(PlaylumiSkillPractice.supports(mode,variant))return PlaylumiSkillPractice.explain(mode,variant,round);
        if(mode==PlaylumiOddOneOut.MODE&&variant==PlaylumiOddOneOut.VARIANT)return PlaylumiOddOneOut.explain(round);
        if(mode==PlaylumiFocusedCounting.MODE&&variant==PlaylumiFocusedCounting.VARIANT)return PlaylumiFocusedCounting.explain(round);
        if(mode==PlaylumiWeekdays.MODE&&variant==PlaylumiWeekdays.VARIANT)return PlaylumiWeekdays.explain(round);
        if(mode==PlaylumiReadingPractice.MODE&&variant==PlaylumiReadingPractice.VARIANT)return PlaylumiReadingPractice.explain(round);
        if(PlaylumiThinkingPractice.supports(mode,variant))return PlaylumiThinkingPractice.explain(mode,variant,round);
        String right=round.answers.get(round.correctIndex);
        if(mode==ExtraGames.SPELLING&&PlaylumiSpellingPractice.isContext(round.display))
            return PlaylumiSpellingPractice.explanation(round.display,right);
        if(mode==ExtraGames.MATH&&variant>=PlaylumiAdvancedMath.TABLES)
            return PlaylumiAdvancedMath.explain(variant,round.display,right);
        if(mode==ExtraGames.MATH){
            Matcher digits=Pattern.compile("\\d+").matcher(round.display);
            if(digits.find()){int a=Integer.parseInt(digits.group());if(digits.find()){
                int b=Integer.parseInt(digits.group());
                if(variant==2)return a+" + "+right+" = "+b;
                return a+(variant==1||round.display.contains("weg")?" − ":" + ")+b+" = "+right;
            }}
        }
        if(mode==ExtraGames.SEQUENCES||mode==23&&(variant==0||variant==1||variant==3))return "De reeks klopt met "+right+".";
        if(mode==23&&variant==2)return right.equals("Even")?"Je kunt dit getal in tweetallen verdelen.":"Er blijft één over bij tweetallen.";
        if(mode==16&&variant==0)return "De wijzers geven "+right+" aan.";
        if(mode==16&&(variant==1||variant==2))return "De minuten blijven gelijk: "+right+".";
        if(mode==16&&variant==3)return "Bij dit dagdeel hoort "+right+".";
        if(mode==22&&variant==0)return right.equals("Gelijk")?"De twee lengtes zijn gelijk.":right+" is langer.";
        if(round.letterBlank()>=0)return "De ontbrekende letter is "+round.blankReplacement()+".";
        if(mode==LearningGame.COUNT||mode==LearningGame.SHAPES&&variant==3)return "Het juiste aantal is "+right+".";
        if(mode==LearningGame.NUMBERS&&variant==0)return "Je ziet "+right+" vingers.";
        if(mode==LearningGame.COLORS)return "De juiste kleur is "+right+".";
        if(mode==LearningGame.SHAPES&&(variant==1||variant==2))return "Dit is een "+right+".";
        if(mode==LearningGame.PATTERNS)return "Het patroon klopt met "+right+".";
        if(mode==ExtraGames.RHYMES)return variant==1?"De eindklank van "+right+" is anders.":right+" heeft dezelfde eindklank.";
        return "";
    }
}

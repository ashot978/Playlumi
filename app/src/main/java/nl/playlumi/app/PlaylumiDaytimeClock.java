package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Learn the relation between spoken 12-hour times and a digital 24-hour clock. */
public final class PlaylumiDaytimeClock {
    private PlaylumiDaytimeClock(){}
    public static String period(int hour){
        if(hour<0||hour>23)throw new IllegalArgumentException("hour");
        if(hour<6)return "’s nachts";
        if(hour<12)return "’s ochtends";
        if(hour<18)return "’s middags";
        return "’s avonds";
    }
    public static String display(int hour,int minute){
        String part=period(hour);
        if(minute<0||minute>59)throw new IllegalArgumentException("minute");
        String icon=hour<6?"🌙":hour<12?"🌅":hour<18?"☀️":"🌆";
        int spoken=hour%12==0?12:hour%12;
        return spoken+" uur"+(minute==0?"":" en "+minute+(minute==1?" minuut":" minuten"))+"\n"+icon+" "+part;
    }
    private static String digital(int hour,int minute){
        return String.format(Locale.ROOT,"%02d:%02d",(hour+24)%24,(minute+60)%60);
    }
    public static LearningGame.Round round(int hour,int minute,int level,Random random){
        if(level<0||level>2)throw new IllegalArgumentException("level");
        String question=display(hour,minute),right=digital(hour,minute);
        int step=level==0?30:level==1?15:5;
        int opposite=(hour+12)%24;
        List<String> answers=new ArrayList<>();
        answers.add(right);
        answers.add(digital(opposite,minute)); // Essential 12/24-hour misconception.
        answers.add(digital(hour,minute-step)); // Same hour, different minutes.
        if(level>=1)answers.add(digital(hour,minute+step));
        if(level==2){
            answers.add(digital(hour-1,minute));
            answers.add(digital(hour+1,minute));
        }
        Collections.shuffle(answers,random);
        List<Integer> colors=new ArrayList<>();
        for(int i=0;i<answers.size();i++)colors.add(0xff244454);
        return new LearningGame.Round("Hoe laat is dit op de digitale klok?",question,0xff376c98,
                answers,colors,answers.indexOf(right));
    }
}

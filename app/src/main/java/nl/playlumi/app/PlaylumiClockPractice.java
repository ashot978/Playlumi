package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

/** A clock face without a day-part clue uses only 1–12 hour notation. */
public final class PlaylumiClockPractice {
    private PlaylumiClockPractice(){}
    private static String format(int minutes,boolean face){
        int t=Math.floorMod(minutes,face?720:1440);
        int hour=t/60;
        if(face&&hour==0)hour=12;
        return String.format(Locale.ROOT,"%02d:%02d",hour,t%60);
    }
    private static int minute(Random r,int level){return level==0?0:level==1?r.nextInt(4)*15:r.nextInt(12)*5;}
    private static LearningGame.Round make(String prompt,String display,int value,boolean face,int level,Random r){
        String right=format(value,face);
        ArrayList<String> choices=new ArrayList<>();choices.add(right);
        int count=level==0?3:level==1?4:6;
        // Neighbouring hour, same hour with different minutes, and another plausible clock reading.
        int step=level==0?60:level==1?15:5;
        int[] offsets=level==0?new int[]{60,-60,120,-120}:new int[]{60,-60,step,-step,30,-30,120};
        ArrayList<Integer> order=new ArrayList<>();
        for(int delta:offsets)order.add(delta);
        Collections.shuffle(order,r);
        for(int delta:order){
            String candidate=format(value+delta,face);
            if(!choices.contains(candidate))choices.add(candidate);
            if(choices.size()==count)break;
        }
        Collections.shuffle(choices,r);
        ArrayList<Integer> colors=new ArrayList<>();for(String ignored:choices)colors.add(0xff244454);
        return new LearningGame.Round(prompt,display,0xff376c98,choices,colors,choices.indexOf(right));
    }
    public static LearningGame.Round analog(int level,Random r){
        int value=r.nextInt(12)*60+minute(r,level);
        return make("Hoe laat is het op de wijzerklok?","clock:"+format(value,true),value,true,level,r);
    }
    public static LearningGame.Round shift(int level,int direction,Random r){
        int value=r.nextInt(24)*60+minute(r,level);
        int[] jumps=level==0?new int[]{60}:level==1?new int[]{30,60,90}:new int[]{15,30,45,60,90,120};
        int jump=jumps[r.nextInt(jumps.length)];
        String amount=jump==120?"2 uur":jump==90?"1,5 uur":jump==60?"1 uur":jump+" minuten";
        String question=direction==1?"Hoe laat is het "+amount+" later?":"Hoe laat was het "+amount+" eerder?";
        return make(question,"⏰ "+format(value,false),value+direction*jump,false,level,r);
    }
}

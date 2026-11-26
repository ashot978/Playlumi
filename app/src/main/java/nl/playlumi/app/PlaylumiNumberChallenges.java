package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Reason about a rule, not just copy a visible number. All answers are offline and deterministic for a seed. */
public final class PlaylumiNumberChallenges {
    private PlaylumiNumberChallenges(){}
    private static int count(int age){return age==0?3:age==1?4:6;}
    public static List<String> nearby(int right,int size,Random random){
        ArrayList<String> answers=new ArrayList<>();answers.add(""+right);
        for(int d=1;answers.size()<size;d++){
            if(right-d>=0&&answers.size()<size)answers.add(""+(right-d));
            if(answers.size()<size)answers.add(""+(right+d));
        }
        Collections.shuffle(answers,random);return answers;
    }
    private static LearningGame.Round round(String prompt,String display,String right,List<String> options){
        ArrayList<Integer> colors=new ArrayList<>();
        for(int i=0;i<options.size();i++)colors.add(0xff3d195f);
        return new LearningGame.Round(prompt,display,0xff3d195f,options,colors,options.indexOf(right));
    }
    public static LearningGame.Round sequence(int age,int direction,boolean middle,boolean alternating,Random random){
        return sequence(age,direction,middle,alternating,random,0);
    }
    public static LearningGame.Round sequence(int age,int direction,boolean middle,boolean alternating,Random random,int fixedStep){
        if(age<0||age>2||(direction!=1&&direction!=-1)||fixedStep<0)throw new IllegalArgumentException("sequence");
        int step=fixedStep>0?fixedStep:age==0?1:age==1?1+random.nextInt(2):2+random.nextInt(4);
        int other=step;
        if(alternating&&age==2){other=2+random.nextInt(4);if(other==step)other=step==5?4:step+1;}
        // At least seven terms for alternating steps: both step sizes remain visible with an internal gap.
        int length=other!=step?7+random.nextInt(2):age==0?4+random.nextInt(2):age==1?5+random.nextInt(2):6+random.nextInt(2);
        int[] values=new int[length];
        int span=0;for(int i=1;i<length;i++)span+=(i%2==1?step:other);
        int start=1+random.nextInt(age==0?3:age==1?10:25);
        if(direction<0)start+=span;
        values[0]=start;
        for(int i=1;i<length;i++)values[i]=values[i-1]+direction*(i%2==1?step:other);
        // Internal gaps never hide all observations of either alternating step.
        int missing=middle||age>0&&random.nextBoolean()?2+random.nextInt(length-3):length-1;
        if(other!=step&&missing==length-2)missing=2;
        StringBuilder display=new StringBuilder();
        for(int i=0;i<length;i++){
            if(i>0)display.append(i==4?" →\n":" → ");
            display.append(i==missing?"?":Integer.toString(values[i]));
        }
        String prompt=other!=step?"De sprongen wisselen. Welk getal ontbreekt?":missing==length-1?
                (direction>0?"Welk getal komt hierna?":"Tel terug. Wat komt hierna?"):"Welk getal ontbreekt in de reeks?";
        return round(prompt,display.toString(),""+values[missing],nearby(values[missing],count(age),random));
    }
    public static LearningGame.Round measure(int age,Random random){
        if(age<0||age>2)throw new IllegalArgumentException("age");
        int cm=2+random.nextInt(age==0?8:14);
        int delta=random.nextInt(5)==0?0:1+random.nextInt(age==2?9:4);
        if(random.nextBoolean())delta=-delta;
        boolean mixed=age==2;
        int left=cm,right=mixed?cm*10+delta:Math.max(1,cm+delta);
        String leftLabel=left+" cm",rightLabel=right+(mixed?" mm":" cm");
        int a=mixed?left*10:left,b=right;
        if(random.nextBoolean()){
            String temp=leftLabel;leftLabel=rightLabel;rightLabel=temp;
            int swap=a;a=b;b=swap;
        }
        List<String> options=new ArrayList<>();
        options.add("Links");options.add("Rechts");options.add("Gelijk");
        return round("Welk lint is langer, of zijn ze even lang?",
                "← "+leftLabel+"    |    "+rightLabel+" →"+(mixed?"\n1 cm = 10 mm":""),
                a>b?"Links":a<b?"Rechts":"Gelijk",options);
    }
}

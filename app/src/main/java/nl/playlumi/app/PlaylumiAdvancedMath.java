package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Separate advanced maths routes, leaving the original arithmetic curriculum intact. */
public final class PlaylumiAdvancedMath {
    public static final int TABLES=5,SHARING=6,TO_HUNDRED=7;
    private static final int[] FACTORS={2,3,4,5,10};
    private PlaylumiAdvancedMath(){}
    public static LearningGame.Round next(int variant,int level,Random r){
        if(variant<TABLES||variant>TO_HUNDRED||level<0||level>2)throw new IllegalArgumentException("advanced maths");
        int a,b,answer,limit;
        String prompt,op;
        LinkedHashSet<Integer> distractors=new LinkedHashSet<>();
        if(variant==TABLES||variant==SHARING){
            int groups=2+r.nextInt(level==0?3:level==1?5:9);
            int each=FACTORS[r.nextInt(level==0?2:level==1?4:FACTORS.length)];
            if(variant==TABLES){
                a=groups;b=each;answer=a*b;limit=100;op="×";prompt="Hoeveel is dit samen?";
                distractors.add(a*(b-1));distractors.add(a*(b+1));distractors.add((a-1)*b);distractors.add(a+b);
            }else{
                a=groups*each;b=each;answer=groups;limit=10;op="÷";prompt="Verdeel in evenveel groepjes als het tweede getal. Hoeveel per groepje?";
                distractors.add(answer-1);distractors.add(answer+1);distractors.add(b);distractors.add(answer+2);
            }
        }else{
            limit=level==0?20:level==1?50:100;
            boolean minus=r.nextBoolean();
            // Prefer two-digit operands in the oldest tier and include crossing a ten.
            int low=level==2?10:level==1?5:2;
            if(minus){
                a=2*low+r.nextInt(limit-2*low+1);
                b=low+r.nextInt(a-low+1);answer=a-b;op="−";
            }else{
                a=low+r.nextInt(limit-2*low+1);
                b=low+r.nextInt(limit-a-low+1);answer=a+b;op="+";
            }
            prompt="Reken uit";
            distractors.add(answer+10);distractors.add(answer-10);distractors.add(answer+1);distractors.add(answer-1);
            distractors.add(minus?a+b:Math.abs(a-b));
        }
        String correct=String.valueOf(answer);
        ArrayList<Integer> wrong=new ArrayList<>();
        for(int n:distractors)if(n>=0&&n<=limit&&n!=answer)wrong.add(n);
        Collections.shuffle(wrong,r);
        int count=level==0?3:4;
        ArrayList<String> choices=new ArrayList<>();choices.add(correct);
        for(int n:wrong){if(choices.size()==count)break;choices.add(String.valueOf(n));}
        for(int delta=1;choices.size()<count;delta++){
            for(int n:new int[]{answer+delta,answer-delta})
                if(choices.size()<count&&n>=0&&n<=limit&&!choices.contains(""+n))choices.add(""+n);
        }
        Collections.shuffle(choices,r);
        List<Integer> colors=new ArrayList<>();for(String ignored:choices)colors.add(0xff3d195f);
        return new LearningGame.Round(prompt,a+" "+op+" "+b+" = ?",0xff3d195f,choices,colors,choices.indexOf(correct));
    }
    public static String hint(int variant,String display){
        if(variant==TABLES)return "Denk aan gelijke groepjes. Tel het tweede getal steeds opnieuw bij elkaar op.";
        if(variant==SHARING)return "Het tweede getal vertelt hoeveel groepjes je maakt. Verdeel het eerste getal eerlijk.";
        return display.contains("−")?"Trek eerst de tientallen af en daarna de eenheden.":"Tel eerst de tientallen erbij en daarna de eenheden.";
    }
    public static String explain(int variant,String display,String right){
        Matcher digits=Pattern.compile("\\d+").matcher(display);
        if(!digits.find())return "";int a=Integer.parseInt(digits.group());
        if(!digits.find())return "";int b=Integer.parseInt(digits.group());
        if(variant==TABLES)return a+" groepjes van "+b+" maken "+right+".";
        if(variant==SHARING)return b+" × "+right+" = "+a+".";
        int tens=b/10*10,units=b%10;boolean minus=display.contains("−");
        String sign=minus?" − ":" + ";
        int middle=minus?a-tens:a+tens;
        if(tens==0||units==0)return a+sign+b+" = "+right+".";
        return a+sign+tens+" = "+middle+"; "+middle+sign+units+" = "+right+".";
    }
}

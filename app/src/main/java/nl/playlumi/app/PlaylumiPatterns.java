package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Age-specific, uniquely solvable periodic patterns with a single hidden tile. */
public final class PlaylumiPatterns {
    public static final String[] COLORS={"🔴","🔵","🟢","🟡","🟣","🟠"};
    private static final int[] PAINTS={0xffdf364b,0xff2768cf,0xff269a54,0xfff5d02c,0xff9143c2,0xfff07b24};
    private static final int[][][] TEMPLATES={
        {{0,1},{0,0,1},{0,1,1}},
        {{0,0,1},{0,1,1},{0,1,2},{0,1,0,2},{0,0,1,2},{0,1,2,2}},
        {{0,1,2},{0,0,1,2},{0,1,0,2},{0,1,2,1},{0,1,2,3},{0,0,1,1,2},{0,1,0,2,2},{0,1,2,3,2},{0,0,1,2,3},{0,1,2,3,4}}
    };
    private PlaylumiPatterns(){}
    public static int colorIndex(String text){
        for(int i=0;i<COLORS.length;i++)if(COLORS[i].equals(text)||LearningGame.COLOR_NAMES[i].equals(text))return i;
        return -1;
    }
    public static int color(int i){return PAINTS[i];}
    public static String[] tokens(String display){return display.trim().split("\\s+");}
    public static boolean isVisualPattern(String display){
        String[] tokens=tokens(display);int blanks=0;
        if(tokens.length<4)return false;
        for(String t:tokens){
            if("?".equals(t))blanks++;
            else if(colorIndex(t)<0&&!Arrays.asList(LearningGame.SHAPE_SYMBOLS).contains(t))return false;
        }
        return blanks==1;
    }
    /** Check all repeating blocks that can fit twice. Require every compatible
     * candidate to imply the same hidden tile; reject underdetermined puzzles. */
    public static int infer(int[] seen,int maxPeriod){
        int hidden=-1;
        for(int i=0;i<seen.length;i++)if(seen[i]<0){if(hidden>=0)return -1;hidden=i;}
        if(hidden<0)return -1;
        int answer=-1;
        for(int p=1;p<=Math.min(maxPeriod,seen.length/2);p++){
            int[] block=new int[p];Arrays.fill(block,-1);boolean valid=true;
            for(int i=0;i<seen.length;i++)if(seen[i]>=0){
                int k=i%p;if(block[k]>=0&&block[k]!=seen[i]){valid=false;break;}
                block[k]=seen[i];
            }
            if(!valid)continue;
            int expected=block[hidden%p];
            if(expected<0||(answer>=0&&answer!=expected))return -1;
            answer=expected;
        }
        return answer;
    }
    public static LearningGame.Round next(int level,int variant,boolean shapes,Random random){
        if(level<0||level>2)throw new IllegalArgumentException("level");
        int pool=level==0?3:level==1?4:6;
        String[] symbols=shapes?LearningGame.SHAPE_SYMBOLS:COLORS;
        String[] labels=shapes?LearningGame.SHAPE_SYMBOLS:LearningGame.COLOR_NAMES;
        int[] seen=null;int answer=-1,blank=0;
        for(int tries=0;tries<200;tries++){
            int[][] options=TEMPLATES[level];
            int[] template=options[random.nextInt(options.length)];
            if(variant==1&&level>0&&template.length==3&&random.nextBoolean())continue;
            ArrayList<Integer> mapping=new ArrayList<>();
            for(int i=0;i<pool;i++)mapping.add(i);
            Collections.shuffle(mapping,random);
            int period=template.length;
            int length=level==0?2*period+random.nextInt(2):
                    level==1?2*period+1+random.nextInt(2):Math.max(8,2*period+1)+random.nextInt(2);
            blank=level==0||random.nextBoolean()?length-1:1+random.nextInt(length-2);
            int[] candidate=new int[length];
            for(int i=0;i<length;i++)candidate[i]=mapping.get(template[i%period]);
            int expected=candidate[blank];candidate[blank]=-1;
            if(infer(candidate,level==0?3:level==1?4:5)==expected){seen=candidate;answer=expected;break;}
        }
        if(seen==null){
            seen=level==0?new int[]{0,1,0,1,0,-1}:new int[]{0,0,1,2,0,0,1,2,0,0,1,-1};
            blank=seen.length-1;answer=level==0?1:2;
        }
        StringBuilder shown=new StringBuilder();
        for(int i=0;i<seen.length;i++){
            if(i>0)shown.append(' ');
            shown.append(seen[i]<0?"?":symbols[seen[i]]);
        }
        ArrayList<String> answers=new ArrayList<>();for(int i=0;i<pool;i++)answers.add(labels[i]);
        Collections.shuffle(answers,random);
        List<Integer> colors=new ArrayList<>();
        for(String label:answers)colors.add(shapes?0xff49326b:color(colorIndex(label)));
        String kind=shapes?"vorm":"kleur";
        String prompt=blank==seen.length-1?"Welke "+kind+" komt hierna in het patroon?":"Welke "+kind+" ontbreekt in het patroon?";
        return new LearningGame.Round(prompt,shown.toString(),0xff333e53,answers,colors,answers.indexOf(labels[answer]));
    }
}

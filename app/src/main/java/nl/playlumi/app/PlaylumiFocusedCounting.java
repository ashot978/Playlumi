package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/** Count one named shape while ignoring the other shapes in the same field. */
public final class PlaylumiFocusedCounting {
    public static final int MODE=LearningGame.COUNT,VARIANT=5;
    private static final String[] SYMBOLS={"●","▲","■","◆","★","♥"};
    private static final String[] SINGULAR={"cirkel","driehoek","vierkant","ruit","ster","hart"};
    private static final String[] PLURAL={"cirkels","driehoeken","vierkanten","ruiten","sterren","harten"};
    private static final int INK=0xff3d195f;

    private PlaylumiFocusedCounting(){}

    private static String prompt(int shape){
        return "Hoeveel "+PLURAL[shape]+" ("+SYMBOLS[shape]+") zie je?";
    }

    public static LearningGame.Round next(int level,Random random){
        if(level<0||level>2||random==null)throw new IllegalArgumentException("focused counting configuration");
        int maximum=level==0?3:level==1?6:10;
        int shapeCount=level==0?3:level==1?5:SYMBOLS.length;
        int shape=random.nextInt(shapeCount);
        // Zero is introduced after the youngest tier: no target shape is also a quantity.
        int answer=level==0?1+random.nextInt(maximum):random.nextInt(maximum+1);
        int minimumOthers=answer<=1?2:1,maximumOthers=level==0?2:level==1?3:5;
        int others=minimumOthers+random.nextInt(maximumOthers-minimumOthers+1);
        ArrayList<String> objects=new ArrayList<>();
        for(int i=0;i<answer;i++)objects.add(SYMBOLS[shape]);
        for(int i=0;i<others;i++)objects.add(SYMBOLS[(shape+1+random.nextInt(shapeCount-1))%shapeCount]);
        Collections.shuffle(objects,random);
        int columns=level==0?3:5;
        StringBuilder display=new StringBuilder();
        for(int i=0;i<objects.size();i++){
            if(i>0)display.append(i%columns==0?'\n':'\u2003');
            display.append(objects.get(i));
        }

        int choiceCount=level==0?3:4,largestChoice=level==0?3:level==1?9:15;
        LinkedHashSet<Integer> candidates=new LinkedHashSet<>();
        // Older children can distinguish the requested amount from the whole field.
        for(int n:new int[]{objects.size(),others,answer-1,answer+1})
            if(n>=0&&n<=largestChoice&&n!=answer&&(level>0||n>0))candidates.add(n);
        ArrayList<Integer> wrong=new ArrayList<>(candidates);
        Collections.shuffle(wrong,random);
        ArrayList<String> choices=new ArrayList<>();
        String correct=Integer.toString(answer);choices.add(correct);
        for(int n:wrong){if(choices.size()==choiceCount)break;choices.add(Integer.toString(n));}
        for(int n=level==0?1:0;choices.size()<choiceCount;n++)
            if(!choices.contains(Integer.toString(n)))choices.add(Integer.toString(n));
        Collections.shuffle(choices,random);
        List<Integer> colors=new ArrayList<>();for(String ignored:choices)colors.add(INK);
        return new LearningGame.Round(prompt(shape),display.toString(),INK,choices,colors,choices.indexOf(correct));
    }

    /** Explain the saved visible question only after it has been solved. */
    public static String explain(LearningGame.Round round){
        if(round==null||!round.isSolved())return "";
        for(int shape=0;shape<SYMBOLS.length;shape++)if(prompt(shape).equals(round.prompt)){
            int count=0;
            for(int i=0;i<round.display.length();i++)if(round.display.charAt(i)==SYMBOLS[shape].charAt(0))count++;
            return (count==1?"Er is 1 "+SINGULAR[shape]:"Er zijn "+count+" "+PLURAL[shape])+". Je telt alleen deze vorm.";
        }
        return "";
    }
}

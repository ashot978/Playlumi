package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A seven-day cycle with explicit distance and direction, independent of today's date. */
public final class PlaylumiWeekdays {
    public static final int MODE=16,VARIANT=5;
    private static final String[] DAYS={"maandag","dinsdag","woensdag","donderdag","vrijdag","zaterdag","zondag"};
    private static final Pattern DISTANCE=Pattern.compile("([1-6]) (dag|dagen) (eerder|later)");
    private static final int INK=0xff3d195f;

    private PlaylumiWeekdays(){}

    private static int shifted(int day,int distance){
        int result=(day+distance)%DAYS.length;
        return result<0?result+DAYS.length:result;
    }

    public static LearningGame.Round next(int level,Random random){
        if(level<0||level>2||random==null)throw new IllegalArgumentException("weekdays configuration");
        int start=random.nextInt(DAYS.length),distance=level==2?1+random.nextInt(6):1;
        boolean later=random.nextBoolean();
        int end=shifted(start,later?distance:-distance);
        String correct=DAYS[end];
        LinkedHashSet<Integer> candidates=new LinkedHashSet<>();
        candidates.add(start);candidates.add(shifted(start,later?-distance:distance));
        candidates.add(shifted(end,-1));candidates.add(shifted(end,1));candidates.remove(end);
        ArrayList<Integer> wrong=new ArrayList<>(candidates);Collections.shuffle(wrong,random);
        int count=level==0?3:4;
        ArrayList<String> choices=new ArrayList<>();choices.add(correct);
        for(int day:wrong){if(choices.size()==count)break;choices.add(DAYS[day]);}
        for(int day=0;choices.size()<count;day++)if(!choices.contains(DAYS[day]))choices.add(DAYS[day]);
        Collections.shuffle(choices,random);
        ArrayList<Integer> colors=new ArrayList<>();for(String ignored:choices)colors.add(INK);
        String display=DAYS[start]+"\n"+distance+(distance==1?" dag":" dagen")+(later?" later":" eerder");
        return new LearningGame.Round("Welke dag is het dan?",display,INK,choices,colors,choices.indexOf(correct));
    }

    /** Read the persisted question, never the current wall clock or a newly generated question. */
    public static String explain(LearningGame.Round round){
        if(round==null||!round.isSolved())return "";
        String[] lines=round.display.split("\n");if(lines.length!=2)return "";
        int start=-1;for(int day=0;day<DAYS.length;day++)if(DAYS[day].equals(lines[0]))start=day;
        Matcher match=DISTANCE.matcher(lines[1]);if(start<0||!match.matches())return "";
        int distance=Integer.parseInt(match.group(1));
        if(!(distance==1?"dag":"dagen").equals(match.group(2)))return "";
        boolean later=match.group(3).equals("later");
        return distance+(distance==1?" dag ":" dagen ")+(later?"na ":"vóór ")+DAYS[start]+" is "+
                DAYS[shifted(start,later?distance:-distance)]+".";
    }
}

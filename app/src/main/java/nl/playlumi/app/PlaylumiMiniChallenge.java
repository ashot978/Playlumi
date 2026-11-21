package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Independently testable rules for three hands-on preschool minigames. */
public final class PlaylumiMiniChallenge {
    public static final int DRAG=0, BALLOONS=1, MEMORY=2;
    public static final String[] NAMES={"Sleep de vormen","Tik de ballon","Memory"};
    public static final String[] ICONS={"🔷","🎈","🃏"};
    public final int kind,level;
    public final String question,target;
    public final List<String> choices;
    public final int correctIndex;
    private final boolean[] matched;
    private int first=-1,second=-1,matchedPairs;
    private static final String[] SHAPES={"⭐","🔺","🔵","🟩","💜","🔶","❤️","⬛","🔷","🔸","🟢","⬜"};
    private static final String[] SHAPE_NAMES={"ster","driehoek","cirkel","vierkant","hart",
            "ruit","hart","vierkant","ruit","ruit","cirkel","vierkant"};
    private static final String[] MEMORY_ICONS={"🐶","🐱","🐮","🐸","🐰","🐻",
            "🦊","🐵","🐘","🐝","🦋","🐢","🐧","🦁","🐼","🐟","🐔","🐙"};

    private PlaylumiMiniChallenge(int kind,int level,String question,String target,
                                  List<String> choices,int answerIndex) {
        this.kind=kind;this.level=level;this.question=question;this.target=target;
        this.choices=Collections.unmodifiableList(new ArrayList<>(choices));
        this.correctIndex=answerIndex;
        this.matched=new boolean[choices.size()];
    }
    public static PlaylumiMiniChallenge generate(int kind,int level,Random random) {
        if (kind<0 || kind>2 || level<0 || level>2) throw new IllegalArgumentException("invalid mini game");
        int count=level==0?3:level==1?4:6;
        List<String> options=new ArrayList<>();
        if(kind==DRAG) {
            // Every target is a specific shape symbol, never an ambiguous shape name.
            int available=level==0?7:level==1?8:SHAPES.length;
            List<Integer> indices=uniqueIndices(random,count,available);
            for(int index:indices)options.add(SHAPES[index]);
            int answer=random.nextInt(options.size());
            return new PlaylumiMiniChallenge(kind,level,"Sleep de juiste vorm naar het huisje",
                    options.get(answer),options,answer);
        }
        if(kind==BALLOONS) {
            int maximum=level==0?9:level==1?20:50;
            List<Integer> nums=uniqueIndices(random,count,maximum);
            for(int number:nums)options.add(""+(number+1));
            int answer=random.nextInt(options.size());
            return new PlaylumiMiniChallenge(kind,level,"Tik op de ballon met "+options.get(answer),
                    options.get(answer),options,answer);
        }
        int pairs=level==0?3:level==1?4:6;
        List<Integer> indices=uniqueIndices(random,pairs,MEMORY_ICONS.length);
        for(int index:indices) {options.add(MEMORY_ICONS[index]);options.add(MEMORY_ICONS[index]);}
        Collections.shuffle(options,random);
        return new PlaylumiMiniChallenge(kind,level,"Zoek twee dezelfde diertjes","🐾",options,-1);
    }
    private static List<Integer> uniqueIndices(Random r,int count,int range) {
        if(count>range)throw new IllegalArgumentException("too many");
        List<Integer> values=new ArrayList<>();
        for(int i=0;i<range;i++)values.add(i);
        Collections.shuffle(values,r);
        return values.subList(0,count);
    }
    /** Clicks only work in tap-based games. A shape MUST arrive via a drag drop. */
    public boolean choose(int index) {
        if(kind==MEMORY)throw new IllegalStateException("memory requires flip");
        if(kind==DRAG)throw new IllegalStateException("shape requires drag and drop");
        check(index);
        return correctIndex==index;
    }
    public boolean drop(int index) {
        if(kind!=DRAG)throw new IllegalStateException("only shapes can be dropped");
        check(index);
        return correctIndex==index;
    }
    private void check(int index) {
        if(index<0 || index>=choices.size()) throw new IllegalArgumentException("tile");
    }
    /** Returns 0 ignored, 1 first card, 2 pair found, 3 game solved, -1 wrong pair. */
    public int flip(int index) {
        if(kind!=MEMORY)throw new IllegalStateException("not memory");
        check(index);
        if(first>=0 && second>=0) return 0;
        if(matched[index] || index==first) return 0;
        if(first<0) {first=index;return 1;}
        second=index;
        if(choices.get(first).equals(choices.get(second))) {
            matched[first]=true;matched[second]=true;
            matchedPairs++;
            first=-1;second=-1;
            return matchedPairs*2==choices.size()?3:2;
        }
        return -1;
    }
    public void hideMiss() {
        if(first>=0 && second>=0) {first=-1;second=-1;}
    }
    public boolean visible(int index) {
        check(index);
        return matched[index] || first==index || second==index;
    }
    public boolean matched(int index) {check(index);return matched[index];}
    public int matchedPairs(){return matchedPairs;}
    public int pairCount(){return choices.size()/2;}
    public boolean solved() {
        return kind==MEMORY && matchedPairs==pairCount();
    }
}

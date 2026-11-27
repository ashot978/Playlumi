package nl.playlumi.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Independently testable rules for three hands-on preschool minigames. */
public final class PlaylumiMiniChallenge {
    public static final int DRAG=0, BALLOONS=1, MEMORY=2;
    public static final String[] NAMES={"Sleep de vormen","Tel de ballonnen","Memory"};
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
            int available=level==0?5:level==1?8:SHAPES.length;
            List<Integer> indices=uniqueIndices(random,count,available);
            for(int index:indices)options.add(SHAPES[index]);
            int answer=random.nextInt(options.size());
            return new PlaylumiMiniChallenge(kind,level,"Sleep de juiste vorm naar het huisje",
                    options.get(answer),options,answer);
        }
        if(kind==BALLOONS) {
            // Count small concrete quantities instead of matching printed numbers.
            int maximum=level==0?3:level==1?5:8;
            List<Integer> nums=uniqueIndices(random,count,maximum);
            for(int number:nums)options.add(""+(number+1));
            int answer=random.nextInt(options.size());
            return new PlaylumiMiniChallenge(kind,level,"Waar zie je "+options.get(answer)+(options.get(answer).equals("1")?" ballon?":" ballonnen?"),
                    options.get(answer),options,answer);
        }
        return new PlaylumiMiniChallenge(kind,level,"Zoek twee dezelfde diertjes","🐾",PlaylumiMiniPractice.memory(level,random),-1);
    }
    public static PlaylumiMiniChallenge restoreDrag(int level,String target,List<String> choices,int correctIndex){
        if(level<0||level>2||target==null||choices==null||choices.size()!=(level==0?3:level==1?4:6)||
                correctIndex<0||correctIndex>=choices.size()||!target.equals(choices.get(correctIndex))||
                new java.util.HashSet<>(choices).size()!=choices.size()||
                !java.util.Arrays.asList(SHAPES).containsAll(choices))
            throw new IllegalArgumentException("Invalid saved shape round");
        return new PlaylumiMiniChallenge(DRAG,level,"Sleep de juiste vorm naar het huisje",target,choices,correctIndex);
    }
    public int matchedMask(){
        int mask=0;for(int i=0;i<matched.length;i++)if(matched[i])mask|=1<<i;
        return mask;
    }
    /** A mismatched pair is closed on restore; keep a single open card. */
    public int firstOpen(){return second<0?first:-1;}
    public static PlaylumiMiniChallenge restore(int kind,int level,String target,List<String> choices,int correctIndex,int mask,int open){
        if(level<0||level>2||kind<DRAG||kind>MEMORY||choices==null||target==null)
            throw new IllegalArgumentException("saved mini game");
        if(kind==DRAG)return restoreDrag(level,target,choices,correctIndex);
        int count=level==0?3:level==1?4:6;
        if(kind==BALLOONS){
            if(choices.size()!=count||correctIndex<0||correctIndex>=count||!target.equals(choices.get(correctIndex))||
                    new java.util.HashSet<>(choices).size()!=count)throw new IllegalArgumentException("saved balloon choices");
            int maximum=level==0?3:level==1?5:8;
            for(String value:choices){int n=Integer.parseInt(value);if(n<1||n>maximum)throw new IllegalArgumentException("saved balloon count");}
            return new PlaylumiMiniChallenge(kind,level,"Waar zie je "+target+(target.equals("1")?" ballon?":" ballonnen?"),target,choices,correctIndex);
        }
        if(choices.size()!=count*2||correctIndex!=-1||mask<0||(mask&~((1<<choices.size())-1))!=0||open<-1||open>=choices.size())
            throw new IllegalArgumentException("saved memory layout");
        PlaylumiMiniChallenge saved=new PlaylumiMiniChallenge(kind,level,"Zoek twee dezelfde diertjes","🐾",choices,-1);
        for(int i=0;i<choices.size();i++){
            String value=choices.get(i);
            if(PlaylumiMiniPractice.animalName(value).isEmpty()||Collections.frequency(choices,value)!=2)
                throw new IllegalArgumentException("saved memory pair");
            boolean marked=(mask&(1<<i))!=0;
            for(int j=0;j<choices.size();j++)if(value.equals(choices.get(j))&&marked!=((mask&(1<<j))!=0))
                throw new IllegalArgumentException("half pair");
            saved.matched[i]=marked;
        }
        if(open>=0&&saved.matched[open])throw new IllegalArgumentException("open matched card");
        saved.matchedPairs=Integer.bitCount(mask)/2;saved.first=open;
        return saved;
    }
    private static List<Integer> uniqueIndices(Random r,int count,int range) {
        if(count>range)throw new IllegalArgumentException("too many");
        List<Integer> values=new ArrayList<>();
        for(int i=0;i<range;i++)values.add(i);
        Collections.shuffle(values,r);
        return values.subList(0,count);
    }
    /** A balloon card represents a quantity, never a printed number. */
    public int balloonCount(int index){
        if(kind!=BALLOONS)throw new IllegalStateException("not balloons");
        check(index);
        return Integer.parseInt(choices.get(index));
    }
    public int targetBalloonCount(){
        if(kind!=BALLOONS)throw new IllegalStateException("not balloons");
        return balloonCount(correctIndex);
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

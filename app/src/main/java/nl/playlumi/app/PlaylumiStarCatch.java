package nl.playlumi.app;

import java.util.Random;

/** Count and recognize stars: hearts are distractors and can never earn progress. */
public final class PlaylumiStarCatch {
    public static final int ROUNDS=10;
    private final int target,total,starMask;
    private int collectedMask;
    private PlaylumiStarCatch(int target,int total,int starMask,int mask) {
        this.target=target;this.total=total;this.starMask=starMask;this.collectedMask=mask;
    }
    public static PlaylumiStarCatch generate(int age,Random random) {
        if(age<0 || age>2)throw new IllegalArgumentException("age");
        int count=3+age,total=count+2+random.nextInt(2);
        int stars=0;
        while(Integer.bitCount(stars)<count)stars|=1<<random.nextInt(total);
        return new PlaylumiStarCatch(count,total,stars,0);
    }
    public static PlaylumiStarCatch restore(int target,int total,int starMask,int mask) {
        if(target<3 || target>5 || total<target+2 || total>8 ||
                starMask<0 || (starMask&~((1<<total)-1))!=0 ||
                Integer.bitCount(starMask)!=target || mask<0 || (mask&~starMask)!=0)
            throw new IllegalArgumentException("invalid star collection");
        return new PlaylumiStarCatch(target,total,starMask,mask);
    }
    public int target(){return target;}
    public int total(){return total;}
    public int collected(){return Integer.bitCount(collectedMask);}
    public int mask(){return collectedMask;}
    public int starMask(){return starMask;}
    private void check(int index){
        if(index<0 || index>=total)throw new IllegalArgumentException("tile");
    }
    public boolean isStar(int index){check(index);return (starMask&(1<<index))!=0;}
    public boolean caught(int index){check(index);return (collectedMask&(1<<index))!=0;}
    /** A heart, an already-collected star or any press after completion never counts. */
    public boolean catchStar(int index){
        if(!isStar(index) || finished() || caught(index))return false;
        collectedMask|=1<<index;
        return true;
    }
    public boolean finished(){return collected()==target;}
}

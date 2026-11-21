package nl.playlumi.app;

import java.util.Random;

/** A no-timer sensory catching game for very young children. */
public final class PlaylumiStarCatch {
    public static final int ROUNDS=10;
    private final int target,total;
    private int collectedMask;
    private PlaylumiStarCatch(int target,int total,int mask) {
        this.target=target;this.total=total;this.collectedMask=mask;
    }
    public static PlaylumiStarCatch generate(int age,Random random) {
        if(age<0 || age>2)throw new IllegalArgumentException("age");
        int count=3+age;
        return new PlaylumiStarCatch(count,count+2+random.nextInt(2),0);
    }
    public static PlaylumiStarCatch restore(int target,int total,int mask) {
        if(target<3 || target>5 || total<target+2 || total>8 ||
                mask<0 || (mask&~((1<<total)-1))!=0 ||
                Integer.bitCount(mask)>target)
            throw new IllegalArgumentException("invalid star collection");
        return new PlaylumiStarCatch(target,total,mask);
    }
    public int target(){return target;}
    public int total(){return total;}
    public int collected(){return Integer.bitCount(collectedMask);}
    public int mask(){return collectedMask;}
    public boolean caught(int index){
        if(index<0 || index>=total)throw new IllegalArgumentException("star");
        return (collectedMask&(1<<index))!=0;
    }
    /** Only an actual unique star press counts; no duplicate rewards. */
    public boolean catchStar(int index){
        if(index<0 || index>=total)throw new IllegalArgumentException("star");
        if(finished() || caught(index))return false;
        collectedMask|=1<<index;
        return true;
    }
    public boolean finished(){return collected()==target;}
}

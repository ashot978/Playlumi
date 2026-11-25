package nl.playlumi.app;
import java.util.Random;

/** Calm counting/shape recognition: only real stars can ever be collected. */
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
        int[] slots=new int[total];
        for(int i=0;i<total;i++)slots[i]=i;
        for(int i=total-1;i>0;i--){
            int j=random.nextInt(i+1),tmp=slots[i];slots[i]=slots[j];slots[j]=tmp;
        }
        int stars=0;
        for(int i=0;i<count;i++)stars|=1<<slots[i];
        return new PlaylumiStarCatch(count,total,stars,0);
    }
    public static PlaylumiStarCatch restore(int target,int total,int starMask,int mask) {
        if(target<3 || target>5 || total<target+2 || total>8 ||
                starMask<0 || (starMask&~((1<<total)-1))!=0 ||
                Integer.bitCount(starMask)!=target || mask<0 ||
                (mask&~starMask)!=0)
            throw new IllegalArgumentException("invalid star collection");
        return new PlaylumiStarCatch(target,total,starMask,mask);
    }
    public int target(){return target;}
    public int total(){return total;}
    public int collected(){return Integer.bitCount(collectedMask);}
    public int mask(){return collectedMask;}
    public int starMask(){return starMask;}
    private void check(int index){
        if(index<0 || index>=total)throw new IllegalArgumentException("star");
    }
    public boolean isStar(int index){check(index);return (starMask&(1<<index))!=0;}
    public boolean caught(int index){check(index);return (collectedMask&(1<<index))!=0;}
    public boolean catchStar(int index){
        check(index);
        if(!isStar(index)||finished()||caught(index))return false;
        collectedMask|=1<<index;
        return true;
    }
    public boolean finished(){return collected()==target;}
}

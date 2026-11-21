package nl.playlumi.app;

import java.util.Random;

/** Pop the requested number of bubbles, without wrong-answer penalties. */
public final class PlaylumiBubbles {
    public final int target,total,level;
    private int poppedMask;
    public static PlaylumiBubbles generate(int age,Random rng) {
        if(age<0 || age>2)throw new IllegalArgumentException("age");
        int min=age==0?2:age==1?3:5;
        int max=age==0?4:age==1?6:9;
        int target=min+rng.nextInt(max-min+1);
        int total=Math.min(12,target+1+rng.nextInt(3));
        return new PlaylumiBubbles(age,target,total,0);
    }
    public static PlaylumiBubbles restore(int age,int target,int total,int mask) {
        if(age<0 || age>2 || target<1 || total<=target || total>12 ||
                (mask&~((1<<total)-1))!=0 || Integer.bitCount(mask)>target)
            throw new IllegalArgumentException("invalid bubble state");
        return new PlaylumiBubbles(age,target,total,mask);
    }
    private PlaylumiBubbles(int age,int requested,int available,int mask) {
        level=age;target=requested;total=available;poppedMask=mask;
    }
    public int popped(){return Integer.bitCount(poppedMask);}
    public int mask(){return poppedMask;}
    public boolean isPopped(int index){
        if(index<0 || index>=total)throw new IllegalArgumentException("bubble");
        return (poppedMask&(1<<index))!=0;
    }
    public boolean pop(int index) {
        if(index<0 || index>=total)throw new IllegalArgumentException("bubble");
        if(solved() || isPopped(index))return false;
        poppedMask|=1<<index;
        return true;
    }
    public boolean solved(){return popped()==target;}
}

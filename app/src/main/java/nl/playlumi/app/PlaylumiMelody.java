package nl.playlumi.app;

import java.util.Random;

/**
 * Listen-and-repeat challenge with four original notes.
 * Wrong taps simply restart this tune; no streak loss or negative score.
 */
public final class PlaylumiMelody {
    public static final int NOTES=4, ROUNDS=12;
    public static final String[] ANIMALS={"🐸","🐱","🐥","🦋"};
    public static final String[] NAMES={"Kikker","Katje","Kuikentje","Vlinder"};
    private final int[] tune;
    private final int[] buttons;
    private int position;
    private boolean completed;
    private PlaylumiMelody(int[] notes,int heard,int[] order) {
        tune=notes.clone();
        buttons=order.clone();
        position=Math.max(0,Math.min(notes.length,heard));
        completed=position==notes.length;
    }
    public static PlaylumiMelody generate(int age,Random random){
        if(age<0 || age>2)throw new IllegalArgumentException("age");
        int count=age+2;
        int[] notes=new int[count];
        for(int i=0;i<count;i++)notes[i]=random.nextInt(NOTES);
        java.util.List<Integer> keys=new java.util.ArrayList<>();
        for(int i=0;i<NOTES;i++)keys.add(i);
        java.util.Collections.shuffle(keys,random);
        int[] order=new int[NOTES];
        for(int i=0;i<NOTES;i++)order[i]=keys.get(i);
        return new PlaylumiMelody(notes,0,order);
    }
    public static PlaylumiMelody restore(int[] notes,int position) {
        return restore(notes,position,new int[]{0,1,2,3});
    }
    public static PlaylumiMelody restore(int[] notes,int position,int[] order) {
        if(notes==null || notes.length<2 || notes.length>4 || position<0||
                position>notes.length || order==null || order.length!=NOTES)
            throw new IllegalArgumentException("invalid melody");
        for(int n:notes)if(n<0||n>=NOTES)throw new IllegalArgumentException("note");
        boolean[] seen=new boolean[NOTES];
        for(int n:order) {
            if(n<0 || n>=NOTES || seen[n])throw new IllegalArgumentException("layout");
            seen[n]=true;
        }
        return new PlaylumiMelody(notes,position,order);
    }
    public int[] buttonOrder(){return buttons.clone();}
    public int[] sequence(){return tune.clone();}
    public int length(){return tune.length;}
    public int progress(){return position;}
    public boolean completed(){return completed;}
    public int noteAt(int i){
        if(i<0||i>=tune.length)throw new IllegalArgumentException("position");
        return tune[i];
    }
    /** 0 = try again, 1 = correct so far, 2 = finished, 3 = already finished. */
    public int tap(int note) {
        if(note<0 || note>=NOTES)throw new IllegalArgumentException("note");
        if(completed)return 3;
        if(tune[position]!=note) {
            position=0;
            return 0;
        }
        position++;
        if(position==tune.length){completed=true;return 2;}
        return 1;
    }
}

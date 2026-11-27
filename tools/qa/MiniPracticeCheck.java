package nl.playlumi.app;
import java.util.*;

public final class MiniPracticeCheck {
    private static void require(boolean valid,String message){if(!valid)throw new AssertionError(message);}
    public static void main(String[] args){
        Random r=new Random(1192026);Set<String> animals=new HashSet<>(),themes=new HashSet<>();
        int boards=0,restores=0,tunes=0;
        for(int age=0;age<3;age++)for(int i=0;i<400;i++){
            PlaylumiMiniChallenge c=PlaylumiMiniChallenge.generate(2,age,r);
            animals.addAll(c.choices);themes.add(PlaylumiMiniPractice.theme(c.choices));
            require(new HashSet<>(c.choices).size()==c.pairCount(),"Distinct pairs");
            int other=1;while(c.choices.get(0).equals(c.choices.get(other)))other++;
            require(c.flip(0)==1&&c.flip(other)==-1&&c.flip(2)==0,"Memory miss lock");
            require(c.matchedPairs()==0&&c.firstOpen()==-1,"Miss credited");
            c.hideMiss();
            while(!c.solved()){
                int first=0;while(c.matched(first))first++;
                int mate=c.choices.lastIndexOf(c.choices.get(first));
                require(mate>first,"Matching animal");
                require(PlaylumiMiniPractice.cardLabel(first,false,false,c.choices.get(first)).equals("Gesloten kaart "+(first+1)),"Hidden animal exposed");
                require(c.flip(first)==1,"First open");
                PlaylumiMiniChallenge restored=PlaylumiMiniChallenge.restore(2,age,c.target,c.choices,-1,c.matchedMask(),c.firstOpen());
                require(restored.visible(first)&&restored.firstOpen()==first&&restored.choices.equals(c.choices),"Open card changed");
                int a=c.flip(mate),b=restored.flip(mate);
                require(a==b&&c.matchedMask()==restored.matchedMask()&&c.matchedPairs()==restored.matchedPairs(),"Pair restore changed");
                restores++;
            }
            require(c.flip(0)==0,"Solved board replay");
            boards++;
            for(int variant=0;variant<=age;variant++){
                PlaylumiMelody m=PlaylumiMelody.generate(age,variant,r);
                for(int pos=0;pos<=m.length();pos++){
                    String[] tokens=m.playedTrail().split("  ");
                    require(tokens.length==m.length(),"Trail length");
                    for(int j=0;j<tokens.length;j++)require(tokens[j].equals(j<pos?PlaylumiMelody.ANIMALS[m.noteAt(j)]:"○"),"Future note exposed");
                    PlaylumiMelody restored=PlaylumiMelody.restore(m.sequence(),m.progress());
                    require(restored.playedTrail().equals(m.playedTrail()),"Melody resume");
                    if(pos<m.length())m.tap(m.noteAt(pos));
                }
                tunes++;
            }
        }
        require(animals.size()==32&&themes.size()==4,"Animal bank coverage");
        String[] old={"🐶","🐱","🐮","🐸","🐰","🐻","🦊","🐵","🐘","🐝","🦋","🐢","🐧","🦁","🐼","🐟","🐔","🐙"};
        for(int i=0;i<old.length;i+=3){
            List<String> cards=Arrays.asList(old[i],old[i+1],old[i+2],old[i],old[i+1],old[i+2]);
            require(PlaylumiMiniChallenge.restore(2,0,"🐾",cards,-1,9,1).choices.equals(cards),"Legacy cards lost");
        }
        System.out.println("PASS: "+boards+" themed memory boards, "+restores+" exact card restores, "+tunes+" melody trails and all 18 legacy animal cards.");
    }
}

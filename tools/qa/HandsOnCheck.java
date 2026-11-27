package nl.playlumi.app;
import java.util.*;

public final class HandsOnCheck {
    private static void require(boolean good,String message){if(!good)throw new AssertionError(message);}
    private static String legacyHabitat(String a){
        switch(a){case "🐠":case "🐬":return "🌊 Zee";case "🐄":case "🐑":return "🌾 Boerderij";case "🐒":case "🦜":return "🌳 Bos";default:throw new AssertionError(a);}
    }
    private static void legacy(int age,long seed,int version){
        Random r=new Random(seed);String[] animals={"🐠","🐄","🐒","🐬","🐑","🦜"};
        List<Integer> indices=new ArrayList<>();for(int i=0;i<animals.length;i++)indices.add(i);
        Collections.shuffle(indices,r);
        PlaylumiInteractive h=new PlaylumiInteractive(2,age,seed,0,version);
        require(h.total()==3+age&&h.options().equals(Arrays.asList("🌊 Zee","🌾 Boerderij","🌳 Bos")),"Legacy habitats changed");
        for(int i=0;i<h.total();i++){
            String a=animals[indices.get(i)];require(h.target(i).equals(a)&&h.expected().equals(legacyHabitat(a)),"Legacy animal changed");
            require(h.choose(h.expected()),"Legacy animal");
        }
        r=new Random(seed);String[] colors={"🔴 Rood","🔵 Blauw","🟡 Geel","🟢 Groen"};
        PlaylumiInteractive m=new PlaylumiInteractive(3,age,seed,0,version);
        require(m.total()==4+age&&m.options().equals(Arrays.asList(colors).subList(0,2+age)),"Legacy mosaic changed");
        for(int i=0;i<m.total();i++){
            String expected=colors[r.nextInt(2+age)];require(m.target(i).equals(expected)&&m.expected().equals(expected),"Legacy color changed");
            m.choose(expected);
        }
    }
    private static void compare(PlaylumiInteractive a,PlaylumiInteractive b){
        require(a.total()==b.total()&&a.options().equals(b.options())&&a.progress()==b.progress()&&a.expected().equals(b.expected()),"Restore differs");
        require(a.step()==b.step()&&a.clue().equals(b.clue())&&a.picture().equals(b.picture()),"Restore clue differs");
        for(int i=0;i<a.total();i++)require(a.target(i).equals(b.target(i)),"Restore target differs");
    }
    public static void main(String[] args){
        Map<String,String> habitats=new HashMap<>();
        for(String a:new String[]{"🐠","🐬","🐙"})habitats.put(a,"🌊 Zee");
        for(String a:new String[]{"🐄","🐑","🐔"})habitats.put(a,"🌾 Boerderij");
        for(String a:new String[]{"🐿️","🦉","🦌"})habitats.put(a,"🌳 Bos");
        for(String a:new String[]{"🐪","🦂","🦊"})habitats.put(a,"🏜️ Woestijn");
        for(String a:new String[]{"🐧","🐻‍❄️"})habitats.put(a,"❄️ Poolgebied");
        int checked=0,restores=0;
        for(int age=0;age<3;age++)for(int seed=0;seed<240;seed++){
            legacy(age,seed,1);legacy(age,seed,2);
            for(int kind=0;kind<4;kind++){
                PlaylumiInteractive g=PlaylumiInteractive.start(kind,age,seed);
                require(g.version==3,"New round version");
                if(kind<2)compare(g,new PlaylumiInteractive(kind,age,seed,0,2));
                Set<String> used=new HashSet<>(),answers=new HashSet<>();
                if(kind==2)require(g.total()==3+2*age&&g.options().size()==3+age,"Habitat depth");
                if(kind==3)require(g.total()==new int[]{4,6,9}[age]&&g.mosaicColumns()==new int[]{2,3,3}[age]&&g.options().size()==new int[]{2,3,5}[age],"Mosaic geometry");
                while(!g.solved()){
                    String target=g.target(g.progress()),correct=g.expected();
                    if(kind==2)require(used.add(target)&&correct.equals(habitats.get(target))&&!g.animalName(g.progress()).isEmpty(),"Habitat content");
                    if(kind==3)require(correct.equals(target),"Mosaic mismatch");
                    answers.add(correct);
                    int n=g.progress();
                    for(String option:g.options())if(!option.equals(correct))require(!g.choose(option)&&g.progress()==n,"Incorrect choice changed progress");
                    require(g.choose(correct),"Valid choice rejected");
                    compare(g,new PlaylumiInteractive(kind,age,seed,g.progress(),3));restores++;
                }
                if(kind>=2)require(answers.equals(new HashSet<>(g.options())),"Unused home/color");
                require(!g.choose(g.options().get(0)),"Repeated completed round");
                checked++;
            }
        }
        System.out.println("PASS: "+checked+" version-three interactive boards, "+restores+" exact step restores, 1440 legacy habitat/mosaic pairs, real grids and independently verified habitats.");
    }
}

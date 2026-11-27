package nl.playlumi.app;
import java.util.*;

public final class PuzzleCheck {
    private static void require(boolean valid,String message){if(!valid)throw new AssertionError(message);}
    private static boolean adjacent(PlaylumiPuzzle p,int a,int b){return Math.abs(a%p.cols-b%p.cols)+Math.abs(a/p.cols-b/p.cols)==1;}
    private static void reject(Runnable test){try{test.run();}catch(IllegalArgumentException expected){return;}throw new AssertionError("Invalid snapshot accepted");}
    public static void main(String[] args){
        Random r=new Random(1202026);int boards=0,restores=0;
        for(int age=0;age<3;age++)for(int kind=4;kind<=12;kind++){
            PlaylumiGameCatalog.Entry entry=PlaylumiGameCatalog.route(age,PlaylumiPuzzle.world(kind),PlaylumiGameCatalog.INTERACTIVE,kind,0);
            require((entry!=null)==PlaylumiPuzzle.offered(kind,age),"Age-specific route");
            if(entry==null)continue;
            for(int seed=0;seed<320;seed++){
                PlaylumiInteractive game=PlaylumiInteractive.start(kind,age,seed);PlaylumiPuzzle p=game.puzzle();
                require(game.version==4&&p.cells()==p.cols*p.rows&&p.cells()<=25,"Board geometry");
                String topic=p.topic();int last=p.startCell(),number=-1;StringBuilder word=new StringBuilder();
                int difference=0;
                for(int i=0;i<p.cells();i++)if((kind==9||kind==11)&&!p.original(i).equals(p.reference(i)))difference++;
                if(kind==9||kind==11)require(difference==p.total(),"Difference count");
                while(!game.solved()){
                    ArrayList<Integer> good=new ArrayList<>();
                    for(int i=0;i<p.cells();i++)if(p.accepts(i))good.add(i);else require(!p.tap(i),"Incorrect tile accepted");
                    require(!good.isEmpty()&&(p.unordered()||good.size()==1),"Playable board");
                    int next=good.get(r.nextInt(good.size()));
                    if(kind==4){int n=Integer.parseInt(p.original(next));if(number>=0)require(n>number,"Number sequence");number=n;}
                    if(kind==5){if(last>=0)require(adjacent(p,last,next),"Nonadjacent word path");word.append(p.original(next));}
                    if(kind==6)require(adjacent(p,last,next)&&!p.blocked(next),"Invalid maze path");
                    if(kind==7)require(p.original(next).equals(p.reference(p.progress())),"Wrong rhythm");
                    if(kind==9||kind==11)require(!p.original(next).equals(p.reference(next)),"Not a difference");
                    if(kind==10){String[] s=p.prompt().split(" ");int a=Integer.parseInt(s[0]),b=Integer.parseInt(s[2]);require(Integer.parseInt(p.original(next))==(s[1].equals("+")?a+b:a-b),"Arithmetic");}
                    if(kind==12)require(p.prompt().endsWith(p.coordinate(next)),"Map coordinate");
                    last=next;require(game.choose(""+next),"Valid move rejected");
                    PlaylumiInteractive restored=new PlaylumiInteractive(kind,age,seed,game.progress(),game.version,game.mask());
                    require(restored.mask()==game.mask()&&restored.progress()==game.progress()&&restored.expected().equals(game.expected()),"Restore state");
                    for(int i=0;i<p.cells();i++)require(restored.puzzle().display(i).equals(p.display(i)),"Restored board changed");
                    restores++;
                }
                if(kind==5)require(word.toString().equals(topic),"Wrong word");
                if(kind==6)require(last==p.goalCell(),"Maze goal");
                if(kind==9||kind==11)for(int i=0;i<p.cells();i++)require(p.display(i).equals(p.reference(i)),"Unrepaired tile");
                require(!game.choose("0"),"Solved board awarded again");
                final int k=kind,a=age,s=seed;reject(()->new PlaylumiPuzzle(k,a,s,0,1<<29));
                boards++;
            }
        }
        System.out.println("PASS: "+boards+" distinct age-scaled puzzle boards and "+restores+" exact move restores; rejected moves, real maze/word paths, arithmetic and repaired pixels.");
    }
}

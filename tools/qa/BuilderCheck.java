package nl.playlumi.app;

import java.util.*;

public final class BuilderCheck {
    private static void require(boolean good,String message){if(!good)throw new AssertionError(message);}
    private static int checkNetwork(String cells,int size){
        // Independently search each house towards any station, without crossing another house.
        int[] bits={1,2,4,8},dx={0,1,0,-1},dy={-1,0,1,0};
        String code=".HSTFWabcdefgh";
        int[] ports={0,15,15,0,0,0,10,5,3,6,12,9,10,5};
        int total=0;
        for(int start=0;start<cells.length();start++){
            if(cells.charAt(start)!='H')continue;
            boolean[] seen=new boolean[cells.length()];ArrayDeque<Integer> q=new ArrayDeque<>();q.add(start);seen[start]=true;
            boolean reaches=false;
            while(!q.isEmpty()){
                int pos=q.removeFirst();char piece=cells.charAt(pos);
                if(piece=='S'){reaches=true;break;}
                if(piece=='H'&&pos!=start)continue;
                for(int d=0;d<4;d++){
                    int x=pos%size+dx[d],y=pos/size+dy[d];if(x<0||y<0||x>=size||y>=size)continue;
                    int next=y*size+x;
                    if(!seen[next]&&(ports[code.indexOf(piece)]&bits[d])!=0&&(ports[code.indexOf(cells.charAt(next))]&bits[(d+2)%4])!=0){
                        seen[next]=true;q.add(next);
                    }
                }
            }
            if(reaches)total++;
        }
        return total;
    }
    public static void main(String[] args){
        Random r=new Random(1222026);
        int designs=0,networks=0,restores=0,edits=0;
        for(int age=0;age<3;age++){
            int done=0,bonus=0;
            for(int slot=0;slot<12;slot++){
                String goal=PlaylumiBuilder.blueprint(age,slot);
                PlaylumiBuilder.State state=new PlaylumiBuilder.State(age,slot,"");
                require(goal.equals(PlaylumiBuilder.blueprint(age,slot)),"Blueprint unstable");
                require(checkNetwork(goal,state.size)==1&&PlaylumiBuilder.inspect(goal,state.size).complete(),"Invalid blueprint");
                ArrayList<Integer> positions=new ArrayList<>();for(int i=0;i<goal.length();i++)positions.add(i);Collections.shuffle(positions,r);
                for(int i:positions){
                    state.place(i,goal.charAt(i));
                    PlaylumiBuilder.State restored=new PlaylumiBuilder.State(age,slot,state.snapshot());
                    require(restored.snapshot().equals(state.snapshot()),"Blueprint restore");
                    if(!restored.matches())require(PlaylumiBuilder.complete(done,restored).bonus==0,"Partial reward");
                    state=restored;restores++;
                }
                require(state.matches(),"Cannot build exact plan");
                PlaylumiBuilder.Completion result=PlaylumiBuilder.complete(done,state);
                require(result.after==done+1,"One stage per plan");done=result.after;bonus+=result.bonus;
                require(PlaylumiBuilder.complete(done,state).bonus==0,"Duplicate award");designs++;
            }
            require(done==12&&bonus==90,"All design rewards");
            for(int slot=12;slot<15;slot++)for(int scenario=0;scenario<80;scenario++){
                PlaylumiBuilder.State state=new PlaylumiBuilder.State(age,slot,"");
                ArrayList<String> undo=new ArrayList<>(),redo=new ArrayList<>();
                String board=state.cells(),codes=".HSTFWabcdefgh";
                for(int action=0;action<60;action++){
                    int operation=r.nextInt(8);
                    if(operation==0){
                        boolean available=!undo.isEmpty();require(state.undo()==available,"Undo availability");
                        if(available){redo.add(board);board=undo.remove(undo.size()-1);}
                    }else if(operation==1){
                        boolean available=!redo.isEmpty();require(state.redo()==available,"Redo availability");
                        if(available){if(undo.size()==16)undo.remove(0);undo.add(board);board=redo.remove(redo.size()-1);}
                    }else{
                        String next;
                        if(operation==2){char[] clear=new char[board.length()];Arrays.fill(clear,'.');next=new String(clear);require(state.clear()==!next.equals(board),"Clear");}
                        else{int pos=r.nextInt(board.length());char piece=codes.charAt(r.nextInt(codes.length()));char[] change=board.toCharArray();change[pos]=piece;next=new String(change);require(state.place(pos,piece)==!next.equals(board),"Edit");}
                        if(!next.equals(board)){if(undo.size()==16)undo.remove(0);undo.add(board);redo.clear();board=next;}
                    }
                    require(state.cells().equals(board),"Board differs from independent edit history");
                    require(state.canUndo()==!undo.isEmpty()&&state.canRedo()==!redo.isEmpty(),"History availability");
                    require(state.network().connected==checkNetwork(board,state.size),"Road network differs from independent search");
                    require(PlaylumiBuilder.complete(0,state).bonus==0,"Free design paid plan reward");
                    if(action%4==0){
                        PlaylumiBuilder.State restored=new PlaylumiBuilder.State(age,slot,state.snapshot());
                        require(restored.snapshot().equals(state.snapshot()),"Lost history");
                        state=restored;restores++;
                    }
                    edits++;networks++;
                }
            }
        }
        System.out.println("PASS: "+designs+" solvable construction blueprints, "+edits+" independent edit/undo/redo checks, "+networks+" independent road searches, "+restores+" exact persistent history restores, and 90 once-only bonus stars per age.");
    }
}

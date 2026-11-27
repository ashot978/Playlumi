package nl.playlumi.app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Independent solver verifies that generated clues imply the accepted answers. */
public final class SudokuCheck {
    private static void require(boolean valid,String message){if(!valid)throw new AssertionError(message);}
    private static boolean legal(int[] cells,int size,int cell,int value){
        int row=cell/size,column=cell%size;
        for(int i=0;i<size;i++)if(cells[row*size+i]==value||cells[i*size+column]==value)return false;
        if(size==4)for(int y=row-row%2;y<row-row%2+2;y++)for(int x=column-column%2;x<column-column%2+2;x++)if(cells[y*size+x]==value)return false;
        return true;
    }
    private static int solutions(int[] cells,int size,int limit){
        int next=-1;
        for(int i=0;i<cells.length;i++)if(cells[i]<0){next=i;break;}
        if(next<0)return 1;
        int count=0;
        for(int value=0;value<size;value++)if(legal(cells,size,next,value)){
            cells[next]=value;count+=solutions(cells,size,limit-count);cells[next]=-1;
            if(count>=limit)return count;
        }
        return count;
    }
    private static void reject(Runnable runnable){try{runnable.run();}catch(IllegalArgumentException expected){return;}throw new AssertionError("Corrupt Sudoku snapshot accepted");}
    public static void main(String[] args){
        int boards=0,restores=0,rejected=0;int[] minBlanks={16,16,16},maxBlanks=new int[3];
        for(int age=0;age<3;age++){
            Set<String> layouts=new HashSet<>();
            for(int seed=0;seed<600;seed++){
                PlaylumiAnimalSudoku model=new PlaylumiAnimalSudoku(age,seed);
                PlaylumiInteractive game=PlaylumiInteractive.start(PlaylumiPuzzle.SUDOKU,age,seed);
                PlaylumiPuzzle puzzle=game.puzzle();int size=age==0?2:4;
                require(game.version==4&&puzzle.cols==size&&puzzle.rows==size,"Wrong Sudoku route or dimensions");
                require(PlaylumiGameCatalog.route(age,2,PlaylumiGameCatalog.INTERACTIVE,13,0)!=null,"Missing Sudoku catalog route");
                require(game.options().size()==size&&puzzle.optionCount()==size,"Wrong animal palette");
                int[] visible=new int[size*size];Arrays.fill(visible,-1);int blank=0,given=-1;
                StringBuilder layout=new StringBuilder();Set<String> animalNames=new HashSet<>();
                for(int i=0;i<size;i++){animalNames.add(puzzle.optionName(i));layout.append(puzzle.optionName(i));}
                require(animalNames.size()==size,"Duplicated animal choice");
                for(int i=0;i<visible.length;i++){
                    visible[i]=model.givenValue(i);layout.append(',').append(visible[i]);
                    if(visible[i]<0)blank++;else{given=i;int value=visible[i];visible[i]=-1;require(legal(visible,size,i,value),"Conflicting clue");visible[i]=value;}
                    require(puzzle.blocked(i)==(visible[i]>=0),"Given not fixed");
                    require(puzzle.original(i).equals(model.clue(i)),"Wrong visible clue");
                }
                layouts.add(layout.toString());
                require(solutions(visible.clone(),size,2)==1,"Puzzle has zero or multiple solutions");
                require(blank==puzzle.total()&&blank>0,"Wrong empty-cell count");
                minBlanks[age]=Math.min(minBlanks[age],blank);maxBlanks[age]=Math.max(maxBlanks[age],blank);
                while(!game.solved()){
                    int cell=puzzle.currentCell(),correct=-1,possibilities=0;
                    require(cell>=0&&visible[cell]<0,"Current cell is filled or invalid");
                    for(int option=0;option<size;option++)if(legal(visible,size,cell,option)){correct=option;possibilities++;}
                    require(possibilities==1,"Highlighted move needs guessing");
                    int count=game.progress(),mask=game.mask();
                    for(int option=-1;option<=size;option++)if(option!=correct){
                        require(!game.choose(""+option),"Wrong animal accepted");
                        require(count==game.progress()&&mask==game.mask(),"Wrong animal mutated progress");rejected++;
                    }
                    require(game.expected().equals(""+correct)&&game.choose(""+correct),"Logic and accepted animal disagree");
                    visible[cell]=correct;require(puzzle.selected(cell)&&puzzle.display(cell).equals(puzzle.optionIcon(correct)),"Correct animal not filled");
                    PlaylumiInteractive restored=new PlaylumiInteractive(13,age,seed,game.progress(),4,game.mask());
                    require(restored.expected().equals(game.expected())&&restored.mask()==game.mask(),"Snapshot did not restore same turn");
                    for(int i=0;i<visible.length;i++)require(restored.puzzle().display(i).equals(puzzle.display(i)),"Snapshot changed a visible cell");
                    require(Integer.bitCount(game.mask())==game.progress(),"Mask counts options instead of filled cells");
                    restores++;
                }
                for(int option=0;option<size;option++)require(!game.choose(""+option),"Completed puzzle advanced again");
                require(puzzle.currentCell()==-1&&game.expected().isEmpty(),"Solved state still expects input");
                final int a=age,s=seed,g=given,total=game.total();
                reject(()->new PlaylumiPuzzle(13,a,s,0,1<<g));
                reject(()->new PlaylumiPuzzle(13,a,s,1,0));
                reject(()->new PlaylumiPuzzle(13,a,s,total+1,game.mask()));
                reject(()->new PlaylumiPuzzle(13,a,s,-1,0));
                boards++;
            }
            require(layouts.size()>age*100+40,"Too few distinct Sudoku boards");
        }
        require(minBlanks[0]==2&&maxBlanks[0]==2,"Young tier needs two missing animals");
        require(minBlanks[1]==5&&maxBlanks[1]==5,"Middle tier needs five missing animals");
        require(minBlanks[2]>=7&&maxBlanks[2]==9,"Older tier is not more challenging");
        System.out.println("PASS: "+boards+" unique-solution age-scaled animal sudokus, "+restores+" exact restores, "+rejected+" rejected wrong choices; every next move independently deduced, no guessing or repeated completion. Blank ranges "+Arrays.toString(minBlanks)+"–"+Arrays.toString(maxBlanks)+".");
    }
}

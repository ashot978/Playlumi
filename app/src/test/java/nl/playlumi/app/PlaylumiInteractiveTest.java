package nl.playlumi.app;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlaylumiInteractiveTest {
    @Test public void allGamesCompleteAndRestore(){
        for(int kind=0;kind<4;kind++)for(int age=0;age<3;age++)for(long seed=0;seed<30;seed++){
            PlaylumiInteractive game=new PlaylumiInteractive(kind,age,seed,0);
            assertFalse(game.choose("invalid"));assertEquals(0,game.progress());
            while(!game.solved()){
                String expected=game.expected();
                assertTrue(game.options().contains(expected));
                assertTrue(game.choose(expected));
                PlaylumiInteractive restored=new PlaylumiInteractive(kind,age,seed,game.progress());
                assertEquals(game.expected(),restored.expected());
                assertEquals(game.options(),restored.options());
                assertEquals(game.progress(),restored.progress());
            }
            assertFalse(game.choose("invalid"));assertEquals(game.total(),game.progress());
        }
    }
    @Test public void existingAndNewBoardGamesAreInLibrary(){
        for(int age=0;age<3;age++){
            int count=0;
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(age))
                if(e.kind==PlaylumiGameCatalog.INTERACTIVE)count++;
            assertEquals(new int[]{10,12,13}[age],count);
        }
    }
}

package nl.playlumi.app;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiGameCatalogTest {
    @Test public void uniqueStableRoutesAndAllSixWorlds(){
        int previous=0;
        for(int a=0;a<3;a++){
            List<PlaylumiGameCatalog.Entry> entries=PlaylumiGameCatalog.entries(a);
            assertTrue(entries.size()>previous);previous=entries.size();
            Set<String> ids=new HashSet<>();
            boolean[] world=new boolean[6];
            for(PlaylumiGameCatalog.Entry e:entries){
                assertTrue(ids.add(e.id));world[e.world]=true;
                assertEquals(e.id,PlaylumiGameCatalog.find(a,e.id).id);
                assertEquals(e.id,PlaylumiGameCatalog.route(a,e.world,e.kind,e.mode,e.variant).id);
                if(a<2&&e.kind==PlaylumiGameCatalog.QUIZ)assertNotEquals(ExtraGames.MATH,e.mode);
            }
            for(boolean w:world)assertTrue(w);
        }
    }
    @Test public void filtersRespectAgeAndFavorites(){
        Set<String> favorites=new HashSet<>();
        for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(2))
            if(e.title.equals("Optellen")||e.title.equals("Dierenmemory"))favorites.add(e.id);
        assertEquals(2,PlaylumiGameCatalog.filter(2,PlaylumiGameCatalog.FAVORITES,favorites).size());
        assertEquals(1,PlaylumiGameCatalog.filter(0,PlaylumiGameCatalog.FAVORITES,favorites).size());
        for(int a=0;a<3;a++)for(int w=0;w<6;w++)
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.filter(a,w,favorites))assertEquals(w,e.world);
    }
    @Test public void eachExposedQuizWorksAtItsAge(){
        LearningGame game=new LearningGame(new Random(843));
        for(int a=0;a<3;a++)for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(a)){
            if(e.kind!=PlaylumiGameCatalog.QUIZ)continue;
            game.configure(e.mode,e.variant,a);
            for(int i=0;i<40;i++){
                LearningGame.Round r=game.round();
                assertFalse(r.prompt.isEmpty());assertFalse(r.display.isEmpty());
                assertEquals(r.answers.size(),new HashSet<>(r.answers).size());
                for(int j=0;j<r.answers.size();j++)if(j!=r.correctIndex)assertFalse(r.choose(j));
                assertTrue(r.choose(r.correctIndex));assertFalse(r.choose(r.correctIndex));
                game.nextRound();
            }
        }
    }
    @Test public void eachWorldHasAgeSpecificContentAndGroupedRoutes(){
        for(int w=0;w<6;w++){
            int young=PlaylumiGameCatalog.filter(0,w,Collections.emptySet()).size();
            int middle=PlaylumiGameCatalog.filter(1,w,Collections.emptySet()).size();
            int older=PlaylumiGameCatalog.filter(2,w,Collections.emptySet()).size();
            assertTrue("middle "+w,middle>young);assertTrue("older "+w,older>middle);
        }
        for(int a=0;a<3;a++){
            int world=-1;
            for(PlaylumiGameCatalog.Entry e:PlaylumiGameCatalog.entries(a)){
                assertTrue(e.world>=world);world=e.world;
                if(e.kind==PlaylumiGameCatalog.MUSIC)assertTrue(e.variant<=a);
            }
        }
    }
    @Test public void actualClockMatchesTheHands(){
        Random r=new Random(84);
        for(int a=1;a<3;a++)for(int i=0;i<120;i++){
            LearningGame.Round round=NewWorldGames.next(16,0,a,r);
            assertTrue(round.display.startsWith("clock:"));
            assertEquals(round.display.substring(6),round.answers.get(round.correctIndex));
            assertFalse(round.answers.contains(round.display));
        }
    }
}

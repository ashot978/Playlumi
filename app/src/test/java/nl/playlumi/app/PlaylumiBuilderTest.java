package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public final class PlaylumiBuilderTest {
    @Test public void allBlueprintsAreSolvableAndOnlyFirstCompletionAwardsStars(){
        for(int age=0;age<3;age++){
            int progress=0,reward=0;
            for(int slot=0;slot<PlaylumiBuilder.PLANS;slot++){
                String blueprint=PlaylumiBuilder.blueprint(age,slot);
                PlaylumiBuilder.State state=new PlaylumiBuilder.State(age,slot,"");
                assertTrue(PlaylumiBuilder.validBoard(blueprint,state.size));
                assertTrue(PlaylumiBuilder.inspect(blueprint,state.size).complete());
                assertTrue(PlaylumiBuilder.available(progress,slot));
                assertEquals(progress,PlaylumiBuilder.complete(progress,state).after);
                for(int i=0;i<blueprint.length();i++)state.place(i,blueprint.charAt(i));
                assertTrue(state.matches());
                PlaylumiBuilder.Completion completion=PlaylumiBuilder.complete(progress,state);
                assertEquals(progress+1,completion.after);
                progress=completion.after;reward+=completion.bonus;
                assertEquals(0,PlaylumiBuilder.complete(progress,state).bonus);
                assertEquals(progress,PlaylumiBuilder.complete(progress,state).after);
            }
            assertEquals(12,progress);assertEquals(90,reward);
        }
    }
    @Test public void routesRespectBothEdgesAndDoNotTravelThroughHouses(){
        assertTrue(PlaylumiBuilder.inspect("HaS......",3).complete());
        assertFalse(PlaylumiBuilder.inspect("HbS......",3).complete());
        assertTrue(PlaylumiBuilder.inspect("He..cS...",3).complete());
        assertTrue(PlaylumiBuilder.inspect("HgS......",3).complete());
        assertFalse(PlaylumiBuilder.inspect("HWS......",3).complete());
        PlaylumiBuilder.Network n=PlaylumiBuilder.inspect("SaH..b..H",3);
        assertEquals(2,n.houses);assertEquals(1,n.connected);
        for(char p:PlaylumiBuilder.palette()){
            char rotated=p;for(int i=0;i<4;i++)rotated=PlaylumiBuilder.rotate(rotated);
            assertEquals(p,rotated);
        }
    }
    @Test public void historyRestoresAcrossReloadAndBranchesCorrectly(){
        PlaylumiBuilder.State state=new PlaylumiBuilder.State(0,12,"");
        assertTrue(state.place(0,PlaylumiBuilder.HOUSE));
        assertTrue(state.place(1,PlaylumiBuilder.ROAD_H));
        assertTrue(state.place(2,PlaylumiBuilder.STATION));
        String completed=state.cells();
        assertTrue(state.network().complete());assertTrue(state.undo());
        PlaylumiBuilder.State restored=new PlaylumiBuilder.State(0,12,state.snapshot());
        assertEquals(state.cells(),restored.cells());assertTrue(restored.canRedo());
        assertTrue(restored.redo());assertEquals(completed,restored.cells());
        assertTrue(restored.clear());assertEquals(0,restored.placed());
        assertTrue(restored.undo());assertEquals(completed,restored.cells());
        assertTrue(restored.place(3,PlaylumiBuilder.TREE));assertFalse(restored.canRedo());
        assertEquals(0,PlaylumiBuilder.complete(0,restored).bonus);
        assertNotEquals(PlaylumiBuilder.key(0,12),PlaylumiBuilder.key(0,13));
        assertNotEquals(PlaylumiBuilder.key(0,12),PlaylumiBuilder.key(1,12));
        assertEquals(0,new PlaylumiBuilder.State(0,12,"invalid").placed());
    }
}

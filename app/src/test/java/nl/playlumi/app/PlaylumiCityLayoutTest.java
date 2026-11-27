package nl.playlumi.app;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlaylumiCityLayoutTest {
    @Test public void allSitesHaveSeparateAccessibleTargetsInsideTheScene(){
        for(float width:new float[]{240,280,300,320,360,412,600,620,673,840,1200}){
            for(float textScale:new float[]{1f,1.3f,1.6f,2f}){
                PlaylumiCityLayout layout=PlaylumiCityLayout.forWidth(width,textScale);
                assertEquals(PlaylumiCityRewards.ALL.length,PlaylumiCityLayout.SITES);
                for(int i=0;i<PlaylumiCityLayout.SITES;i++){
                    PlaylumiCityLayout.Plot a=layout.plot(i);
                    assertTrue(a.width>=PlaylumiCityLayout.MIN_TARGET);
                    assertTrue(a.labelHeight>=PlaylumiCityLayout.MIN_TARGET);
                    assertTrue(a.left>=0&&a.top>=0);
                    assertTrue(a.right()<=width+.01f&&a.bottom()<=layout.height);
                    for(int j=i+1;j<PlaylumiCityLayout.SITES;j++){
                        PlaylumiCityLayout.Plot b=layout.plot(j);
                        boolean overlap=a.left<b.right()&&a.right()>b.left&&a.top<b.bottom()&&a.bottom()>b.top;
                        assertFalse("Overlapping accessible sites at "+width+" / "+textScale,overlap);
                    }
                }
            }
        }
    }
    @Test public void phonesFoldAndLargeTextUseTheAvailableWidth(){
        assertEquals(2,PlaylumiCityLayout.forWidth(360,1).columns);
        assertEquals(3,PlaylumiCityLayout.forWidth(673,1).columns);
        assertEquals(1,PlaylumiCityLayout.forWidth(280,1).columns);
        assertEquals(1,PlaylumiCityLayout.forWidth(360,2).columns);
        assertEquals(2,PlaylumiCityLayout.forWidth(673,2).columns);
        assertTrue(PlaylumiCityLayout.forWidth(360,2).height>PlaylumiCityLayout.forWidth(360,1).height);
        PlaylumiCityLayout phone=PlaylumiCityLayout.forWidth(360,1);
        assertEquals(phone.width/2,phone.plot(6).centerX(),.01);
    }
    @Test public void invalidMeasurementInputsStillProduceFiniteGeometry(){
        for(float width:new float[]{Float.NaN,Float.POSITIVE_INFINITY,-100,0}){
            PlaylumiCityLayout layout=PlaylumiCityLayout.forWidth(width,Float.NaN);
            assertFalse(Float.isNaN(layout.height));assertFalse(Float.isInfinite(layout.height));
            assertTrue(layout.plot(0).width>=PlaylumiCityLayout.MIN_TARGET);
        }
    }
}

package nl.playlumi.app;

/** Deterministic gentle bubble animation, independently testable without Android. */
public final class PlaylumiBubbleMotion {
    public static final long LOOP_MILLIS=4800L;
    private PlaylumiBubbleMotion() {}

    public static final class Frame {
        public final float lift, tilt, scale, sparkle;
        Frame(float lift,float tilt,float scale,float sparkle){
            this.lift=lift;this.tilt=tilt;this.scale=scale;this.sparkle=sparkle;
        }
    }

    public static Frame sample(long elapsed,int seed,boolean reducedMotion){
        if(reducedMotion)return new Frame(0f,0f,1f,0.5f);
        long safe=elapsed<0?0:elapsed%LOOP_MILLIS;
        double t=(safe*2.0*Math.PI/LOOP_MILLIS)+(seed%24)*0.65;
        return new Frame((float)(5.0*Math.sin(t)),
                (float)(2.1*Math.sin(t*0.7+0.4)),
                (float)(1.0+0.019*Math.sin(t+1.3)),
                (float)(0.50+0.45*Math.sin(t*1.3+0.8)));
    }
}

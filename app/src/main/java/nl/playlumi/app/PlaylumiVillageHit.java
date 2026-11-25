package nl.playlumi.app;

/** Pure-Java coordinate mapping for illustrated village and clickable buildings. */
public final class PlaylumiVillageHit {
    public static final float ART_W=941f, ART_H=1672f;
    public static final int ROCKET=0,BOOKS=1,SHAPES=2,MUSIC=3,PLAYGROUND=4,COLORS=5,
            FRIEND=6,PARENTS=7,MISSIONS=8,REWARDS=9,HOME=10,COLLECTION=11,PROFILE=12,
            SETTINGS=13;
    private static final float[][] HIT={
            {115,253,467,530},{535,262,927,548},
            {25,535,360,880},{610,542,924,887},
            {5,889,385,1266},{570,891,936,1250},
            {351,614,613,921},{730,83,940,190},
            {0,1467,177,1672},{179,1467,365,1672},
            {366,1434,574,1672},{575,1467,759,1672},
            {761,1467,941,1672},{816,0,941,86}
    };
    private PlaylumiVillageHit() {}
    /** Aspect fill for phones; letterboxed artwork on wide tablets/foldables. */
    public static float[] destination(int screenW,int screenH) {
        if(screenW<=0 || screenH<=0)return new float[]{0,0,1,1};
        if(screenW/(float)screenH <= .72f) {
            return new float[]{0,0,screenW,screenH};
        }
        float factor=Math.min(screenW/ART_W,screenH/ART_H);
        float dw=ART_W*factor,dh=ART_H*factor;
        return new float[]{(screenW-dw)*.5f,(screenH-dh)*.5f,dw,dh};
    }
    public static float[] artCoordinate(int screenW,int screenH,float x,float y) {
        float[] dest=destination(screenW,screenH);
        return new float[]{(x-dest[0])*ART_W/dest[2],(y-dest[1])*ART_H/dest[3]};
    }
    public static int at(int screenW,int screenH,float x,float y) {
        if(screenW<=0 || screenH<=0)return -1;
        float[] d=destination(screenW,screenH);
        if(x<d[0] || y<d[1] || x>d[0]+d[2] || y>d[1]+d[3])return -1;
        float[] pos=artCoordinate(screenW,screenH,x,y);
        return hit(pos[0],pos[1]);
    }
    public static float[] bounds(int place) {
        if(place<0 || place>=HIT.length)throw new IllegalArgumentException("place");
        return HIT[place].clone();
    }
    private static boolean contains(int i,float x,float y) {
        float[] b=HIT[i];return x>=b[0]&&y>=b[1]&&x<=b[2]&&y<=b[3];
    }
    public static int hit(float x,float y) {
        if(x<0||y<0||x>ART_W||y>ART_H)return -1;
        // Controls win when their artwork overlaps interactive village scenery.
        if(contains(SETTINGS,x,y))return SETTINGS;
        for(int i=PARENTS;i<=PROFILE;i++)if(contains(i,x,y))return i;
        if(contains(FRIEND,x,y))return FRIEND;
        for(int i=ROCKET;i<=COLORS;i++)if(contains(i,x,y))return i;
        return -1;
    }
}

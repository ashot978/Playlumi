package nl.playlumi.app;

/** Small original, royalty-free synthesized jingles, no external audio assets. */
public final class PlaylumiTunes {
    public static final int TAP=0, SUCCESS=1, TRY_AGAIN=2, UNLOCK=3,
            HELLO=4, POP=5, DANCE=6, SPARKLE=7, NOTE_GREEN=8, NOTE_BLUE=9, NOTE_YELLOW=10, NOTE_PURPLE=11;
    public static final int BONUS=12,GARDEN=13,STORY=14,MATCH=15,
            DISCOVER=16,PARTY=17,WELCOME=18,BUBBLE=19,
            STAR_SHOWER=20,CHEER=21,TAP_ALT=22,LEVEL_UP=23;
    public static final int COUNT=24, RATE=22050;
    private static final double[][] NOTES={
        {660,0,0},                 // button tap
        {523.25,659.25,783.99},   // good answer
        {392,329.63,0},           // gentle encouragement
        {523.25,659.25,783.99,1046.5,1318.5}, // unlock
        {659.25,783.99,880,0,880}, // greeting
        {920,700,480},            // popping bubble, descending
        {392,523.25,659.25,523.25,783.99,880}, // little dance
        {1046.5,1318.5,1568,2093}, // magic chime
        {261.63}, // frog: C4
        {329.63}, // cat: E4
        {392.00}, // chick: G4
        {523.25}, // butterfly: C5
        {523.25,659.25,783.99,1046.5,1318.5},    // bonus fanfare
        {392,523.25,659.25,783.99,659.25},       // growing garden
        {329.63,440,523.25,659.25,523.25},      // story choice
        {523.25,659.25,523.25,783.99},           // matching pairs
        {440,587.33,698.46,880},                 // discovery
        {523.25,659.25,783.99,1046.5,1174.66,1318.5}, // party
        {587.33,698.46,880,987.77},             // warm welcome
        {1046.5,783.99,587.33,880},              // new bubble sound
        {783.99,1046.5,1318.5,1567.98,1046.5}, // star shower
        {392,587.33,783.99,987.77,783.99},      // cheer
        {659.25,880},                            // light tap
        {440,523.25,659.25,880,1046.5}          // level up
    };
    private static final int[] NOTE_MS={
        85,130,170,150,175,38,135,155,245,245,245,245,
        122,145,140,126,128,145,135,92,125,138,83,140};
    private static final int[] GAP_MS={
        0,13,14,10,14,0,16,11,0,0,0,0,
        15,17,16,12,13,12,14,8,12,14,8,13};
    private PlaylumiTunes() {}
    public static byte[] wav(int effect) {
        if(effect<0 || effect>=COUNT)throw new IllegalArgumentException("Invalid jingle");
        double[] notes=NOTES[effect];
        int perNote=NOTE_MS[effect]*RATE/1000;
        int perGap=GAP_MS[effect]*RATE/1000;
        int frames=notes.length*(perNote+perGap)+RATE/35;
        byte[] wave=new byte[44+frames*2];
        // PCM WAV header, little endian: mono, 16-bit, 22,050 Hz
        writeAscii(wave,0,"RIFF");write32(wave,4,wave.length-8);
        writeAscii(wave,8,"WAVE");writeAscii(wave,12,"fmt ");
        write32(wave,16,16);write16(wave,20,1);write16(wave,22,1);
        write32(wave,24,RATE);write32(wave,28,RATE*2);
        write16(wave,32,2);write16(wave,34,16);
        writeAscii(wave,36,"data");write32(wave,40,frames*2);
        int offset=44;
        for(int n=0;n<notes.length;n++) {
            double f=notes[n];
            for(int i=0;i<perNote+perGap;i++) {
                double v=0;
                if(i<perNote && f>0) {
                    double t=(double)i/RATE;
                    double fadeIn=Math.min(1.0,(double)i/(RATE*.013));
                    double fadeOut=Math.min(1.0,(double)(perNote-i)/(RATE*.047));
                    double amp=Math.min(fadeIn,fadeOut);
                    double pitch=effect==POP?f*(1.0-.32*(double)i/perNote):f;
                    double phase=2*Math.PI*pitch*t;
                    double primary=Math.sin(phase);
                    double high=.12*Math.sin(2*phase);
                    double shimmer=.035*Math.sin(3*phase);
                    v=amp*(primary+high+shimmer)*.19;
                }
                short sample=(short)Math.max(-32768,Math.min(32767,Math.round(v*32767)));
                wave[offset++]=(byte)(sample&255);
                wave[offset++]=(byte)((sample>>>8)&255);
            }
        }
        return wave;
    }
    private static void writeAscii(byte[] bytes,int pos,String text) {
        for(int i=0;i<text.length();i++)bytes[pos+i]=(byte)text.charAt(i);
    }
    private static void write16(byte[] b,int pos,int value) {
        b[pos]=(byte)value;b[pos+1]=(byte)(value>>8);
    }
    private static void write32(byte[] b,int pos,int value) {
        for(int i=0;i<4;i++)b[pos+i]=(byte)(value>>(i*8));
    }
}

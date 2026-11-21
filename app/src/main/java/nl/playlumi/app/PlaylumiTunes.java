package nl.playlumi.app;

/** Small original, royalty-free synthesized jingles, no external audio assets. */
public final class PlaylumiTunes {
    public static final int TAP=0, SUCCESS=1, TRY_AGAIN=2, UNLOCK=3,
            HELLO=4, POP=5, DANCE=6, SPARKLE=7;
    public static final int COUNT=8, RATE=22050;
    private static final double[][] NOTES={
        {660,0,0},                 // button tap
        {523.25,659.25,783.99},   // good answer
        {392,329.63,0},           // gentle encouragement
        {523.25,659.25,783.99,1046.5,1318.5}, // unlock
        {659.25,783.99,880,0,880}, // greeting
        {920,700,480},            // popping bubble, descending
        {392,523.25,659.25,523.25,783.99,880}, // little dance
        {1046.5,1318.5,1568,2093} // magic chime
    };
    private static final int[] NOTE_MS={85,130,170,150,175,38,135,155};
    private static final int[] GAP_MS={0,13,14,10,14,0,16,11};
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
                    double high=.23*Math.sin(2*phase);
                    v=amp*(primary+high)*.16;
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

package nl.playlumi.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Playlumi's own original eight chimes, played quietly. Short WAVs are
 * synthesized once in the private app cache; no music download or permission.
 * Synthetic narration is intentionally absent; music and effects are retained.
 */
public final class PlaylumiAudio {
    private final AtomicIntegerArray loaded=new AtomicIntegerArray(PlaylumiTunes.COUNT);
    private final AtomicIntegerArray soundIds=new AtomicIntegerArray(PlaylumiTunes.COUNT);
    private final Context app;
    private SoundPool pool;
    private ToneGenerator fallback;
    private volatile boolean enabled,released;
    private Thread preloader;

    public PlaylumiAudio(Context context,boolean enabled) {
        this.app=context.getApplicationContext();
        this.enabled=enabled;
        try {
            AudioAttributes attributes=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            pool=new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(attributes).build();
            pool.setOnLoadCompleteListener((soundPool,id,status)->{
                if(released || status!=0)return;
                for(int i=0;i<PlaylumiTunes.COUNT;i++)
                    if(soundIds.get(i)==id)loaded.set(i,1);
            });
            preloader=new Thread(this::prepare,"playlumi-audio");
            preloader.setDaemon(true);
            preloader.start();
        } catch(RuntimeException ignored) {
            if(pool!=null)pool.release();
            pool=null;
        }
        try {fallback=new ToneGenerator(AudioManager.STREAM_MUSIC,32);} catch(RuntimeException ignored){}
    }
    private void prepare() {
        for(int i=0;i<PlaylumiTunes.COUNT && !released;i++) {
            try {
                File file=new File(app.getCacheDir(),"playlumi-chime-v2-"+i+".wav");
                if(!file.isFile() || file.length()<100) {
                    byte[] wav=PlaylumiTunes.wav(i);
                    try(FileOutputStream out=new FileOutputStream(file)) {out.write(wav);}
                }
                if(released)break;
                SoundPool current=pool;
                if(current==null)break;
                int id=current.load(file.getAbsolutePath(),1);
                soundIds.set(i,id);
            } catch(IOException | RuntimeException ignored) {
                // Quiet failure: the game remains fully playable without sounds.
            }
        }
    }
    public boolean isEnabled() {return enabled;}
    public void setEnabled(boolean value) {
        enabled=value;
    }
    private void cue(int effect,int fallbackTone) {
        if(released || !enabled)return;
        SoundPool current=pool;
        int id=soundIds.get(effect);
        if(current!=null && id>0 && loaded.get(effect)==1) {
            try {
                // Gentle maximum level: protects young ears.
                current.play(id,.34f,.34f,1,0,1f);
                return;
            } catch(RuntimeException ignored){}
        }
        if(fallback!=null) {
            try {fallback.startTone(fallbackTone,75);} catch(RuntimeException ignored){}
        }
    }
    public void tap(){cue(PlaylumiTunes.TAP,ToneGenerator.TONE_PROP_BEEP);}
    public void good(){cue(PlaylumiTunes.SUCCESS,ToneGenerator.TONE_PROP_ACK);}
    public void mistake(){cue(PlaylumiTunes.TRY_AGAIN,ToneGenerator.TONE_PROP_NACK);}
    public void unlock(){cue(PlaylumiTunes.UNLOCK,ToneGenerator.TONE_PROP_ACK);}
    public void hello(){cue(PlaylumiTunes.HELLO,ToneGenerator.TONE_PROP_BEEP2);}
    public void pop(){cue(PlaylumiTunes.POP,ToneGenerator.TONE_PROP_BEEP);}
    public void dance(){cue(PlaylumiTunes.DANCE,ToneGenerator.TONE_PROP_ACK);}
    public void sparkle(){cue(PlaylumiTunes.SPARKLE,ToneGenerator.TONE_PROP_ACK);}
    public void note(int index){
        if(index<0 || index>=PlaylumiMelody.NOTES)throw new IllegalArgumentException("note");
        cue(PlaylumiTunes.NOTE_GREEN+index,ToneGenerator.TONE_PROP_BEEP);
    }
    public void close() {
        released=true;
        if(pool!=null) {pool.release();pool=null;}
        if(fallback!=null) {fallback.release();fallback=null;}
        // The private app cache may keep tiny synthesized WAVs for next launch.
    }
}

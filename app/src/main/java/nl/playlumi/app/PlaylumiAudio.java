package nl.playlumi.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/** Pure original instrumental jingles; no human/synthesized speech engine. */
public final class PlaylumiAudio {
    private final AtomicIntegerArray loaded=new AtomicIntegerArray(PlaylumiTunes.COUNT);
    private final AtomicIntegerArray soundIds=new AtomicIntegerArray(PlaylumiTunes.COUNT);
    private final AtomicInteger successTurn=new AtomicInteger(),tapTurn=new AtomicInteger(),
            sparkleTurn=new AtomicInteger(),danceTurn=new AtomicInteger(),
            previewTurn=new AtomicInteger();
    private final Context app;
    private SoundPool pool;
    private volatile boolean enabled,effectsEnabled=true,released;
    private volatile int activeNoteStream,lastEffectStream;

    public PlaylumiAudio(Context context,boolean soundOn){
        app=context.getApplicationContext();
        enabled=soundOn;
        try {
            AudioAttributes attrs=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            pool=new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(attrs).build();
            pool.setOnLoadCompleteListener((p,id,status)->{
                if(released || status!=0)return;
                for(int i=0;i<PlaylumiTunes.COUNT;i++)
                    if(soundIds.get(i)==id)loaded.set(i,1);
            });
            Thread loader=new Thread(this::prepare,"playlumi-original-jingles");
            loader.setDaemon(true);
            loader.start();
        } catch(RuntimeException ignored){
            if(pool!=null)pool.release();
            pool=null;
        }
    }

    private void prepare(){
        for(int i=0;i<PlaylumiTunes.COUNT && !released;i++)try {
            File file=new File(app.getCacheDir(),"playlumi-jingle-v4-"+i+".wav");
            if(!file.isFile() || file.length()<100) {
                try(FileOutputStream out=new FileOutputStream(file)){
                    out.write(PlaylumiTunes.wav(i));
                }
            }
            if(released)break;
            SoundPool current=pool;
            if(current==null)break;
            soundIds.set(i,current.load(file.getAbsolutePath(),1));
        } catch(IOException | RuntimeException ignored){}
    }
    public boolean isEnabled(){return enabled;}
    public void setEnabled(boolean value){
        enabled=value;
        if(!value) {
            stopNotes();
            if(pool!=null)try{pool.autoPause();}catch(RuntimeException ignored){}
        } else if(pool!=null)try{pool.autoResume();}catch(RuntimeException ignored){}
    }
    public boolean effectsEnabled(){return effectsEnabled;}
    public void setEffectsEnabled(boolean value){
        effectsEnabled=value;
        if(!value && pool!=null && lastEffectStream>0)try{
            pool.stop(lastEffectStream);
            lastEffectStream=0;
        } catch(RuntimeException ignored){}
    }

    private static int choose(AtomicInteger index,int... options){
        return options[(index.getAndIncrement() & Integer.MAX_VALUE)%options.length];
    }
    private void cue(int effect,boolean deliberateMusic){
        if(released || !PlaylumiAudioRules.shouldPlay(enabled,effectsEnabled,deliberateMusic))return;
        SoundPool current=pool;
        if(current==null || loaded.get(effect)!=1)return;
        int id=soundIds.get(effect);
        if(id<=0)return;
        try {
            if(deliberateMusic)stopNotes();
            float volume=deliberateMusic?.52f:.42f;
            int stream=current.play(id,volume,volume,1,0,1f);
            if(deliberateMusic)activeNoteStream=stream;
            else lastEffectStream=stream;
        } catch(RuntimeException ignored){}
    }
    private void cue(int effect){cue(effect,false);}

    public void tap(){cue(choose(tapTurn,PlaylumiTunes.TAP,PlaylumiTunes.TAP_ALT));}
    public void good(){cue(choose(successTurn,PlaylumiTunes.SUCCESS,
            PlaylumiTunes.CHEER,PlaylumiTunes.DISCOVER,PlaylumiTunes.LEVEL_UP));}
    public void mistake(){cue(PlaylumiTunes.TRY_AGAIN);}
    public void unlock(){cue(choose(successTurn,PlaylumiTunes.UNLOCK,
            PlaylumiTunes.BONUS,PlaylumiTunes.LEVEL_UP));}
    public void hello(){cue(choose(danceTurn,PlaylumiTunes.HELLO,PlaylumiTunes.WELCOME));}
    public void pop(){cue(choose(tapTurn,PlaylumiTunes.POP,PlaylumiTunes.BUBBLE));}
    public void dance(){cue(choose(danceTurn,PlaylumiTunes.DANCE,PlaylumiTunes.PARTY));}
    public void sparkle(){cue(choose(sparkleTurn,PlaylumiTunes.SPARKLE,
            PlaylumiTunes.STAR_SHOWER,PlaylumiTunes.DISCOVER));}
    public void garden(){cue(PlaylumiTunes.GARDEN);}
    public void story(){cue(PlaylumiTunes.STORY);}
    public void match(){cue(PlaylumiTunes.MATCH);}
    public void party(){cue(PlaylumiTunes.PARTY);}
    public void sample(){
        cue(choose(previewTurn,PlaylumiTunes.BONUS,PlaylumiTunes.GARDEN,
                PlaylumiTunes.STORY,PlaylumiTunes.MATCH,PlaylumiTunes.DISCOVER,
                PlaylumiTunes.PARTY,PlaylumiTunes.WELCOME,
                PlaylumiTunes.STAR_SHOWER,PlaylumiTunes.CHEER,
                PlaylumiTunes.LEVEL_UP),true);
    }
    public void note(int index){
        if(index<0 || index>=PlaylumiMelody.NOTES)
            throw new IllegalArgumentException("note");
        cue(PlaylumiTunes.NOTE_GREEN+index,true);
    }
    public void stopNotes(){
        SoundPool current=pool;
        int stream=activeNoteStream;
        activeNoteStream=0;
        if(current!=null && stream>0)try{current.stop(stream);}catch(RuntimeException ignored){}
    }
    public void close(){
        released=true;
        stopNotes();
        if(pool!=null){pool.release();pool=null;}
    }
}

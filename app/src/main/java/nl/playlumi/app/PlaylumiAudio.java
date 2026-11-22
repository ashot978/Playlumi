package nl.playlumi.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Playlumi's own original eight chimes, played quietly. Short WAVs are
 * synthesized once in the private app cache; no music download or permission.
 * Existing text-to-speech remains optional and works without these sounds.
 */
public final class PlaylumiAudio {
    private final AtomicIntegerArray loaded=new AtomicIntegerArray(PlaylumiTunes.COUNT);
    private final AtomicIntegerArray soundIds=new AtomicIntegerArray(PlaylumiTunes.COUNT);
    private final Context app;
    private SoundPool pool;
    private TextToSpeech speech;
    private volatile boolean ttsReady,enabled,effectsEnabled=true,released;
    private Thread preloader;

    public PlaylumiAudio(Context context,boolean enabled) {
        this.app=context.getApplicationContext();
        this.enabled=enabled;
        try {
            AudioAttributes attributes=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            pool=new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attributes).build();
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
        try {
            speech=new TextToSpeech(app,status->{
                if(released || speech==null)return;
                if(status==TextToSpeech.SUCCESS)configureDutchVoice();
            });
        } catch(RuntimeException ignored) {speech=null;}
    }

    /** Select the best available offline Dutch voice; engine quality varies by device. */
    private void configureDutchVoice() {
        if(released || speech==null)return;
        Locale dutch=Locale.forLanguageTag("nl-NL");
        try {
            int available=speech.setLanguage(dutch);
            if(available==TextToSpeech.LANG_MISSING_DATA ||
                    available==TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsReady=false;
                return;
            }
            Voice bestVoice=null;
            int bestRank=-1;
            Set<Voice> installed=speech.getVoices();
            if(installed!=null)for(Voice voice:installed) {
                int rank=PlaylumiVoiceChoice.rank(voice.getLocale(),
                        voice.isNetworkConnectionRequired(),voice.getQuality());
                if(rank>bestRank) {
                    bestRank=rank;
                    bestVoice=voice;
                }
            }
            if(bestVoice!=null &&
                    speech.setVoice(bestVoice)!=TextToSpeech.SUCCESS)
                speech.setLanguage(dutch);
            speech.setSpeechRate(.97f);
            speech.setPitch(1.0f);
            ttsReady=true;
        } catch(RuntimeException ignored) {
            ttsReady=false;
        }
    }
    private void prepare() {
        for(int i=0;i<PlaylumiTunes.COUNT && !released;i++) {
            try {
                File file=new File(app.getCacheDir(),"playlumi-chime-v3-"+i+".wav");
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
        if(!value && speech!=null)speech.stop();
    }
    /** Effects and explicit speech can be controlled separately. */
    public boolean effectsEnabled() {return effectsEnabled;}
    public void setEffectsEnabled(boolean value) {
        effectsEnabled=value;
        if(!value && pool!=null)pool.autoPause();
    }
    private void cue(int effect) {
        if(released || !enabled || !effectsEnabled)return;
        SoundPool current=pool;
        int id=soundIds.get(effect);
        if(current!=null && id>0 && loaded.get(effect)==1) {
            try {
                // No harsh emergency beep while the original chime loads.
                current.play(id,.25f,.25f,1,0,1f);
            } catch(RuntimeException ignored){}
        }
    }
    public void tap(){cue(PlaylumiTunes.TAP);}
    public void good(){cue(PlaylumiTunes.SUCCESS);}
    public void mistake(){cue(PlaylumiTunes.TRY_AGAIN);}
    public void unlock(){cue(PlaylumiTunes.UNLOCK);}
    public void hello(){cue(PlaylumiTunes.HELLO);}
    public void pop(){cue(PlaylumiTunes.POP);}
    public void dance(){cue(PlaylumiTunes.DANCE);}
    public void sparkle(){cue(PlaylumiTunes.SPARKLE);}
    public void note(int index){
        if(index<0 || index>=PlaylumiMelody.NOTES)throw new IllegalArgumentException("note");
        cue(PlaylumiTunes.NOTE_GREEN+index);
    }
    public boolean speak(String text) {
        if(!enabled || !ttsReady || speech==null || released || text==null || text.trim().isEmpty())return false;
        try {
            return speech.speak(text,TextToSpeech.QUEUE_FLUSH,null,
                    "playlumi-listen")==TextToSpeech.SUCCESS;
        } catch(RuntimeException ignored) {return false;}
    }
    /** Keep narration on its current page, never talk over the next activity. */
    public void stopSpeaking() {
        if(speech!=null) {
            try {speech.stop();} catch(RuntimeException ignored) {}
        }
    }
    public void close() {
        released=true;
        if(speech!=null) {speech.stop();speech.shutdown();speech=null;}
        if(pool!=null) {pool.release();pool=null;}
        // The private app cache may keep tiny synthesized WAVs for next launch.
    }
}

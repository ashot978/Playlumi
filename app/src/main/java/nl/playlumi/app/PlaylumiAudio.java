package nl.playlumi.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/** Optional original feedback tones and on-demand Dutch spoken questions. */
public final class PlaylumiAudio {
    private ToneGenerator tones;
    private TextToSpeech speech;
    private boolean ready;
    private boolean enabled;
    private boolean released;
    public PlaylumiAudio(Context context,boolean enabled) {
        this.enabled=enabled;
        try {tones=new ToneGenerator(AudioManager.STREAM_MUSIC,42);} catch(RuntimeException ignored) {}
        try {
            speech=new TextToSpeech(context.getApplicationContext(),status->{
                if(released || speech==null)return;
                if(status==TextToSpeech.SUCCESS) {
                    int result=speech.setLanguage(Locale.forLanguageTag("nl-NL"));
                    ready=result!=TextToSpeech.LANG_MISSING_DATA &&
                            result!=TextToSpeech.LANG_NOT_SUPPORTED;
                    speech.setSpeechRate(0.89f);
                    speech.setPitch(1.06f);
                }
            });
        } catch(RuntimeException ignored) {speech=null;}
    }
    public boolean isEnabled() {return enabled;}
    public void setEnabled(boolean enabled) {
        this.enabled=enabled;
        if(!enabled && speech!=null)speech.stop();
    }
    public void tap() {tone(ToneGenerator.TONE_PROP_BEEP,65);}
    public void good() {tone(ToneGenerator.TONE_PROP_ACK,155);}
    public void mistake() {tone(ToneGenerator.TONE_PROP_NACK,100);}
    private void tone(int which,int millis) {
        if(released || !enabled || tones==null)return;
        try {tones.startTone(which,millis);} catch(RuntimeException ignored) {}
    }
    public boolean speak(String text) {
        if(!enabled || !ready || speech==null || released || text==null)return false;
        speech.speak(text,TextToSpeech.QUEUE_FLUSH,null,"playlumi-listen");
        return true;
    }
    public void close() {
        released=true;
        if(speech!=null) {speech.stop();speech.shutdown();speech=null;}
        if(tones!=null) {tones.release();tones=null;}
    }
}

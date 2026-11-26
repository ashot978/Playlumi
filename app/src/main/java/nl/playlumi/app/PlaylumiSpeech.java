package nl.playlumi.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import java.util.Locale;
import java.util.Set;

/** Device-local speech only. Never select a network-required voice or start a voice download. */
public final class PlaylumiSpeech {
    private final Handler main=new Handler(Looper.getMainLooper());
    private TextToSpeech engine;
    private boolean initialized,available,closed;
    private String pending;
    private Runnable unavailable;
    private int serial;
    public PlaylumiSpeech(Context context){
        try {
            engine=new TextToSpeech(context.getApplicationContext(),status->main.post(()->ready(status)));
        }catch(RuntimeException ex){initialized=true;available=false;}
    }
    private void ready(int status){
        if(closed||engine==null)return;
        initialized=true;available=false;
        if(status==TextToSpeech.SUCCESS){
            try{
                Set<Voice> voices=engine.getVoices();
                Voice selected=null;
                if(voices!=null)for(Voice voice:voices){
                    if(voice==null||voice.isNetworkConnectionRequired()||!"nl".equals(voice.getLocale().getLanguage()))continue;
                    Set<String> features=voice.getFeatures();
                    if(features!=null&&features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))continue;
                    if(selected==null||rank(voice)>rank(selected))selected=voice;
                }
                if(selected!=null&&engine.setVoice(selected)==TextToSpeech.SUCCESS){
                    engine.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
                    engine.setSpeechRate(.88f);available=true;
                }
            }catch(RuntimeException ignored){available=false;}
        }
        flush();
    }
    private static int rank(Voice voice){return ("NL".equals(voice.getLocale().getCountry())?1000:0)+voice.getQuality();}
    public void speak(String text,Runnable noVoice){
        if(closed||text==null||text.trim().isEmpty())return;
        pending=text;unavailable=noVoice;
        if(initialized)flush();
    }
    private void flush(){
        if(closed||pending==null)return;
        String value=pending;Runnable failure=unavailable;pending=null;unavailable=null;
        boolean ok=false;
        if(available&&engine!=null)try{
            Voice current=engine.getVoice();
            ok=current!=null&&!current.isNetworkConnectionRequired()&&"nl".equals(current.getLocale().getLanguage())&&
                    engine.speak(value,TextToSpeech.QUEUE_FLUSH,null,"playlumi-"+(++serial))==TextToSpeech.SUCCESS;
        }catch(RuntimeException ignored){}
        if(!ok&&failure!=null)failure.run();
    }
    public void stop(){
        pending=null;unavailable=null;
        if(engine!=null)try{engine.stop();}catch(RuntimeException ignored){}
    }
    public void close(){
        closed=true;stop();
        if(engine!=null){try{engine.shutdown();}catch(RuntimeException ignored){}engine=null;}
        main.removeCallbacksAndMessages(null);
    }
}

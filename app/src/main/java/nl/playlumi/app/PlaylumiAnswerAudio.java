package nl.playlumi.app;

import java.util.List;

/**
 * Local, score-free answer previews. Only the visible options are spoken,
 * always in their screen order. No reference to correctness is included.
 * Android speech and interaction remain the responsibility of the UI.
 */
public final class PlaylumiAnswerAudio {
    private PlaylumiAnswerAudio() {}

    private static void check(List<String> options,int mask) {
        if(options==null || options.size()<2 || options.size()>30)
            throw new IllegalArgumentException("options");
        int legal=(1<<options.size())-1;
        if((mask&~legal)!=0)throw new IllegalArgumentException("hidden options");
        for(String value:options)
            if(value==null || value.trim().isEmpty())
                throw new IllegalArgumentException("empty answer");
    }

    private static String label(List<String> options,int index) {
        return "Antwoord "+(index+1)+": "+
                options.get(index).trim().replace('\n',' ').replace('\r',' ');
    }

    /** A long press reads one still-visible option, without selecting it. */
    public static String oneChoice(List<String> options,int index,int hiddenMask) {
        check(options,hiddenMask);
        if(index<0 || index>=options.size() || (hiddenMask&(1<<index))!=0)
            throw new IllegalArgumentException("hidden or invalid choice");
        return label(options,index)+".";
    }

    /** A visible audio button announces all remaining options in order. */
    public static String allChoices(List<String> options,int hiddenMask) {
        check(options,hiddenMask);
        StringBuilder text=new StringBuilder("Je kunt kiezen uit: ");
        int remaining=0;
        for(int i=0;i<options.size();i++) {
            if((hiddenMask&(1<<i))!=0)continue;
            if(remaining++>0)text.append(" ");
            text.append(label(options,i)).append(".");
        }
        if(remaining<2)throw new IllegalArgumentException("not enough options");
        return text.toString();
    }
}

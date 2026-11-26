package nl.playlumi.app;
import java.util.Calendar;
import java.util.TimeZone;

/** Local calendar days, independent of DST and elapsed hours. No network or timers. */
public final class PlaylumiPlayDays {
    public long last;public int run,best,total;
    public PlaylumiPlayDays(long last,int run,int best,int total){
        this.last=last;this.run=Math.max(0,run);
        this.best=Math.max(this.run,best);this.total=Math.max(this.best,total);
    }
    public static long today(){
        Calendar local=Calendar.getInstance(),utc=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();utc.set(local.get(Calendar.YEAR),local.get(Calendar.MONTH),local.get(Calendar.DAY_OF_MONTH),0,0,0);
        return utc.getTimeInMillis()/86400000L;
    }
    public boolean record(long day){
        if(day<=last)return false;
        run=last!=Long.MIN_VALUE&&day-last==1?increment(run):1;
        last=day;total=increment(total);best=Math.max(best,run);return true;
    }
    public int current(long day){
        return last!=Long.MIN_VALUE&&day>=last&&day-last<=1?run:0;
    }
    private static int increment(int x){return x==Integer.MAX_VALUE?x:x+1;}
}

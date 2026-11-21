package nl.playlumi.app;

import java.util.ArrayList;
import java.util.List;

/** At most six pinned game configurations per child; no answers are saved. */
public final class PlaylumiFavorites {
    public static final int CAPACITY=6;
    private final List<PlaylumiPlayHistory.Entry> entries=new ArrayList<>(CAPACITY);

    public int size(){return entries.size();}
    public PlaylumiPlayHistory.Entry get(int i){return entries.get(i);}

    public boolean contains(int mode,int variant){
        for(PlaylumiPlayHistory.Entry entry:entries)
            if(entry.mode==mode && entry.variant==variant)return true;
        return false;
    }

    /** Pin newest first. Returns false only if a different sixth favorite fills the list. */
    public boolean pin(PlaylumiPlayHistory.Entry item){
        if(item==null)throw new IllegalArgumentException("game");
        for(int i=0;i<entries.size();i++){
            PlaylumiPlayHistory.Entry existing=entries.get(i);
            if(existing.mode==item.mode && existing.variant==item.variant){
                entries.set(i,item);
                return true;
            }
        }
        if(entries.size()>=CAPACITY)return false;
        entries.add(0,item);
        return true;
    }

    /** A second tap on a pinned heart removes that favorite, with no effect on scores. */
    public boolean toggle(PlaylumiPlayHistory.Entry item){
        if(item==null)throw new IllegalArgumentException("game");
        for(int i=0;i<entries.size();i++)
            if(entries.get(i).mode==item.mode && entries.get(i).variant==item.variant){
                entries.remove(i);
                return false;
            }
        return pin(item);
    }

    public String serialize(){
        StringBuilder out=new StringBuilder();
        for(PlaylumiPlayHistory.Entry item:entries){
            if(out.length()!=0)out.append(';');
            out.append(item.mode).append(',').append(item.variant).append(',')
                    .append(item.difficulty).append(',').append(item.smart?'1':'0');
        }
        return out.toString();
    }

    /** Corrupt or oversized local preferences are safely ignored. */
    public static PlaylumiFavorites restore(String saved){
        PlaylumiFavorites result=new PlaylumiFavorites();
        if(saved==null || saved.length()>512)return result;
        String[] rows=saved.split(";",-1);
        for(int index=rows.length-1;index>=0;index--){
            String row=rows[index];
            String[] parts=row.split(",",-1);
            if(parts.length!=4 || (!parts[3].equals("0") && !parts[3].equals("1")))continue;
            try {
                result.pin(new PlaylumiPlayHistory.Entry(
                        Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),parts[3].equals("1")));
            } catch(IllegalArgumentException ignored) {}
        }
        return result;
    }
}

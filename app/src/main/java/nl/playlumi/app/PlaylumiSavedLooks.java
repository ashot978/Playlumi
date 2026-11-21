package nl.playlumi.app;

/**
 * Three independent outfits saved locally per child. All items are already
 * owned; restoring a look never spends earned stars or unlocks new items.
 */
public final class PlaylumiSavedLooks {
    public static final int SLOTS=3;
    private final int[][] looks=new int[SLOTS][PlaylumiWardrobe.SLOTS];
    private final boolean[] filled=new boolean[SLOTS];

    private static void check(int slot){
        if(slot<0 || slot>=SLOTS)throw new IllegalArgumentException("look slot");
    }

    public boolean isFilled(int slot){check(slot);return filled[slot];}

    public void save(int slot,PlaylumiWardrobe wardrobe){
        check(slot);
        if(wardrobe==null)throw new IllegalArgumentException("wardrobe");
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)looks[slot][i]=wardrobe.equipped(i);
        filled[slot]=true;
    }

    /**
     * If previously saved clothing is no longer owned after a restored backup,
     * leave that category untouched rather than buying it or changing data.
     */
    public boolean wear(int slot,PlaylumiWardrobe wardrobe){
        check(slot);
        if(wardrobe==null)throw new IllegalArgumentException("wardrobe");
        if(!filled[slot])return false;
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)
            if(wardrobe.owned(i,looks[slot][i]))
                wardrobe.buyOrEquip(i,looks[slot][i]); // owned => free EQUIPPED
        return true;
    }

    public String describe(int slot){
        check(slot);
        if(!filled[slot])return "Nog geen outfit bewaard";
        StringBuilder summary=new StringBuilder();
        for(int i=0;i<PlaylumiWardrobe.SLOTS;i++){
            if(i>0)summary.append(" · ");
            summary.append(PlaylumiWardrobe.ITEM_ICONS[i][looks[slot][i]]);
        }
        return summary.toString();
    }

    public String serialize(){
        StringBuilder out=new StringBuilder();
        for(int slot=0;slot<SLOTS;slot++){
            if(slot>0)out.append(';');
            if(!filled[slot]){out.append('-');continue;}
            for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)
                out.append((char)('0'+looks[slot][i]));
        }
        return out.toString();
    }

    public static PlaylumiSavedLooks restore(String saved){
        PlaylumiSavedLooks result=new PlaylumiSavedLooks();
        if(saved==null || saved.length()>80)return result;
        String[] rows=saved.split(";",-1);
        for(int slot=0;slot<Math.min(rows.length,SLOTS);slot++){
            String row=rows[slot];
            if(row.length()!=PlaylumiWardrobe.SLOTS)continue;
            boolean valid=true;
            for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)
                if(row.charAt(i)<'0' || row.charAt(i)>='0'+PlaylumiWardrobe.OPTIONS)
                    valid=false;
            if(!valid)continue;
            result.filled[slot]=true;
            for(int i=0;i<PlaylumiWardrobe.SLOTS;i++)
                result.looks[slot][i]=row.charAt(i)-'0';
        }
        return result;
    }
}

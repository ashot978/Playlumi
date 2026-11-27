package nl.playlumi.app;

/** Presentation only, with the original puzzle visible until the answer is credited. */
public final class PlaylumiSkillVisual {
    private PlaylumiSkillVisual(){}
    public static int dots(String display){
        if(display==null)return 0;
        String[] parts=display.trim().split("\\s+");
        if(parts.length>4)return 0;
        for(String part:parts)if(!"●".equals(part))return 0;
        return parts.length;
    }
    public static int corners(String shape){
        if("●".equals(shape))return 0;
        if("▲".equals(shape))return 3;
        if("■".equals(shape)||"◆".equals(shape)||"▭".equals(shape))return 4;
        return -1;
    }
    public static boolean supported(int mode,int variant,LearningGame.Round q){
        if(q==null||!PlaylumiSkillPractice.supports(mode,variant))return false;
        if(mode==LearningGame.COUNT)return dots(q.display)>0;
        if(mode==LearningGame.SHAPES)return corners(q.display)>=0;
        if(!PlaylumiSkillPractice.revealsBlank(mode,variant))return false;
        String[] parts=q.display.trim().split("\\s+");int blanks=0;
        for(String token:parts)if("?".equals(token))blanks++;
        return blanks==1&&parts.length>=3&&parts.length<=9;
    }
    public static int columns(int widthDp,int count){
        return Math.max(1,Math.min(count,Math.max(2,Math.min(5,(widthDp-24)/54))));
    }
    public static int rows(int widthDp,int count){
        int cols=columns(widthDp,count);return (count+cols-1)/cols;
    }
    public static String[] tokens(LearningGame.Round q){
        String[] tokens=q.display.trim().split("\\s+");
        if(q.isSolved())for(int i=0;i<tokens.length;i++)
            if("?".equals(tokens[i]))tokens[i]=q.answers.get(q.correctIndex);
        return tokens;
    }
    public static int filledIndex(LearningGame.Round q){
        if(!q.isSolved())return -1;
        String[] tokens=q.display.trim().split("\\s+");
        for(int i=0;i<tokens.length;i++)if("?".equals(tokens[i]))return i;
        return -1;
    }
    public static int dotDelta(LearningGame.Round q){
        if(!q.isSolved()||dots(q.display)==0)return 0;
        try{
            int delta=Integer.parseInt(q.answers.get(q.correctIndex))-dots(q.display);
            return delta==1||delta==-1?delta:0;
        }catch(NumberFormatException ignored){return 0;}
    }
    public static String description(int mode,LearningGame.Round q){
        if(!q.isSolved())return q.display;
        if(mode==LearningGame.SHAPES)return q.display+" · "+q.answers.get(q.correctIndex)+" hoeken";
        if(mode==LearningGame.COUNT)return q.display+" · "+q.answers.get(q.correctIndex);
        return String.join(" ",tokens(q));
    }
}

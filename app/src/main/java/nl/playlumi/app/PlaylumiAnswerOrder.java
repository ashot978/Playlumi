package nl.playlumi.app;
import java.util.List;

/** Spatial and binary choices have a predictable position in every round, including restored rounds. */
public final class PlaylumiAnswerOrder {
    private PlaylumiAnswerOrder(){}
    public static boolean horizontalPair(List<String> values){return values.contains("Links")&&values.contains("Rechts");}
    public static boolean verticalPair(List<String> values){return values.size()==2&&values.contains("Boven")&&values.contains("Onder");}
    public static int[] indices(List<String> values){
        int[] order=new int[values.size()];
        String[][] fixed={{"Links","Rechts","Gelijk"},{"Boven","Onder"},{"Voor","Achter"},{"Ja","Nee"},{"Noord","Oost","Zuid","West"}};
        String[] chosen=null;
        for(String[] pair:fixed)if(values.contains(pair[0])&&values.contains(pair[1])){chosen=pair;break;}
        if(chosen==null){for(int i=0;i<order.length;i++)order[i]=i;return order;}
        int n=0;boolean[] used=new boolean[values.size()];
        for(String label:chosen){int i=values.indexOf(label);if(i>=0&&!used[i]){order[n++]=i;used[i]=true;}}
        for(int i=0;i<used.length;i++)if(!used[i])order[n++]=i;
        return order;
    }
}

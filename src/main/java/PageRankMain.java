import jdk.nashorn.internal.ir.Terminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PageRankMain {

    private static HashMap<String, Node> graph = new HashMap<String, Node>();
    private static int N = 0; // Number of nodes in graph
    // The max iteration rounds to terminate the algorithm, it will be changed
    // in initPageRank() function as it should be depends on the scale of the graph
    private static int ITERATION = 100;
    private final static BigDecimal alpha = BigDecimal.valueOf(0.85);
    private static BigDecimal epsilon;

    public void initPageRank(){
        N = graph.size();
        ITERATION *= N;
        epsilon = new BigDecimal(1).divide(BigDecimal.valueOf(10 * N),3, BigDecimal.ROUND_HALF_UP);
        for(Map.Entry entity : graph.entrySet()){
            Node node = (Node) entity.getValue();
            node.setPR(node.getPR().divide(BigDecimal.valueOf(N), 3, BigDecimal.ROUND_HALF_UP));
        }
    }

    public void calPageRankValue(){
        int round = 0;
        boolean checkTerminate;
        do{
            checkTerminate = true;
            for(Map.Entry entity : graph.entrySet()) {
                BigDecimal inNodePR = BigDecimal.valueOf(0);
                Node node = (Node) entity.getValue();
                ArrayList<String> inNeighbours = node.getInNeighbours();
                for(String nodeName:inNeighbours){
                    Node inNode = graph.get(nodeName);
                    inNodePR.add(inNode.getPR().divide(inNode.getOutDegree(), 3, BigDecimal.ROUND_HALF_UP));
                }
                BigDecimal newPR = inNodePR.multiply(alpha).add(
                        (BigDecimal.valueOf(1).subtract(alpha))
                        .divide(BigDecimal.valueOf(N), 3, BigDecimal.ROUND_HALF_UP));

                if( node.getPR().subtract(newPR).compareTo(epsilon) > 0 ){
                    checkTerminate = false;
                }
                node.setPR(newPR);
            }
        }while(!checkTerminate && round < ITERATION);
        //If the changes of all nodes' PR smaller than epsilon or iteration time up to maximum, then terminate.
    }

    private static boolean checkTermination(){
        for(Map.Entry entity : graph.entrySet()){
            Node node = (Node) entity.getValue();
            if(node.getEpsilon().compareTo(epsilon) > 0){
                return false;
            }
        }
        return true;
    }

    public void constructGraph(ArrayList<String> rawData){
        for(String line : rawData){
            Node node1 = null;
            Node node2 = null;
            String[] nodes = line.replaceAll("[ |\t]+"," ").split(" ");
            String vx = nodes[0];
            String vy = nodes[1];

            if(graph.containsKey(vx)){
                node1 = graph.get(vx);
            }else {
                node1 = new Node(vx);
                graph.put(vx, node1);
            }
            if(graph.containsKey(vy)){
                node2 = graph.get(vy);
            }else {
                node2 = new Node(vy);
                graph.put(vy, node2);
            }

            node1.setOutDegree( node1.getOutDegree().add(BigDecimal.valueOf(1)) );
            node2.getInNeighbours().add( node1.getId() );
        }
    }

    public static ArrayList<String> readData(String filePath){
        String line = "";
        ArrayList<String> rawData = new ArrayList<String>();
        try{
            File file = new File(filePath);
            if(!file.exists()){
                throw new Exception("File is not exist, please check the file path.");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            while( (line=br.readLine()) != null ){
                rawData.add(line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return rawData;
    }

    private class Node{
        private String id;
        private ArrayList<String> inNeighbours;
        private BigDecimal outDegree;
        private BigDecimal PR;
        private BigDecimal epsilon;

        public Node(String id){
            this.PR = new BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP);
            this.inNeighbours = new ArrayList<String>();
            this.id = id;
            this.outDegree = BigDecimal.valueOf(1);
            this.epsilon = BigDecimal.valueOf(0.01);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ArrayList<String> getInNeighbours() {
            return inNeighbours;
        }

        public void setInNeighbours(ArrayList<String> inNeighbours) {
            this.inNeighbours = inNeighbours;
        }

        public BigDecimal getPR() {
            return PR;
        }

        public void setPR(BigDecimal PR) {
            this.PR = PR;
        }

        public BigDecimal getOutDegree() {
            return outDegree;
        }

        public void setOutDegree(BigDecimal outDegree) {
            this.outDegree = outDegree;
        }

        public BigDecimal getEpsilon() {
            return epsilon;
        }

        public void setEpsilon(BigDecimal epsilon) {
            this.epsilon = epsilon;
        }
    }

    public static void main(String [] arg) {
        if (arg.length < 1) {
            System.out.println("Please input the file path of graph data.");
            return;
        }
        String filePath = arg[0];

        for(Map.Entry entity : graph.entrySet()) {
            Node node = (Node) entity.getValue();
            System.out.println(node.getId() + " " + node.getPR());
        }
    }
}

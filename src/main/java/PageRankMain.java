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
    private static BigDecimal alpha = BigDecimal.valueOf(0.85);
    private static BigDecimal beta = BigDecimal.valueOf(0.15);
    private static BigDecimal epsilon;  //error range

    /**
     * Initial nodes values of graph size, iteration times, error range, nodes' outdegree, and RP default PR
     */
    public void initPageRank(){
        N = graph.size();
        ITERATION *= N;
        epsilon = new BigDecimal(1).divide(BigDecimal.valueOf(1000000 * N),6, BigDecimal.ROUND_HALF_UP);
        for(Map.Entry entity : graph.entrySet()){
            Node node = (Node) entity.getValue();
            node.setPR(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(N), 6, BigDecimal.ROUND_HALF_UP));
            if(node.getOutDegree().intValue() == 0){
                node.setOutDegree(BigDecimal.valueOf(1));
            }
        }
    }

    /**
     * Calculate PageRank values.
     * Xi = alpha * SUM(Xj / outdegree of j) + beta
     * Termination: |Xi - Xi-1| <= epsilon or iteration times up to maximum
     */
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
                    inNodePR = inNodePR.add(inNode.getPR().divide(inNode.getOutDegree(), 6, BigDecimal.ROUND_HALF_UP));
                }
                BigDecimal newPR = inNodePR.multiply(alpha).add(beta);

                // Check error range
                if( node.getPR().subtract(newPR).abs().compareTo(epsilon) > 0 ){
                    checkTerminate = false;
                }

                //update new PR value
                node.setPR(newPR);
            }
            round++;
        }while(!checkTerminate && round < ITERATION);
        //If the changes of all nodes' PR smaller than epsilon or iteration time up to maximum, then terminate.
    }


    /**
     * Construction a graph<Node Name, Node info>
     * @param rawData
     */
    public void constructGraph(ArrayList<String> rawData){
        for(String line : rawData){
            Node node1;
            Node node2;
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
        private String id;  //Node name
        private ArrayList<String> inNeighbours;  //The nodes direct to it
        private BigDecimal outDegree;  // The number of out-links .
        private BigDecimal PR;

        public Node(String id){
            this.PR = new BigDecimal(0).setScale(6, BigDecimal.ROUND_HALF_UP);
            this.inNeighbours = new ArrayList<String>();
            this.id = id;
            this.outDegree = BigDecimal.valueOf(0);
        }

        public String getId() {
            return id;
        }

        public ArrayList<String> getInNeighbours() {
            return inNeighbours;
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
    }

    public static void main(String [] arg) {
        if (arg.length < 3) {
            System.out.println("Please input the file path of graph data, alpha value and beta value.");
            return;
        }
        String filePath = arg[0];
        alpha = BigDecimal.valueOf(Double.valueOf(arg[1]));
        beta = BigDecimal.valueOf(Double.valueOf(arg[2]));

        //Read data from file
        ArrayList<String> rawData = readData(filePath);

        PageRankMain pageRankMain = new PageRankMain();
        pageRankMain.constructGraph(rawData);  //construct a graph based on rawdata
        pageRankMain.initPageRank();  //initial the nodes' data
        pageRankMain.calPageRankValue();  // calculate the RP values of nodes

        //Print out the result
        for(Map.Entry entity : graph.entrySet()) {
            Node node = (Node) entity.getValue();
            System.out.println("[" + node.getId() + "]\t" + node.getPR());
        }
    }
}

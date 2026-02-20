package router;
import config.ConfigParser;
import config.ConfigTypes;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;

public class router {
    private final String id;
    private final int  gatewayPort;
    private DatagramSocket socket;
    private final Map<String, Integer> routingTable = new LinkedHashMap<>();

    public router(String id){
        this.id = id;
        try {
            ConfigTypes.RouterConfig myConfig = ConfigParser.getRouterConfig(id);
            this.gatewayPort = myConfig.realPort();
            this.socket = new DatagramSocket(gatewayPort);

            System.out.println("Config loaded for " + id);
            String[] neighbors = myConfig.virtualIPs();
            for (String neighbor : neighbors){
                ConfigTypes.RouterConfig neighborConfig = ConfigParser.getRouterConfig(neighbor);
                routingTable.put(neighborConfig.realIP(), neighborConfig.realPort());
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void create_routing_table(String subnetPrefix, Integer port){
        if(!routingTable.containsKey(subnetPrefix) || !routingTable.containsValue(port)){
            routingTable.put(subnetPrefix, port);
        }
    }

    public void print_routing_table(){
        System.out.println("\n========= ROUTING TABLE (ID: " + this.id + ") =========");
        System.out.printf("%-15s | %-10s%n", "Subnet Prefix", "Next Hop");
        System.out.println("-------------------------------------------");
        for (Map.Entry<String, Integer> entry : routingTable.entrySet()){
            System.out.printf("%-15s | %-10s%n", entry.getKey(), entry.getValue());
        }
    }

    private void processFrame(String frame) throws SocketException {
        this.socket = new DatagramSocket();
        String[] parts= frame.split(":", 5);
        if (parts.length != 5) {
            System.out.println("[ROUTER " + this.id + "] bad frame (needs 5 fields): " + frame);
            return;
        }
        String srcMac = parts[0].trim();
        String dstMac = parts[1].trim();
        String srcIp = parts[2].trim();
        String dstIp = parts[3].trim();
        String msg = parts[4];

        System.out.println("ROUTER " + parts.length);
    }

    static void main(String[] args){
        router r1 = new router("R1");
        System.out.println(r1.id + r1.gatewayPort);
    }
}

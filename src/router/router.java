package router;
import config.ConfigParser;
import config.DeviceConfig;

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
            DeviceConfig myConfig = ConfigParser.getConfigForDevice(id);
            this.gatewayPort = myConfig.port();
            this.socket = new DatagramSocket(gatewayPort);

            System.out.println("Config loaded for " + id);
            String[] neighbors = myConfig.neighbors();
            for (String neighbor : neighbors){
                DeviceConfig neighborConfig = ConfigParser.getConfigForDevice(neighbor);
                routingTable.put(neighborConfig.ipAddress(), neighborConfig.port());
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

    public void addEntry(String subnet, Integer port) {
        routingTable.put(subnet, port);
    }

    private void processFrame(String frame) throws SocketException {
        this.socket = new DatagramSocket();
    }

    static void main(String[] args){
        router r1 = new router("r1");

        r1.addEntry("net1", 3000);
        r1.addEntry("net2", 3001);
        r1.addEntry("net3", 3002);

        r1.print_routing_table();
    }
}

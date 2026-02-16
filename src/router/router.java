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
    private final Map<String, String> routingTable = new LinkedHashMap<>();

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
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void create_routing_table(String subnetPrefix, String nextHop){
        if(!routingTable.containsKey(subnetPrefix) || !routingTable.get(subnetPrefix).equals(nextHop)){
            routingTable.put(subnetPrefix, nextHop);
        }
    }

    public void print_routing_table(){
        System.out.println("\n========= ROUTING TABLE (ID: " + this.id + ") =========");
        System.out.printf("%-15s | %-10s%n", "Subnet Prefix", "Next Hop");
        System.out.println("-------------------------------------------");
        for (Map.Entry<String, String> entry : routingTable.entrySet()){
            System.out.printf("%-15s | %-10s%n", entry.getKey(), entry.getValue());
        }
    }

    public void addEntry(String subnet, String nextHop) {
        routingTable.put(subnet, nextHop);
    }

    private void processFrame(String frame) throws SocketException {
        this.socket = new DatagramSocket();
    }

    static void main(String[] args){
        router r1 = new router("r1");

        r1.addEntry("net1", "left port");
        r1.addEntry("net2", "right port");
        r1.addEntry("net3", "net2.R2");

        r1.print_routing_table();
    }
}

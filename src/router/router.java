package router;
import config.ConfigParser;
import config.ConfigTypes;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class router {
    private final String id;
    private final int  gatewayPort;
    private DatagramSocket socket;
    private final Map<String, InetSocketAddress> routingTable = new LinkedHashMap<>();
    private final Map<String, String> virtualRoutingTable = new HashMap<>();

    public router(String id){
        this.id = id;
        try {
            ConfigTypes.RouterConfig myConfig = ConfigParser.getRouterConfig(id);
            this.gatewayPort = myConfig.realPort();
            this.socket = new DatagramSocket(gatewayPort);
            String ip = null;
            int port = -1;

            System.out.println("Config loaded for " + id);
            Map<String, String> forwarder = myConfig.forwardingTable();
            for (Map.Entry<String, String> entry : forwarder.entrySet()){
                String subnet = entry.getKey();
                String nextHopId = entry.getValue();
                virtualRoutingTable.put(subnet, nextHopId);
                ConfigTypes.RouterConfig nextHopConfig = ConfigParser.getRouterConfig(nextHopId);
                if(nextHopConfig != null) {
                    routingTable.put(subnet, new InetSocketAddress(nextHopConfig.realIP(), nextHopConfig.realPort()));
                }
                else {
                    ConfigTypes.SwitchConfig switchConfig = ConfigParser.getSwitchConfig(nextHopId);
                    if(switchConfig !=  null) {
                        ip = switchConfig.ipAddress();
                        port = switchConfig.port();
                    }
                    else{
                        ConfigTypes.HostConfig hostConfig = ConfigParser.getHostConfig(nextHopId);
                        if(hostConfig != null){
                            ip = hostConfig.realIP();
                            port = hostConfig.realPort();
                        }
                    }
                    if(ip != null && port != -1){
                        routingTable.put(subnet, new InetSocketAddress(ip, port));
                    }else {
                        System.err.println("NPE Prevention: ID '" + nextHopId + "' not found in any config map!");
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void print_routing_table(){
        System.out.println("\n========= ROUTING TABLE (ID: " + this.id + ") =========");
        System.out.printf("%-15s | %-10s%n", "Subnet Prefix", "Next Hop");
        System.out.println("-------------------------------------------");
        for (Map.Entry<String, String> entry :virtualRoutingTable.entrySet()){
            System.out.printf("%-15s | %-10s%n", entry.getKey(), entry.getValue());
        }
    }

    private void processFrame(String frame) throws SocketException {
        // format: srcMac:dstMac:srcIp:dstIp:msg
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

        System.out.println("[ROUTER " + this.id + "] " +
                "srcMac=" + srcMac + " dstMac=" + dstMac +
                " srcIp=" + srcIp + " dstIp=" + dstIp + " msg=" + msg);

        // get "net3" from "net3.D", then turn it into "subnet3"
        String key = "subnet" + dstIp.split("\\.", 2)[0].substring(3);

        InetSocketAddress next = routingTable.get(key);
        String nextHopId = virtualRoutingTable.get(key);

        if (next == null || nextHopId == null) {
            System.out.println("[ROUTER " + this.id + "] no route for " + dstIp + " (key=" + key + ")");
        }
    }

    static void main(String[] args){
        if(args.length != 1){
            System.out.println("Please provide Router ID in Arguments");
        }
        router r1 = new router(args[0]);
        r1.print_routing_table();
    }
}

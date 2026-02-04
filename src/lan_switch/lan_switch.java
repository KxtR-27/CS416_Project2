package lan_switch;
import config.ConfigParser;
import config.DeviceConfig;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class lan_switch {
    private final String id;
    private String ipAddress;
    private int listeningPort;
    private final Map<String, DeviceConfig> neighborConfigs = new HashMap<>();
    private final Map<String, Integer> switchTable = new HashMap<>();
    private final Map<Integer, DeviceConfig> virtualPorts = new HashMap<>();

    private void create_and_update_switch_table(String sourceIP, int port){
        if(!switchTable.containsKey(sourceIP) || switchTable.get(sourceIP) != port){
            if (!switchTable.containsKey(sourceIP)) {
                System.out.println("NEW HOST LEARNED: " + sourceIP + " on Port " + port);
            } else {
                System.out.println("HOST MOVED: " + sourceIP + " moved to Port " + port);
            }
            switchTable.put(sourceIP, port);
            display_switch_table();
        }
    }

    public void display_switch_table(){
        System.out.println("\n========= SWITCH TABLE (ID: " + this.id + ") =========");
        System.out.printf("%-15s | %-10s%n", "Source IP/MAC", "Port");
        System.out.println("-------------------------------------------");

        for (Map.Entry<String, Integer> entry : switchTable.entrySet()) {
            System.out.printf("%-15s | %-10d%n", entry.getKey(), entry.getValue());
        }
        System.out.println("===========================================\n");
    }

    public lan_switch(String id){
        this.id = id;
        DeviceConfig myConfig = ConfigParser.getConfigForDevice(id);
        if(myConfig != null){
            this.ipAddress = myConfig.ipAddress();
            this.listeningPort = myConfig.port();
            System.out.println("Config loaded for " + id);
            String[] neighbors = myConfig.neighbors();
            for(int i = 0; i < neighbors.length; i ++){
                DeviceConfig neighborConfig = ConfigParser.getConfigForDevice(neighbors[i]);
                if (neighborConfig != null){
                    int portNum = i + 1;
                    virtualPorts.put(portNum, neighborConfig);
                    neighborConfigs.put(neighbors[i], neighborConfig);
                }
            }
        }
        else {
            System.out.println("No Configuration found with " + id);
        }
    }
    public void processPacket(String sourceIP, String destinationIP, String data, int incomingPort) throws IOException {
        create_and_update_switch_table(sourceIP, incomingPort);
        try (DatagramSocket hostSocket = new DatagramSocket()){
            if(!switchTable.containsKey(destinationIP)){
                System.out.println("FLOODING: Destination " + destinationIP + " unknown.");
                for(DeviceConfig neighbor : virtualPorts.values()){
                    if (neighbor.port() != incomingPort){
                        sendUDP(hostSocket, destinationIP, neighbor.port(), data);
                    }
                }
            }
            else {
                int targetPort = switchTable.get(destinationIP);
                System.out.println("FORWARDING: Sending " + data + " to Port " + targetPort);
                sendUDP(hostSocket, destinationIP, targetPort, data);
            }
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void startListening() {
        try (DatagramSocket socket = new DatagramSocket(this.listeningPort)) {
            System.out.println("Switch " + id + " online on port " + listeningPort);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                socket.receive(p);

                // Expecting -> "SRC:DEST:MSG"
                String frame = new String(p.getData(), 0, p.getLength()).trim();
                String[] parts = frame.split(":", 3);

                if (parts.length == 3) {
                    String src = parts[0];
                    String dest = parts[1];
                    String data = parts[2];
                    int incomingPort = p.getPort();
                    processPacket(src, dest, data, incomingPort);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUDP(DatagramSocket socket, String ip, int port, String data) throws IOException {
        byte[] buffer = data.getBytes();
        DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                InetAddress.getByName(ip),
                port
        );
        socket.send(packet);
    }



    static void main(String[] args) {
        if (args.length > 0){
            try{
                String inputID = args[0];
                lan_switch lanSwitch = new lan_switch(inputID);
                lanSwitch.display_switch_table();
                lanSwitch.startListening();
            }
            catch (NumberFormatException e){
                System.err.println("Argument Must Be Device ID");
            }
        }
        else {
            System.out.println("Please provide a Switch ID in the run config");
        }
    }
}
package lan_switch;

import config.ConfigParser;
import config.DeviceConfig;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Switch {
    private final String id;
	private int listeningPort;
	private final Map<String, Integer> switchTable = new HashMap<>();
    private final Map<Integer, DeviceConfig> virtualPorts = new HashMap<>();
    private DatagramSocket listeningSocket;

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

    public Switch(String id){
        this.id = id;
        DeviceConfig myConfig = ConfigParser.getConfigForDevice(id);
        if(myConfig != null){
			this.listeningPort = myConfig.port();
            System.out.println("Config loaded for " + id);
            String[] neighbors = myConfig.neighbors();
            for (String neighbor : neighbors) {
                DeviceConfig neighborConfig = ConfigParser.getConfigForDevice(neighbor);
                if (neighborConfig != null) {
                    virtualPorts.put(neighborConfig.port(), neighborConfig);
                }
            }
        }
        else {
            System.out.println("No Configuration found with " + id);
        }
    }
    public void processPacket(String sourceIP, String destinationIP, String fullFrame, int incomingPort) throws IOException {
        create_and_update_switch_table(sourceIP, incomingPort);
        if (!switchTable.containsKey(destinationIP)) {
            System.out.println("FLOODING: Destination " + destinationIP + " unknown.");
            for (DeviceConfig neighbor : virtualPorts.values()) {
                if (neighbor.port() != incomingPort) {
                    sendUDP(this.listeningSocket, neighbor.ipAddress(), neighbor.port(), fullFrame);
                }
            }
        }
            else {
                int targetPort = switchTable.get(destinationIP);
                DeviceConfig targetDevice = virtualPorts.get(targetPort);
                if (targetDevice != null) {
                    System.out.println("FORWARDING: Sending " + fullFrame + " to Port " + targetPort);
                    sendUDP(this.listeningSocket, targetDevice.ipAddress(), targetPort, fullFrame);
                }
                else {
                    System.err.println("Port " + targetPort + " has no associated DeviceConfig.");
                }
            }
        }


    public void startListening() {
        try {
            this.listeningSocket = new DatagramSocket(this.listeningPort);
            System.out.println("Switch " + id + " online on port " + listeningPort);
            byte[] buffer = new byte[1024];

            // loop is manually interrupted
            //noinspection InfiniteLoopStatement
            while (true) {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                this.listeningSocket.receive(p);
                int portCheck = p.getPort();

                if (portCheck == this.listeningPort){
                    continue;
                }

                // Expecting -> "SRC:DEST:MSG"
                String frame = new String(p.getData(), 0, p.getLength()).trim();
                String[] parts = frame.split(":", 3);

                if (parts.length == 3) {
                    String src = parts[0];
                    String dest = parts[1];
                    int incomingPort = p.getPort();
                    processPacket(src, dest, frame, incomingPort);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void sendUDP(DatagramSocket socket, String ip, int port, String fullFrame) throws IOException {
        byte[] buffer = fullFrame.getBytes();
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
                Switch lanSwitch = new Switch(inputID);
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
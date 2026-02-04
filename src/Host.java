import config.ConfigParser;
import config.DeviceConfig;

import java.net.*;
import java.util.Scanner;

public class Host {

    private String hostId;
    private String hostIp;
    private int hostPort;

    private String switchIp;
    private int switchPort;

    private DatagramSocket socket;

    public Host(String hostId) throws Exception {
        this.hostId = hostId;
        Config();
        socket = new DatagramSocket(hostPort);
        System.out.println("Host " + hostId + " started on " + hostIp + ":" + hostPort);
        System.out.println("Connected to switch at " + switchIp + ":" + switchPort);
    }

    private void Config() {
        DeviceConfig deviceConfig = ConfigParser.getConfigForDevice(hostId);
        if (deviceConfig == null) {
            throw new RuntimeException("No config found for host " + hostId);
        }

        hostIp = deviceConfig.ipAddress();
        hostPort = deviceConfig.port();

        String[] neighbors = deviceConfig.neighbors();
        if (neighbors.length == 0) {
            throw new RuntimeException("Host has no neighbors in config");
        }

        String switchId = neighbors[0];

        DeviceConfig switchConfig = ConfigParser.getConfigForDevice(switchId);
        if (switchConfig == null) {
            throw new RuntimeException("No config found for switch " + switchId);
        }

        switchIp = switchConfig.ipAddress();
        switchPort = switchConfig.port();
    }

    public void startReceiver() {
        Thread receiver = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String frame = new String(packet.getData(), 0, packet.getLength());
                    handleFrame(frame);
                }
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        receiver.setDaemon(true);
        receiver.start();
    }

    // handleFrame - Cam
    // frame looks like SRC:DST:MSG
    // break it up
    // make sure it has 3 parts
    // if for me -> show msg
    // if not -> debug print
    private void handleFrame(String frame) {

        String[] parts = frame.split(":");

        if (parts.length < 3) {
            System.out.println("Bad frame received: " + frame);
            return;
        }

        String src = parts[0];
        String dst = parts[1];
        String msg = parts[2];


        if (dst.equals(hostId)) {
            System.out.println("Message from " + src + ": " + msg);
        } else {
            // flooded frame
            System.out.println("Frame for " + dst + " received at " + hostId + " (MAC mismatch)");
        }
    }

    private void startSender() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter destination host ID: ");
            String dst = scanner.nextLine().trim();

            System.out.print("Enter message: ");
            String msg = scanner.nextLine().trim();

            String frame = hostId + ":" + dst + ":" + msg;
            sendFrame(frame);
        }
    }

    private void sendFrame(String frame) {
        try {
            byte[] data = frame.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName(switchIp),
                    switchPort
            );
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(){
        startReceiver();
        startSender();
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java Host <HOST_ID>");
            return;
        }
        Host host = new Host(args[0]);
        host.start();
    }
}
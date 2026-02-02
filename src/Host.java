import java.net.*;
import java.util.Scanner;

public class Host {

    private String hostId;
    private String hostIp;
    private int hostPort;

    private String switchIp;
    private int switchPort;

    private DatagramSocket socket;

    public void config() {
        switchIp = "127.0.0.1";
        int port = 3000;
        switch (hostId) {
            case "A" :
                hostIp = "127.0.0.1";
                hostPort = 5001;
                break;
            case "B" :
                hostIp = "127.0.0.1";
                hostPort = 5002;
                break;
            case "C":
                hostIp = "127.0.0.1";
                hostPort = 5003;
                break;
            case "D":
                hostIp = "127.0.0.1";
                hostPort = 5004;
                break;
        }
    }
    public void startReciever() {
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
    // make frame
    // send to switch
    // try/catch

    private void sendFrame(String dst, String msg) {

        String frame = hostId + ":" + dst + ":" + msg;

        try {
            byte[] data = frame.getBytes();
            InetAddress addr = InetAddress.getByName(switchIp);
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, switchPort);
            socket.send(packet);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public static void main(String[] args) throws Exception {
    }
}
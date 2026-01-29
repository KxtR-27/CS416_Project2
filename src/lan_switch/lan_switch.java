package lan_switch;
import config.ConfigParser;
import config.DeviceConfig;
import java.util.HashMap;
import java.util.Map;

public class lan_switch {
    private final String id;
    private String ipAddress;
    private int listeningPort;
    private final Map<String, DeviceConfig> neighborConfigs = new HashMap<>();
    private final Map<String, Integer> switchTable = new HashMap<>();
    private final Map<Integer, DeviceConfig> virtualPorts = new HashMap<>();

    private void create_switch_table(String sourceIP, int port){
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

    private void update_switch_table(String sourceIP, int port){
        System.out.println("Updating IP to: " + sourceIP + " to now port" + port);
        switchTable.put(sourceIP, port);
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

    public static void main(String[] args) {
        if (args.length > 0){
            try{
                String inputID = args[0];
                lan_switch lanSwitch = new lan_switch(inputID);
                lanSwitch.display_switch_table();
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
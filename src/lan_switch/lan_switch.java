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
    private final Map<Integer, DeviceConfig> virtualPorts = new HashMap<>();

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

    static void main(String[] args) {
        if (args.length < 1){
            try{
                String inputID = args[0];
                lan_switch lanSwitch = new lan_switch(inputID);
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
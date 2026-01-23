public class Switch {
    String firstMacAddress = "A";
    String secondMacAddress = "B";
    String data = "Hello World";
    String firstPort = "3000";
    String secondPort = "4000";

    private void sendFrame(String sourceMacAddress, String destinationMacAddress, String userData){

    }
    private void recieveFrame(String sourceMacAddress, String destinationMacAddress, String data){

    }
    private void createSwitchTable(String sourceMacAddress, String port){

    }
    private void updateSwitchTable(String sourceMacAddress, String port){

    }
    public static void main(String[] args){
    Switch virtualSwitch = new Switch();
    virtualSwitch.sendFrame(virtualSwitch.firstMacAddress, virtualSwitch.secondMacAddress, virtualSwitch.data);
    }
}
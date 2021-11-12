import gearth.extensions.Extension;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.extra.tools.ChatConsole;
import gearth.extensions.parsers.*;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import java.util.*;

@ExtensionInfo(
        Title =  "RoomPricer",
        Description =  "This extension will search for the pioce of furnis in the marketplace, using it's meadian will calculate the total value of a room.",
        Version =  "1.0",
        Author =  "denio4321"
)
class RoomEvaluator extends Extension {
    private ChatConsole mChatConsole;
    private HFloorItem[] FurniList;
    public List<RoomFurni> roomFurnis = new ArrayList<>();
    private String roomName;
    public static void main(String[] args) {
        new RoomEvaluator(args).run();
    }
    public RoomEvaluator(String[] args) {
        super(args);
    }

    @Override
    protected void initExtension() {

        intercept(HMessage.Direction.TOCLIENT, "Objects", this::parseFurnis);
        intercept(HMessage.Direction.TOCLIENT, "MarketplaceItemStats", this::parseMarketPlaceOffers);
        intercept(HMessage.Direction.TOSERVER, "OpenFlatConnection", this::enterNewRoom);
        intercept(HMessage.Direction.TOCLIENT, "GetGuestRoomResult", this::getRoomName);
        mChatConsole = new ChatConsole(this, "Enter a room and once i processed all the info i will notify you the price!");
    }

    private void getRoomName(HMessage hMessage){
        HPacket packet = hMessage.getPacket();
        packet.readBoolean();
        packet.readInteger();
        roomName = packet.readString();
    }

    private void enterNewRoom(HMessage hMessage){
        roomFurnis.clear();
    }

    private void parseFurnis(HMessage hMessage) {
        this.FurniList = HFloorItem.parse(hMessage.getPacket());
        Thread thread = new Thread(this::getFurniData);
        thread.start();
    }

    private void parseMarketPlaceOffers(HMessage hMessage){
        HPacket packet = hMessage.getPacket();
        int median = packet.readInteger();
        int typeId = packet.readInteger(packet.getBytesLength() - 4);
        for(RoomFurni f: roomFurnis){
            if(f.getTypeId() == typeId){
                f.setMedian(median);
                System.out.println(f);
            }
        }
    }
    private void sendData(){
        int totalFurnis = 0, totalPrice = 0;
        for(RoomFurni f: roomFurnis){
            totalFurnis += f.quantity;
            totalPrice += f.median*f.quantity;
        }
        mChatConsole.writeOutput("¥==================¥" + "\nResults for " + roomName + "\nTotal furnis: " + totalFurnis + "\nTotal price: " + totalPrice + "$", false);
    }
    private void getFurniData() {
        for(HFloorItem furni: FurniList){
            boolean isInList = false;
            for(RoomFurni f: roomFurnis){
                if(f.getTypeId() == furni.getTypeId()){
                    isInList = true;
                    f.quantity += 1;
                }
            }

            if(!isInList){
                this.roomFurnis.add(new RoomFurni(0, furni.getTypeId(), 0, 1));
                sendToServer(new HPacket("GetMarketplaceItemStats", HMessage.Direction.TOSERVER, 1, furni.getTypeId()));
                try{
                    Thread.sleep(700);
                } catch(InterruptedException e) {
                    
                }
            }
        }
        this.sendData();
    }
}

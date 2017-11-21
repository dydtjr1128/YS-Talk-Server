import java.util.Vector;

public class Room {
	private int roomNum = -1;
	private String roomName = null;
	private String roomPassword = null;
	private int roomPeople = 0;	
	public Vector<MyServer.Receiver> receiver = new Vector<MyServer.Receiver>();
	public Room(int roomNum, String roomName, String roomPassword, int roomPeople) {
		this.roomNum = roomNum;
		this.roomName = roomName;
		this.roomPassword = roomPassword;
		this.roomPeople = roomPeople;
	}
	public int getRoomNum(){
		return roomNum;
	}
	public String getRoomName(){
		return roomName;
	}
	public int getRoomPeople(){
		return roomPeople;
	}
	public void setRoomPeople(int n){
		this.roomPeople = n;
	}	
	public boolean checkRoom(String password){
		if(this.roomPassword.equals(password))
			return true;
		return false;
	}
	public String toString(){
		return roomNum + "&1&1&" + roomName + "&1&1&" + roomPeople;
	}
}

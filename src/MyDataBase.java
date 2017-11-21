import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDataBase {
	private static MyDataBase INSTANCE;
	private Connection con = null;
	private Statement stat;
	private ResultSet rs;
	private final String url = "jdbc:mysql://localhost:3306/chatting";
	private final String user = "root";
	private String password = "q1w2e3";
	private MyDataBase() {	
		
	}
	public void init(){
		try {
			con = DriverManager.getConnection(url, user, password);
			stat = con.createStatement();
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}
	}
	public void closeDataBase(){
		try {
			con.close();
			stat.close();
			rs.close();
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}		
	}
	public void logoutAllClient(){
		try {
			stat.executeUpdate("update user set login=\"false\"");
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}
	}
	public boolean check(String password){
		if(this.password.equals(password)){
			return true;
		}
		return false;
	}
	public static MyDataBase get_Instance(){
		if(INSTANCE == null){
			INSTANCE = new MyDataBase();			
		}
		return INSTANCE;
	}
	public void changeLoginState(String name, String state){
		try {
			stat.executeUpdate("update user set login=\"" + state + "\"where name=\"" + name + "\"");
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}
	}
	synchronized public boolean loginAccess(String name, String password) {

		try {
			rs = stat.executeQuery("select name,password,login from user where name=\"" + name + "\"");
			while (rs.next()) {
				if (rs.getString("name").equals(name) && rs.getString("password").equals(password)
						&& rs.getString("login").equals("false")) {						
					stat.executeUpdate("update user set login=\"" + "true" + "\"where name=\"" + name + "\"");									
					return true;
				}
			}
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}		
		return false;
	}

	public boolean isMember(String name) {
		try {
			rs = stat.executeQuery("select name from user where name=\"" + name + "\"");
			while (rs.next()) {
				if (rs.getString("name").equals(name)) {					
					return true;
				}
			}
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}		
		return false;
	}

	synchronized public boolean memberJoin(String name, String PW) {
		try {
			rs = stat.executeQuery("select name from user where name=\"" + name + "\"");
			while (rs.next()) {
				if (rs.getString("name").equals(name)) {					
					return false;
				}
			}
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}
		try {
			stat.execute("insert into user values(\"" + name + "\",\"" + PW + "\",\"" + "false\")");
		} catch (SQLException e) {
			DebugMsg.handleError(e.getMessage());
		}
		return true;
	}
}

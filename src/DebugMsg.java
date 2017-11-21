
public class DebugMsg {
	public static void handleError(String text){
		System.out.println(text);
	}
	public static void handleError(Exception e){
		System.out.println(e.getMessage());
		e.printStackTrace();
	}
}

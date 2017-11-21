import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class MyServer extends JFrame {

	private ServerSocket listener = null;
	private ServerSocket listener2 = null;
	private MyTextPane showPane = new MyTextPane();
	private JScrollPane scrollPane = new JScrollPane(showPane);
	private JTextField sendField = new JTextField(10);
	
	private Vector<Receiver> users = new Vector<Receiver>();
	private Vector<Room> rooms = new Vector<Room>();
	private Vector<BufferedOutputStream> datas = new Vector<BufferedOutputStream>();
	private AESdecode decode = AESdecode.getInstance();
	private MyDataBase database;
	private HTMLEditorKit kit;
	private HTMLDocument doc;
	
	private JLabel clientLabel = new JLabel("클라이언트 수 : 0", JLabel.CENTER);
	private int clientCount = 0;
	private final int PORT_NUM = 9994;
	private int roomCount = 0;
	public MyServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("서버");
		setLocation(400, 400);

		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(clientLabel, BorderLayout.SOUTH);
		getContentPane().add(sendField, BorderLayout.NORTH);

		showPane.setEditable(false);
		sendField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = "서버>> " + sendField.getText();
				for (int i = 0; i < users.size(); i++) {
					Receiver r = users.get(i);
					if (r.login) {
						r.sendMsg(text);
					}
				}
				sendField.setText("");
				printHTML("<span>" + text + "</span>", "black");
			}
		});
		// showPane.setLineWrap(true);
		// showPane.setMargin(new Insets(10, 10, 10, 15));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				database.logoutAllClient();
			}
		});

		setup();
		setAutoRequestFocus(true);
		setSize(400, 400);
		setVisible(true);
	}

	public void setup() {
		Scanner scanner = new Scanner(System.in);
		String openPassword = null;
		database = MyDataBase.get_Instance();
		while (true) {
			System.out.printf("비밀번호 입력하세요>>");
			openPassword = scanner.nextLine();
			if (database.check(openPassword)) {
				break;
			} else {
				System.out.println("틀렸습니다. 다시입력하세요");
			}
		}
		try {
			listener = new ServerSocket(PORT_NUM);
			listener2 = new ServerSocket(PORT_NUM + 1);
			database.init();
			//String room[][] = { { "1", "열로와~", "3/4" }, { "2", "두번째방ㅋ~", "2/4" } };
			roomCount++;
			rooms.add(new Room(roomCount, "열로와~", "23234", 0));
			roomCount++;
			rooms.add(new Room(roomCount, "두번째방 ㅋㅋ", "2", 0));
			Thread th = new Thread(showPane);
			th.start();
		} catch (Exception e) {
			DebugMsg.handleError(e.getMessage());
			try {
				database.closeDataBase();
				listener.close();
			} catch (IOException e1) {
				DebugMsg.handleError(e1.getMessage());
			}
		}
	}	
	private void printHTML(String text, String color) {
		try {
			kit.insertHTML(doc, doc.getLength(), "<span Color='" + color + "'>" + text + "</span><br>", 0, 0,
					HTML.Tag.SPAN);
			showPane.setCaretPosition(doc.getLength());
		} catch (BadLocationException | IOException e1) {
			e1.printStackTrace();
		}
	}

	class MyTextPane extends JTextPane implements Runnable {

		MyTextPane() {
			kit = new HTMLEditorKit();
			setEditorKit(kit);
			StyleSheet css = kit.getStyleSheet();
			css.addRule("body {margin-left:12px; margin-top:12px; margin-right:12px;}");
			/*
			 * Font font = new Font("태 나무", Font.PLAIN, 14); String bodyRule =
			 * "body { font-family: " + font.getFamily() + "; " + "font-size: "
			 * + font.getSize() + "pt; }"; css.addRule(bodyRule);
			 */
			doc = (HTMLDocument) kit.createDefaultDocument();
			setDocument(doc);
		
		}

		public void run() {
			Socket socket = null;
			while (true) {
				try {
					socket = listener.accept();
					users.add(new Receiver(socket));
				} catch (IOException e) {
					DebugMsg.handleError(e.getMessage());
					try {
						socket.close();
						return;
					} catch (IOException e1) {
						DebugMsg.handleError(e1.getMessage());
					}
				}
			}
		}
	}

	class Receiver extends Thread {
		private Socket socket = null;
		private boolean login = false;
		private BufferedReader in = null;
		private BufferedWriter out = null;
		private String name = null;
		private InputStream is;
		private int roomNumber = -1;
		private BufferedInputStream dis;
		private BufferedOutputStream dos;
		private BufferedOutputStream dos2;
		private Room myRoom = null;
		public Receiver(Socket socket) {
			this.socket = socket;
			try {
				is = socket.getInputStream();
				in = new BufferedReader(new InputStreamReader(is));
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			} catch (IOException e) {
				DebugMsg.handleError(e.getMessage());
			}
			this.start();
		}

		private void sendMsg(String text) {
			try {
				out.write(decode.Encrypt(text) + "\n");
				out.flush();
			} catch (Exception e) {
				DebugMsg.handleError(e.getMessage());
			}
		}
		public int getRoomNumber() {
			return roomNumber;
		}

		public void setRoomNumber(int roomNumber) {
			this.roomNumber = roomNumber;
		}

		public void run() {
			String text = null;
			while (true) {
				try {
					
					text = in.readLine();
					
					//System.out.println("전    " + text);
					text = decode.Decrypt(text);
					//System.out.println("후    " + text);
					//System.out.println(myRoom.receiver.size());
					if (login) {
						//System.out.println(myRoom.receiver.size());
						if (text.equals("I&WILL&FILE@SEND@25F25&@$")) {// 25							
							try {
								Socket socket2 = listener2.accept();// 파일 전송용 소켓
																	// 채팅과 다른포트
								/*
								 * socket2.setTcpNoDelay(true);
								 * socket.setReceiveBufferSize(128);
								 */
								/* 저장할 파일의 객체 생성함 */
								String fileName = in.readLine();
								fileName = decode.Decrypt(fileName);
							//	System.out.println("fileName : " + fileName);
								//File f = new File("C:\\Users\\CYSN\\Desktop\\받기", fileName);

								/* 기록할 파일 연결함 */
								dis = new BufferedInputStream(socket2.getInputStream());
								//dos = new BufferedOutputStream(new FileOutputStream(f));
								dos2 = new BufferedOutputStream(socket2.getOutputStream());
								datas.add(dos2);
								byte buffer[] = new byte[1048576];
								/* 보내온 파일의 끝까지 읽어서 파일로 씀 */
								int size = 0;
								/*for(int i=0; i<users.size(); i++){									
									Receiver r = users.get(i);
									if (r.login && this.getRoomNumber() == r.roomNumber) {
										r.sendMsg("I&WILL&FILE@SEND@25F25&@$");
									}									
								}*/
								for(int i=0; i<myRoom.receiver.size(); i++){
									Receiver r = myRoom.receiver.get(i);
									if (!r.name.equals(this.name)) {
										r.sendMsg("I&WILL&FILE@SEND@25F25&@$" + fileName);
										Socket socket3 = listener2.accept();
										r.dos2 = new BufferedOutputStream(socket3.getOutputStream());										
									}
								}
							
								while ((size = dis.read(buffer)) != -1) {
									// Thread.sleep(100);
									//dos.write(buffer, 0, size);									
									for(int i=0; i<myRoom.receiver.size(); i++){
										Receiver r = myRoom.receiver.get(i);
										if (!r.name.equals(this.name)) {
											r.dos2.write(buffer,0,size);											
										}
									}
								}
								for(int i=0; i<myRoom.receiver.size(); i++){
									Receiver r = myRoom.receiver.get(i);
									if (!r.name.equals(this.name)) {
										r.dos2.close();										
									}
								}
								dis.close();
								dos.close();
								socket2.close();

							} catch (Exception e) {
								System.out.println(e.getMessage());
								DebugMsg.handleError(e + "sdf");
							}
							continue;
						}
						else if(text.startsWith("CAN@%I*ENTER*THE!$ROOM")){//22
							text = text.substring(22);							
							roomNumber = Integer.parseInt(text);
							if(roomNumber>0){
								printHTML("※" + name + "이 " + roomNumber + "방 입장!!", "red");
							/*	System.out.println(text + "입니다!!!!!!");
								System.out.println(text + "입니다!!!!!!");
								System.out.println(text + "입니다!!!!!!");
								System.out.println(text + "입니다!!!!!!");*/							
								sendMsg("true");
								for (int i = 0; i < users.size(); i++) {
									Receiver r = users.get(i);
									if (r.login && this.getRoomNumber() == r.roomNumber) {
										r.sendMsg("※" + name + " 이 방에 입장 하였습니다.");
									}
								}
								for(int i=0; i<rooms.size(); i++){
									Room room = rooms.get(i);
									if(room.getRoomNum() == roomNumber){
										int people = room.getRoomPeople()+1;
										room.setRoomPeople(people);
										myRoom = room;
										myRoom.receiver.addElement(this);
										break;
									}
								}
							}
							if(roomNumber==-5){
								sendMsg(Integer.toString(rooms.size()));
								Thread.sleep(10);
								for (int i = 0; i < rooms.size(); i++) {
									Room r = rooms.get(i);									
									sendMsg(r.toString());
									Thread.sleep(10);
								}
							}
						}
						else if(text.startsWith("CAN@%I*MAKE*THE!$ROOM")){//21
							//System.out.println(text + "  도착했음");
							text = text.substring(21);			
							//System.out.println("문자열은" + text);							
							String roomString[] = text.split("w22%df!@@rw");							
						//	System.out.println(roomString[0]+"이란다"+roomString[1]);
							if(roomString.length == 2){
								String roomName = roomString[0];
								String roomPassword = roomString[1];							
								roomCount++;
								myRoom = new Room(roomCount, roomName, roomPassword, 0);
								rooms.addElement(myRoom);
								myRoom.receiver.addElement(this);
							//	System.out.println(rooms.size() + "tttttttttttttttttttttttt");
								sendMsg("true");
							}
							else{
								sendMsg("false");
							}
							
						}
						else{
							System.out.println(myRoom.receiver.size());
							for (int i = 0; i < users.size(); i++) {
								Receiver r = users.get(i);
								if (r.login && this.getRoomNumber() == r.roomNumber) {
									r.sendMsg(name + ">> " + text);
								}
							}
							printHTML("<span>" + name + "(" + socket.getInetAddress() + ")>>" + text + "</span>", "black");
						}
					} else {// login false or not login
						String splitText[] = text.split("MySecretNumber19941128");
						if (splitText.length == 2) {// ID, PW
							if (database.loginAccess(splitText[0], splitText[1])) {
								sendMsg("true");
								this.name = splitText[0];
								login = true;
							} else {
								sendMsg("false");
							}
							if (login) {							
								printHTML("<span>※" + name + "(" + socket.getInetAddress() + ")이 로그인하였습니다.</span>",
										"black");
								clientCount++;
								clientLabel.setText("클라이언트 수 : " + Integer.toString(clientCount));
							}

						} else if (splitText.length == 3) {// join
							if (splitText[2].equals("check")) {
								if (database.isMember(splitText[0]))
									sendMsg("false");
								else
									sendMsg("true");
							} else if (splitText[2].equals("join")) {
								if (database.memberJoin(splitText[0], splitText[1]))
									sendMsg("ture");
								else
									sendMsg("false");
							}
						} else {
							sendMsg("false");
							continue;
						}
					}
				} catch (Exception e) {// error->exit
					DebugMsg.handleError(e.getMessage() + "113");
					users.remove(this);
					if (login) {
						for (int i = 0; i < users.size(); i++) {
							Receiver r = users.get(i);
							if (r.login && this.getRoomNumber() == r.roomNumber) {
								r.sendMsg("※" + name + " 이 퇴장하였습니다");
							}
						}
						for (int i = 0; i < rooms.size(); i++) {
							Room room = rooms.get(i);
							if(room.getRoomNum() == roomNumber && room.getRoomPeople()>0){
								//System.out.println(roomNumber + "  " + getRoomNumber() + "이여요");								
								room.setRoomPeople(room.getRoomPeople()-1);
								break;
							}
						}						
						if(myRoom != null){
							myRoom.receiver.remove(this);
						}
						printHTML("<span>※" + name + "(" + socket.getInetAddress() + ")이 퇴장하였습니다.</span>", "black");
						
						clientCount--;
						clientLabel.setText("클라이언트 수 : " + Integer.toString(clientCount));
						database.changeLoginState(name, "false");
						//System.out.println("1");
					}
					try {
						socket.close();
					} catch (IOException e1) {
						DebugMsg.handleError(e1.getMessage());
					}
					//System.out.println("2");

					this.interrupt();
					return;
				}
			}
		}
	}

	public static void main(String[] args) {
		new MyServer();
	}
}

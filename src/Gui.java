import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Observable;


public class Gui extends Observable implements MouseListener, ActionListener,
		KeyListener, WindowListener {

	private boolean isConnectedToServer;
	private JFrame theWindow;
	private JLabel conectionInfo;
	private JTextField inputChat;
	private JTextPane chatLines;
	private JTextPane onlineList;

    private String personName;
    private String inetAddr;
    private int port;

	public Gui(int width, int height, boolean isConnToSrv,String personName, String inetAddr, int port) {
        this.personName = personName;
        this.inetAddr = inetAddr;
        this.port = port;
		//
		// MTU timestamp (3 int) + 10max.char(name) + 256 char (message) + CRC
		// check + byte(check what type of data is sent) MTU

		// MTU byte(check data) + name + rest + crc MTU audio/video

		this.isConnectedToServer = isConnToSrv;

		theWindow = new JFrame("ChatLocal -- " + personName);

		theWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		theWindow.setBounds(30, 30, width, height);

		JPanel conectivity = new JPanel();

		JButton connectDisconect = new JButton();
		connectDisconect.setBounds(15, 15, 200, 250);
		connectDisconect.addActionListener(this);
		theWindow.addWindowListener(this);
		conectionInfo = new JLabel();
		conectionInfo.setEnabled(false);

		chatLines = new JTextPane();


		inputChat = new JTextField(40);
		inputChat.setText("Click and add text to chat");
		inputChat.setDocument(new FixedSizeDocument(Mediator.MAX_MESSAGE_LENGTH));
		inputChat.addMouseListener((MouseListener) this);
		JButton sendChatLine = new JButton();
		sendChatLine.setText("Send");
		sendChatLine.addActionListener((ActionListener) this);
		inputChat.addKeyListener((KeyListener) this);

		if (isConnectedToServer) {
			connectDisconect.setText("Disconnect");
			conectionInfo.setText(personName + " connected to " + inetAddr + " on " + port);
			addStringToChat("System", 0, 0, 0, "No chat!");
		} else {
			connectDisconect.setText("Connect!");
			conectionInfo.setText("Not connected!");
			addStringToChat("System", 0, 0, 0, "You are not connected...");
		}

		conectivity.add(connectDisconect);
		conectivity.add(conectionInfo);

		Dimension sizeForTheChat = new Dimension(650, 150);
		chatLines.setPreferredSize(sizeForTheChat);
		chatLines.setEditable(false);
		JPanel theMessages = new JPanel();
		JScrollPane theScroller = new JScrollPane(chatLines);
		theMessages.add(theScroller);

		JPanel chatInputLine = new JPanel();
		chatInputLine.add(inputChat);
		chatInputLine.add(sendChatLine);

		Dimension sizeForTheOnlineList = new Dimension(200, 150);
		onlineList = new JTextPane();
		onlineList.setPreferredSize(sizeForTheOnlineList);
		onlineList.setEditable(false);

		// <-- Getting the first line look nice for the Online users List
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.black);
		SimpleAttributeSet underlineText = new SimpleAttributeSet();
		StyleConstants.setUnderline(underlineText, true);
		onlineList.setCharacterAttributes(set, true);
		Document doc = onlineList.getStyledDocument();
		try {
			doc.insertString(0, "      ", set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		try {
			doc.insertString(doc.getLength(), "~~| Users Online |~~",
					underlineText);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		// -->

		JPanel peopleOnlinePanel = new JPanel();
		JScrollPane anotherScroll = new JScrollPane(onlineList);
		anotherScroll.setPreferredSize(sizeForTheOnlineList);
		peopleOnlinePanel.add(anotherScroll);

		theWindow.setLayout(new BorderLayout());

		theWindow.add(peopleOnlinePanel, BorderLayout.EAST);
		theWindow.add(theMessages, BorderLayout.CENTER);
		theWindow.add(conectivity, BorderLayout.NORTH);
		theWindow.add(chatInputLine, BorderLayout.SOUTH);

		theWindow.setVisible(true);

	}

	// method to add users to the online list
	public void addUserToList(String user) {
		// Document doc = onlineList.getStyledDocument();

		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.green);
		StyleConstants.setBold(set, true);

		onlineList.setCharacterAttributes(set, true);
		Document doc = onlineList.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), "\n - " + user, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}
	// method to remove users from the online list
	public void removeUserFromList(String user){
		Document doc = onlineList.getStyledDocument();
		try {
			String temp= doc.getText(0, doc.getEndPosition().getOffset()); 
			System.out.println(temp);
			int index= temp.indexOf(user);
			System.out.println("Index : " + index + "length" + user.length());
			doc.remove(index-4, user.length()+4);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}


	public void addStringToChat(String user, int hour, int minute, int second,
			String toAdd) { // this method is public because we gonna use this
							// in main to add chat lines!
		if (user.equals("System")) {
			SimpleAttributeSet set = new SimpleAttributeSet();
			StyleConstants.setForeground(set, Color.red);
			StyleConstants.setBold(set, true);

			Document doc = new DefaultStyledDocument();

			try {

				doc.insertString(0, user + ": " + toAdd, set);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			chatLines.setStyledDocument((StyledDocument) doc);
		} else if (isConnectedToServer) {
			SimpleAttributeSet set = new SimpleAttributeSet();
			if (user.equals("me")) {
				StyleConstants.setBold(set, true);
				// Set the attributes before adding text
				chatLines.setCharacterAttributes(set, true);
			} else {
				if (StyleConstants.isBold(set))
					StyleConstants.setBold(set, false);

				StyleConstants.setItalic(set, true);

				// Set the attributes before adding text
				chatLines.setCharacterAttributes(set, true);
			}
			Document doc = chatLines.getStyledDocument();
			
			try {
				doc.insertString(0, user + "[" + hour + ":" + minute + ":"
						+ second + "]: " + toAdd + "\n", set);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void sendChatText() {
		if (inputChat.getText().equals(""))
			return;
		if (isConnectedToServer) {
			setChanged();
			notifyObservers(inputChat.getText());

			Calendar calendar = new GregorianCalendar();
			addStringToChat("me", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), inputChat.getText());
			inputChat.setText("");
			// System.out.println(calendar.get(Calendar.AM_PM));
		} else {
			addStringToChat("System", 0, 0, 0, "Sorry you are not connected");
			inputChat.setText("!! Sorry you are not connected !!");
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		JButton theBut = (JButton)e.getSource();
		if(e.getActionCommand().equals("Connect!") ){
			isConnectedToServer = true;
			theBut.setText("Disconnect");
			conectionInfo.setText(personName + " connected to " + inetAddr + " on " + port);
			addStringToChat("System",0,0,0, "No chat!");
			setChanged();
			notifyObservers(true);
		
		}else if(e.getActionCommand().equals("Disconnect")){
			isConnectedToServer = false;
			theBut.setText("Connect!");
			conectionInfo.setText("Not connected!");
			addStringToChat("System",0,0,0, "You are not connected...");
			setChanged();
			notifyObservers(false);
		}else if(e.getActionCommand().equals("Send")){
			sendChatText();
		}
		
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		inputChat.setText("");		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == 10) { // if u press enter
			sendChatText();
		}
		
	}
	public void windowClosed(WindowEvent arg0) {
		setChanged();
		notifyObservers(arg0);
		System.out.println("Closing Window");
		
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosing(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
}

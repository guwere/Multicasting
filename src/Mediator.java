import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;

public class Mediator implements Observer{
	public static final int MAX_TIMEOUTS=35; // more than enough. when expires the sender stops trying to send message;
	public static final int ACK=0;
	public static final int TEXT=1;
	public static final int AUDIO=2;
	public static final int DISCONNECT=3;
	public static final int USER_NAME_LENGTH=20;
	public static final int MAX_MESSAGE_LENGTH=512;
	public static final int SEQ_NUM_INDEX=555;
	public static final int TIMEOUT=50;
	public static final int MTU=1024;//data_type[0] + name[1-20] + message[22-533] + seq_num[555]
	private byte last_seq_num=-1;//abitrary as long as its not 0 or 1
	private boolean resend;
	private String name="default" + (System.currentTimeMillis()%999999);//default<random number> name if setName was not called
	private byte [] packet;
	private Vector<String> members;//members connected; deleting members not yet implemented.should not include itself
	private Vector<String> ack_buffer;//because its only stop&wait we only need to store names and cross them off when ack with that particular name arrives;
	private Gui ui;
	private Sender sender;
	private javax.swing.Timer timer;
	private GregorianCalendar calendar;
	private int conseq_timeouts=0;
	
	public class Timeout implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(conseq_timeouts < MAX_TIMEOUTS){
				sender.wakeUp();
				timer.start();
			conseq_timeouts++;
			}
		}
	}
	
	public Mediator(Gui ui){
		this.ui=ui;
		members=new Vector<String>();
		ack_buffer=new Vector<String>();
		timer = new javax.swing.Timer(TIMEOUT,new Timeout());
		calendar= new GregorianCalendar(); 
	}
	
	public void handleReceivedPacket(DatagramPacket packet){
		byte [] data=packet.getData();
		String temp_name=extractName(data);

		if(temp_name.equals(name)){ 
			return;//if packet with the name of the host arrived -> dont do anything
		}
		
		String message;
		if(!members.contains(temp_name)){ // first time connected
			members.add(temp_name);
			ui.addUserToList(temp_name);
			System.out.println(printMembers(members));
		}
		
		if(data[0]==TEXT){
			System.out.println("Received from : " + temp_name);
			if(data[SEQ_NUM_INDEX]!=last_seq_num){				
				last_seq_num=data[SEQ_NUM_INDEX];
				message=extractText(data);
				System.out.println("Received new Text : " + message);
				ui.addStringToChat(temp_name,calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), message);	
			}else{ 
				ui.addStringToChat(temp_name,calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), "sent us a duplicate!!!");//debugging	
			}
			prepareNextAck();//send back an ack always
			sender.wakeUp();
		}
		
		if(data[0]==ACK){
			System.out.println("ACK received!");
			ack_buffer.remove(temp_name);//cross off that ack
			if(ack_buffer.isEmpty()){ 
				conseq_timeouts=0;//reset timeout counter
				timer.stop();
			}
		}
		
		if(data[0]==AUDIO){
			//logic goes here;
		}
		if(data[0]==DISCONNECT){
			System.out.println("DISCONNECT user : " + temp_name);
			members.remove(temp_name);
			ack_buffer.remove(temp_name);
			ui.removeUserFromList(temp_name);
		}
		//sender.wakeUp();
	}
	
	private void prepareDisconnectPacket(){
		packet = new byte[MTU];
		packet[0]=DISCONNECT;
		fill(packet,1,name);
		System.out.println("Name for disconnect " + extractName(packet));
		for(int i=0; i < members.size();i++){
			System.out.println("members: " + members.get(i));
			ui.removeUserFromList(members.get(i));
		}
		members.removeAllElements();
		ack_buffer.removeAllElements();
		
//		ui.removeAllFromList();
	}
	private void prepareNextTextMessage(String message){
		//if(isResending())return;
		packet= new byte[MTU];
		last_seq_num++;
		last_seq_num%=2; // alternate between 1 and 0
		packet[0]=TEXT;
		fill(packet,1,name);
					System.out.println("Name " + extractName(packet));
		fill(packet,USER_NAME_LENGTH+2,message);
					System.out.println("Message " + extractText(packet));
		packet[SEQ_NUM_INDEX]=last_seq_num;
	}
	
	private void prepareNextAudio(){
		//logic goes here;
	}
	
	private void prepareNextAck(){
		packet= new byte[MTU];
		packet[0]=ACK;
		fill(packet,1,name);
		System.out.println("made new acknow\n");
	}
	

	public void update(Observable obs, Object o) {
		if(o instanceof String){
			prepareNextTextMessage(((String)o));//looks weird 
				ack_buffer.addAll(members.subList(0, members.size()));//add expected acknowledgements from all the users of members.
				timer.start();
			sender.wakeUp();
		}
		if(o instanceof WindowEvent){
			prepareDisconnectPacket();
			sender.wakeUp();
			try {
				synchronized(this){
				wait(1);
				System.exit(1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		if(o instanceof Boolean){
			if(!((Boolean)o)){
				System.out.println("Leaving group...");
				prepareDisconnectPacket();
				sender.wakeUp();
				try {
					sender.getSocket().leaveGroup(sender.getGroup());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(((Boolean)o)){
				try {
					last_seq_num=-1;
					System.out.println("Joining group...");
					sender.getSocket().joinGroup(sender.getGroup());
					
				} catch (IOException e) {
					System.out.println("Could not connect ");
					e.printStackTrace();
				}
			}
		
		}
	}
		
	public byte [] getPreparedPacket(){
		return packet;
		
	}
	
	public boolean isResending(){
		return resend;
	}
	
	public void setResend(boolean v){
		resend=v;
	}
	
	public void setName(String name){
		this.name=name;
	}
	public void addSender(Sender sender){
		this.sender=sender;
	}
	public Sender getMySender(){
		return sender;
	}

	public Gui getGui(){
		return ui;
	}
	public String getName(){
		return name;
	}
	//puts a sub-array into a bigger array
	public static void fill(byte [] dest, int fromIndex, String str){//max char to copy
		byte [] temp=str.getBytes();
		int length = temp.length;
		for(int i=0;i < length  ;i++){
			dest[i+fromIndex]=temp[i];
		}
	}
	
	public static String extractName(byte [] src){
		int last_letter=2;
		while(src[last_letter]!=0)last_letter++; // there should be at least one null character between the name and the message
		//System.out.println("THE LAST LETTER : "+last_letter);
		byte [] temp=Arrays.copyOfRange(src, 1, last_letter);
		return new String(temp);
	}
	
	public static String extractText(byte [] src){
		int last_letter = 22;
		while(src[last_letter]!=0) last_letter++;
		byte [] temp=Arrays.copyOfRange(src,22, last_letter);
		return new String(temp);

	}
	
	public static<T> String printMembers( Vector<T> users){
		String s="";
		for(int i=0;i < users.size();i++){
			s += users.get(i).toString() + " ";
		}
		s+="\n";
		return s;
	}
}

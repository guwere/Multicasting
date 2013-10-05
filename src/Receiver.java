import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;


public class Receiver implements Runnable {
	private MulticastSocket socket;
	private Mediator mediator;
	private int noise=0;
	public Receiver(MulticastSocket socket,Mediator med)throws  UnknownHostException{
		socket.getInetAddress();
		socket.getInetAddress();
		this.socket=socket;
		mediator=med;
	}
	public void setNoise(int n){
		noise=n;
	}
	public void run(){
		while(true){
			byte[] buf = new byte[Mediator.MTU];
			 DatagramPacket recv = new DatagramPacket(buf, buf.length);
			 try {
				socket.receive(recv);
				if ((Math.random()*100) > noise || recv.getData()[0] == Mediator.DISCONNECT) {
					mediator.handleReceivedPacket(recv);
				}else{///this is just for testing
					mediator.getGui().addStringToChat(" ", 0, 0, 0, "this line dropped the ack/message");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

}

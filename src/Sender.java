import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Sender implements Runnable {
	private MulticastSocket socket;
	private InetAddress group;
	Mediator mediator;
    private int port;

	public Sender(MulticastSocket socket, InetAddress group,Mediator med,int port){
		this.socket=socket;
		this.group=group;
		mediator=med;
        this.port = port;
	}
	
	public void run(){
		byte [] message; 
		while(true){
			rest();
			message= mediator.getPreparedPacket();
			DatagramPacket packet = new DatagramPacket(message,message.length,
                    group, port);

			try {
				socket.send(packet);
				System.out.println("Packet sent");
				if(packet.getData()[0]==Mediator.DISCONNECT){
					rest();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}	
			
		}
	}
	public MulticastSocket getSocket(){
		return socket;
	}
	public InetAddress getGroup(){
		return group;
	}
	public synchronized void wakeUp(){
		notify();
	}
	public synchronized void rest(){
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}

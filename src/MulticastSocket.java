import java.io.IOException;
import java.net.DatagramPacket;
public class MulticastSocket extends java.net.MulticastSocket {
	private int noise=0;
	public MulticastSocket(int port) throws IOException{
		super(port);
	}
	/**
	 * GLOBAL NOISE . DO NOT SET FROM HERE . INSTEAD USE instanceofreceiver.setNoise(int n);
	 */
	public void setNoise(int n){
		noise=n;
	}
	public void send(DatagramPacket arg0) throws IOException {
		if ((Math.random()*100) > noise) {  // the packet will be send depending on a random number between 0 and 100
			super.send(arg0);
		}
		else {
			 System.out.println("** Packet dropped");
		}
	}

}

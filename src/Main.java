import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) {
        initialDialog();
    }
    public static void initialDialog(){
        String strTest = "hello";
        final String [] labels = {"Your name:","Inet Address(IP):","Port:" };
        int numPairs = labels.length ;

        //Create and populate the panel. Save the references to text fields as they will be accessed later
        JPanel p = new JPanel(new SpringLayout());
        final List<JTextField> list = new ArrayList<JTextField>();
        for (int i = 0; i < numPairs; i++) {
            JLabel l = new JLabel(labels[i], JLabel.TRAILING);
            p.add(l);
            JTextField textField = new JTextField(20);
            l.setLabelFor(textField);
            p.add(textField);
            list.add(textField);
        }
        list.get(1).setText("224.24.24.24");
        list.get(2).setText("6666");
        JButton detailsButton = new JButton("Confirm");
        JLabel buttonLabel = new JLabel("Ready?", JLabel.TRAILING);
        buttonLabel.setLabelFor(detailsButton);
        p.add(buttonLabel);
        p.add(detailsButton);


        //Lay out the panel.
        SpringUtilities.makeCompactGrid(p,
                numPairs + 1, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

        //Create and set up the window.
        final JFrame frame = new JFrame("Details Form");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        p.setOpaque(true);  //content panes must be opaque
        frame.setContentPane(p);

        //Display the window.
        frame.pack();
        frame.setVisible(true);

        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String name = list.get(0).getText();
                String inetAddress = list.get(1).getText();
                int port = Integer.valueOf(list.get(2).getText());

                System.out.println("details: " + name + "  " + inetAddress + "  " + port);
                startProgram(name, inetAddress, port);
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }
    private static void startProgram(String name, String inetAddress, int port){
        InetAddress group = null;
        try {
            group = InetAddress.getByName(inetAddress);
            MulticastSocket s = new MulticastSocket(port);
            s.joinGroup(group);

            Gui ui= new Gui(900,400,true,name,inetAddress,port);
            //ui.setConnectionInfoLabel("Address: " + group.getHostAddress() + " on Port: "  + s.getLocalPort());
            Mediator mediator= new Mediator(ui);
            mediator.setName(name);

            Sender sender= new Sender(s,group,mediator,port);
            Receiver receiver= new Receiver(s,mediator);
            ui.addObserver(mediator);
            receiver.setNoise(0);

            mediator.addSender(sender);
            Thread ts= new Thread(sender);
            Thread tr= new Thread(receiver);
            ts.start();
            tr.start();
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(null,"not a valid inet address. Maybe try above 224.0.0.0","address error",JOptionPane.ERROR_MESSAGE);
            initialDialog();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

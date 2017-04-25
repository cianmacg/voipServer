package voipServer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/*
 * This class simply creates a GUI for the server
 */

public class ServerGui extends JFrame{
	
	private Server myServer = new Server();
	
	
	public ServerGui(){
		initUI();
	}
	
	
	public void initUI(){
		final JPanel panel = new JPanel();
		final JButton serverStart = new JButton("Start");
		final JButton serverStop = new JButton("Stop");
		final JLabel portLabel = new JLabel(" Port:");
		final JTextField getPort = new JTextField("8090");
		
		getPort.setPreferredSize( new Dimension( 160, 24 ) );
		
		serverStart.setEnabled(true);
		serverStop.setEnabled(false);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				myServer.stopServer();
				System.exit(0);
			}
		});
		
		serverStart.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						serverStart.setEnabled(false);
						serverStop.setEnabled(true);
						myServer.startServer(Integer.parseInt(getPort.getText()));
					}	
				});
		
		serverStop.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						serverStart.setEnabled(true);
						serverStop.setEnabled(false);
						myServer.stopServer();
					}	
				});
		panel.add(serverStart);
		panel.add(serverStop);
		panel.add(portLabel);
		panel.add(getPort);
		
		panel.setLayout(new FlowLayout());
		add(panel);
        setTitle("Voip Server");
        setSize(400, 70);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setVisible(true);

	}
	
	public static void main(String[] args){
		try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
		
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ServerGui ex = new ServerGui();
                ex.setVisible(true);
            }
		});
	}
}

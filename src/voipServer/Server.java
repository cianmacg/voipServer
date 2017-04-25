package voipServer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * This class will deal with any packets received i.e. adding users to the server list, removing users from the server list, distributing received voice packets, etc.
 */

public class Server {

	//What we do with a packet is determined by its first byte
	private final byte voiceByte = Byte.parseByte("00000001", 2);
	private final byte newConn = Byte.parseByte("00000010", 2);
	private final byte removeUser = Byte.parseByte("00000011", 2);
	private final byte connSuccess = Byte.parseByte("00000100", 2);
	private final byte connEnd = Byte.parseByte("00000101", 2);
	
	DatagramSocket serverSocket = null;
	
	private Thread receiverThread = null;
	private final int defaultClientPort = 8150;
	private int myPort;
	private boolean serverStatus = false;
	
	private CopyOnWriteArrayList<Client> clients = new CopyOnWriteArrayList<Client>();
	public Server(){
		
	}
	
	public void startServer(int port){

		System.out.println("Server started and listening on port " + port);
		//clients.add(new Client("86.45.47.50", "FakeUser"));
		this.myPort = port;

		
		try {
			serverSocket = new DatagramSocket(myPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		receiverThread = new Thread(new Receive());
		serverStatus = true;
		receiverThread.start();
	}
	
	//Alerts all clients that the server is stopping and closes any connections
	public void stopServer(){
		byte[] end = new byte[1];
		end[0] = connEnd;
		for(Client c : clients){
			try {
				serverSocket.send(new DatagramPacket(end, end.length, InetAddress.getByName(c.getIp()), defaultClientPort));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(serverStatus){
			serverSocket.close();
		}
		serverStatus = false;
		clients = new CopyOnWriteArrayList<Client>();
	}
	
	//This class will receive packets from clients and deal with them accordingly
	class Receive extends Thread{
			public void run(){
				try{

					byte[] b = new byte[10000];
					DatagramPacket receivedPacket = new DatagramPacket(b, b.length);
					
					while (serverStatus){
						serverSocket.receive(receivedPacket);
						if(receivedPacket.getData()[0] == voiceByte)		send(receivedPacket);
						
						else if(receivedPacket.getData()[0] == newConn) 		newConnection(receivedPacket);
						
						else if(receivedPacket.getData()[0] == removeUser)	removeUser(receivedPacket);
						
						else												System.out.println("Unknown Packet...");
					}
					
				} catch(IOException e) {
					if(serverSocket.isClosed())	System.out.println("Server Closed.");
					else e.printStackTrace();
				}
			}
		
	}

	//performed when a packet is of the type newConn (checks if the client already exists, if not adds to the list and confirms successful connection)
	private void newConnection(DatagramPacket connection){
		String address = connection.getAddress().getHostName();
		byte[] name = Arrays.copyOfRange(connection.getData(), 1, connection.getData().length-1);
		if(!(clients.contains(new Client(connection.getAddress().getHostName(), new String(name))))){
			
			byte[] confirm = new byte[1];
			confirm[0] = connSuccess;
			try {
				serverSocket.send(new DatagramPacket(confirm, confirm.length, InetAddress.getByName(address), defaultClientPort));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			DatagramPacket copy = connection;
			for(Client c : clients){
				try {
					copy.setAddress(InetAddress.getByName(c.getIp()));
					copy.setPort(defaultClientPort);
					serverSocket.send(copy);
					
					byte[] cName = new byte[c.getName().getBytes().length+1];
					cName[0] = newConn;
					for(int i = 0; i<cName.length-1; i++){
						cName[i+1] = c.getName().getBytes()[i];
					}
					
					serverSocket.send(new DatagramPacket(cName, cName.length, InetAddress.getByName(address), defaultClientPort));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			clients.add(new Client(address, new String(name)));
			System.out.println("User Connected: " + address + "/" + new String(name));
		}
	}
	
	//removes a client from out list and informs them that they have been disconnected successfully (tells all other clients that this client has been removed)
	private void removeUser(DatagramPacket disconnection){
		byte[] removedUser = new byte[101];
		removedUser[0] = removeUser;
		for(Client c : clients){
			if((c.getIp().equals(disconnection.getAddress().getHostAddress()))||(c.getIp().equals(disconnection.getAddress().getHostName()))){
				System.out.println("User Disconnected: "+c.getIp() + "/" + c.getName());
				int x = removedUser.length;
				if(c.getName().getBytes().length<x) x=c.getName().getBytes().length;
				for(int i = 1; i<x-1; i++){
					removedUser[i] = c.getName().getBytes()[i+1];
				}
				clients.remove(c);
			}
		}
		
		for(Client c : clients){
			try {
				serverSocket.send(new DatagramPacket(removedUser, removedUser.length, InetAddress.getByName(c.getIp()), defaultClientPort));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//sends voice data to all clients except for the source client
	private void send(DatagramPacket sendPacket){
		try {
			
			DatagramPacket initialPacket = sendPacket;
			for(Client c : clients){
				//if(!c.getIp().equals(initialPacket.getAddress().getHostAddress())){
					DatagramSocket serverSocket = new DatagramSocket();
					sendPacket.setAddress(InetAddress.getByName(c.getIp()));
					sendPacket.setPort(defaultClientPort);
					serverSocket.send(sendPacket);
				//}
			}
	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

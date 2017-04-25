package voipServer;

public class Client {
	
	private String ip = null;
	private String name = null;
	
	public Client(){}
	
	public Client(String ip, String name){
		this.ip = ip;
		this.name = name;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getIp(){
		return this.ip;
	}
	
	public String getName(){
		return this.name;
	}
}

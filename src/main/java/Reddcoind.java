import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.GsonBuilder;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import database.implementation.SQLLiteDatabase;
import webbit_lite.WebServers;
import webbit_lite.netty.NettyWebServer;

public class Reddcoind{
	private final JsonRpcHttpClient _CLIENT;

	
	public Reddcoind(String rpc_url,String rpc_user,String rpc_password) throws Exception{

		
		// Init RPC client
		Authenticator.setDefault(new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(rpc_user,rpc_password.toCharArray());
			}
		});
		_CLIENT=new JsonRpcHttpClient(new URL("http://xmpp.frk.wf:45443/"));


	}
	
	public long getReceivedByAddress(String addr) throws Throwable {
		double v=(double)_CLIENT.invoke("getreceivedbyaddress",new Object[]{addr},Object.class);
		return (long)(v*100000000L);
	}
	
	public void sendToAddress(String addr, long ammount_long) throws Throwable {
		double ammount = (ammount_long)/100000000.0;
		double balance=(double)_CLIENT.invoke("getbalance",new Object[]{},Object.class);
		if (balance>ammount)_CLIENT.invoke("sendtoaddress",new Object[]{addr,ammount},Object.class);
		else throw new Exception("Not enough coins");
	}

	public String getNewAddress() throws Throwable {
		String addr=(String)_CLIENT.invoke("getnewaddress",new Object[]{},Object.class);
		return addr;
	}
	
}

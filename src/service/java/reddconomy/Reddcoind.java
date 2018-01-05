package reddconomy;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;


public class Reddcoind implements CentralWallet{
	private final JsonRpcHttpClient _CLIENT;

	
	public Reddcoind(String rpc_url,String rpc_user,String rpc_password) throws Exception{

		// Init RPC client
		Map<String,String> headers=new HashMap<String,String>();
		String userpw=rpc_user+":"+rpc_password;
		userpw=Base64.getEncoder().encodeToString(userpw.getBytes("UTF-8"));
		
		headers.put("Authorization","Basic "+userpw);
		
		_CLIENT=new JsonRpcHttpClient(new URL(rpc_url));
		_CLIENT.setHeaders(headers);


	}
	
	public synchronized long getReceivedByAddress(String addr) throws Throwable {
		double v=(double)_CLIENT.invoke("getreceivedbyaddress",new Object[]{addr},Object.class);
		return (long)(v*100000000L);
	}
	
	public synchronized void sendToAddress(String addr, long ammount_long) throws Throwable {
		double ammount = (ammount_long)/100000000.0;
		double balance=(double)_CLIENT.invoke("getbalance",new Object[]{},Object.class);
		if (balance>ammount)_CLIENT.invoke("sendtoaddress",new Object[]{addr,ammount},Object.class);
		else throw new Exception("Not enough coins");
	}

	public synchronized String getNewAddress() throws Throwable {
		String addr=(String)_CLIENT.invoke("getnewaddress",new Object[]{},Object.class);
		return addr;
	}
	
}

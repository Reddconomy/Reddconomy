
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

public class JRPC_Test{
	public static void main(String[] args) throws Throwable {
		final String rpcuser="test";
		final String rpcpassword="test123";

		Authenticator.setDefault(new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(rpcuser,rpcpassword.toCharArray());
			}
		});

		JsonRpcHttpClient client=new JsonRpcHttpClient(new URL("http://xmpp.frk.wf:45443/"));
//		String addr=(String)client.invoke("getnewaddress",new Object[]{},Object.class);
//		System.out.println(addr);

	// Other commands
//		Double v=(Double)client.invoke("getreceivedbyaddress",new Object[]{addr},Object.class);
//
//		System.out.println(v);
//
		Double balance=(Double)client.invoke("getbalance",new Object[]{},Object.class);
		System.out.println(balance);
//
//		Double ammount=10.;
//		client.invoke("sendtoaddress",new Object[]{addr,ammount},Object.class);
//		Double v=(Double)client.invoke("getreceivedbyaddress",new Object[]{addr},Object.class);

//		System.out.println(v);
		}

	

	public static String addr() throws Throwable {
		final String rpcuser="test";
		final String rpcpassword="test123";

		Authenticator.setDefault(new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(rpcuser,rpcpassword.toCharArray());
			}
		});
		JsonRpcHttpClient client=new JsonRpcHttpClient(new URL("http://xmpp.frk.wf:45443/"));
		String addr = (String) client.invoke("getnewaddress",new Object[]{},Object.class);
		return addr;
	}
}

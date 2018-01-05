import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

public class JRPC_SendCoins{
	public static void main(String[] args) throws Throwable {
		final String rpcuser="test";
		final String rpcpassword="test123";

		Authenticator.setDefault(new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(rpcuser,rpcpassword.toCharArray());
			}
		});

		JsonRpcHttpClient client=new JsonRpcHttpClient(new URL("http://reddconomy.frk.wf:45443/"));
	
		client.invoke("sendtoaddress",new Object[]{"nn6GTxBU1fUrWkfXHYNNLYDkXmHMtSUjiZ",100},Object.class);
			System.out.println("Done");
	}
}

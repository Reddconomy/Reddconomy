import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import database.Database;
import database.implementation.SQLLiteDatabase;
import webbit_lite.HttpControl;
import webbit_lite.HttpHandler;
import webbit_lite.HttpRequest;
import webbit_lite.HttpResponse;
import webbit_lite.WebServers;
import webbit_lite.netty.NettyWebServer;

public class Reddconomy implements HttpHandler{

	final Database _DATABASE;
	final Gson _JSON;

	public static void main(String[] args) throws Exception {

		int port=8099;
		String ip="0.0.0.0";
		ExecutorService thread_pool=Executors.newFixedThreadPool(1);
		NettyWebServer ws=(NettyWebServer)WebServers.createWebServer(thread_pool,new InetSocketAddress(InetAddress.getByName(ip),port));
		ws.add(new Reddconomy());
		ws.staleConnectionTimeout(10000);

		ws.start();
		System.out.println("Server started @ "+ip+":"+port);

	}


	final String rpcuser="test";
	final String rpcpassword="test123";
	public JsonRpcHttpClient client=new JsonRpcHttpClient(new URL("http://xmpp.frk.wf:45443/"));
	
	public Reddconomy() throws Exception{
		_DATABASE=new SQLLiteDatabase("db.sqlite");
		_DATABASE.open();
		_JSON=new GsonBuilder().setPrettyPrinting().create();
		
		Thread t=new Thread(){
			public void run(){
				try {
					loop();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.start();
		
		Authenticator.setDefault(new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(rpcuser,rpcpassword.toCharArray());
			}
		});
	}
	
	public void loop() throws Throwable{
		long t=0;
		while(true){
			try{
				long delta_t=t==0?0:System.currentTimeMillis()-t;
				Collection<Map<String,Object>> deposits=_DATABASE.getIncompletedDepositsAndUpdate(delta_t);
				for(Map<String,Object> deposit:deposits){
					String addr = deposit.get("addr").toString();
					long balance = (long)deposit.get("balance");
					if (getReceivedBA(addr) >= balance)
					{
						_DATABASE.completeDeposit(addr);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
 			t=System.currentTimeMillis();
			try{
				Thread.sleep(10000);
			}catch(InterruptedException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public long getReceivedBA(String addr) throws Throwable {
		Double v=(Double)client.invoke("getreceivedbyaddress",new Object[]{addr},Object.class);
		long v_long=(long)(v*100000000L); 
		return v_long;
	}
	
	public String getAddr() throws Throwable {
		JsonRpcHttpClient client=new JsonRpcHttpClient(new URL("http://xmpp.frk.wf:45443/"));
		String addr = (String) client.invoke("getnewaddress",new Object[]{},Object.class);
		return addr;
	}
	
	@Override
	public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
		try{

			// Parsa req get
			// wwww?a=1&b=2&c=3
			// diventa
			// { 
			//		a:1,
			//		b:2,
			//		c:3
			// }
			//
			final Map<String,Object> _GET=new HashMap<String,Object>();
			{
				String uri=request.uri();
				String uri_p[]=uri.split("\\?");
				if(uri_p.length>1){
					String params[]=uri_p[1].split("&");
					for(String param:params){
						if(param.contains("=")){
							String pp[]=param.split("=");
							_GET.put(pp[0],pp[1]);
						}else{
							_GET.put(param,true);
						}
					}
				}
			}

			System.out.println("Request: "+request.uri());
			System.out.println("Get params: "+_GET);

			String action=(String)_GET.get("action");
			if(action!=null){
				switch(action){
					case "test":{
						// Esempio
						Map<String,Object> resp_obj=new HashMap<String,Object>();
						try{

							Map<String,Object> obj=_DATABASE.getWallet("test__fake_walletid");
							resp_obj.put("status",200); // Aggiungo lo status della risposta 200=ok, qualsiasi altro numero = fallita
							resp_obj.put("data",obj); // Aggiungo i dati della risposta 
						}catch(Exception e){
							String error=e.toString();
							resp_obj.put("status",500);
							resp_obj.put("error",error);
							e.printStackTrace();
						}

						// Converto risposta in json
						String resp_json=_JSON.toJson(resp_obj);

						response.status(200);
						response.header("Content-type","application/json");
						response.content(resp_json);
						response.end();
						break;
					}
					case "deposit":{
						Map<String,Object> resp_obj=new HashMap<String,Object>();
						try {
							Map<String,Object> data = new HashMap<String,Object>();
							String addr = getAddr();
							data.put("addr", addr);
							resp_obj.put("status",200); // Aggiungo lo status della risposta 200=ok, qualsiasi altro numero = fallita
							resp_obj.put("data",data); // Aggiungo i dati della risposta 
							String wallet_id=_GET.get("wallid").toString();
							long balance=Long.parseLong(_GET.get("balance").toString());
							_DATABASE.prepareForDeposit(addr, wallet_id, balance);
						} catch (Throwable e) {
							String error=e.toString();
							resp_obj.put("status",500);
							resp_obj.put("error",error);
							e.printStackTrace();
						}
						
						
						
						// Converto risposta in json
						String resp_json=_JSON.toJson(resp_obj);

						response.status(200);
						response.header("Content-type","application/json");
						response.content(resp_json);
						response.end();
						break;
					}
					default:{
						response.status(401);
						response.header("Content-type","application/json");
						response.content("{'status':401,'error':'Invalid action'}");
						response.end();
					}
				}

			}else{
				response.status(401);
				response.header("Content-type","application/json");
				response.content("{'status':401,'error':'Unspecified action'}");
				response.end();
			}

		}catch(Exception e){
			response.status(401);
			response.header("Content-type","application/json");
			response.content("{'status':500,'error':'Invalid request'}");
			response.end();
			e.printStackTrace();
		}
	}
}

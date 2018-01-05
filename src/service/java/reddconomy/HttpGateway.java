package reddconomy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import reddconomy.utils.HashUtils;
import webbit_lite.HttpControl;
import webbit_lite.HttpHandler;
import webbit_lite.HttpRequest;
import webbit_lite.HttpResponse;
import webbit_lite.WebServers;
import webbit_lite.netty.NettyWebServer; 

public class HttpGateway implements HttpHandler{

	private final NettyWebServer _WS;
	private final ActionListener _LISTENER;
	private final String _SECRET;

	public HttpGateway(String secret,String bind_ip,int bind_port,ActionListener listener) throws Exception{
		_LISTENER=listener;
		_SECRET=secret;

		// Init webservice
		ExecutorService thread_pool=Executors.newFixedThreadPool(1);
		_WS=(NettyWebServer)WebServers.createWebServer(thread_pool,new InetSocketAddress(InetAddress.getByName(bind_ip),bind_port));
		_WS.add(this);
		_WS.staleConnectionTimeout(10000);

	}

	public void start(){
		_WS.start();
	}
	
	public void stop() {
		_WS.stop();
	}

	@Override
	public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
		try{
			
			String uri=request.uri();
			String hash=HashUtils.hmac(_SECRET,uri);
			String sent_hash=request.header("Hash");
			if(hash.equals(sent_hash)){
	
				Map<String,String> _GET=new HashMap<String,String>();
				{
					String uri_p[]=uri.split("\\?");
					if(uri_p.length>1){
						String params[]=uri_p[1].split("&");
						for(String param:params){
							if(param.contains("=")){
								String pp[]=param.split("=");
								_GET.put(pp[0],pp[1]);
							}else{
								_GET.put(param,"true");
							}
						}
					}
				}
	
				System.out.println("Request: "+request.uri());
				System.out.println("Get params: "+_GET);
	
				String action=(String)_GET.get("action");
				String resp=_LISTENER.performAction(action,_GET);
				response.status(200);
				response.header("Content-type","application/json");
				response.content(resp);
				response.end();
			}else{
				System.err.println("Unauthorized");
				response.status(401);
				response.header("Content-type","text/plain");
				response.content("Unauthorized");
				response.end();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

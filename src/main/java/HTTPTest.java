

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import webbit_lite.HttpControl;
import webbit_lite.HttpHandler;
import webbit_lite.HttpRequest;
import webbit_lite.HttpResponse;
import webbit_lite.WebServers;
import webbit_lite.netty.NettyWebServer;

public class HTTPTest implements HttpHandler{
	public static void main(String[] args) throws UnknownHostException, InterruptedException, ExecutionException {
		
			int port=8099;
			String ip="0.0.0.0";
			ExecutorService thread_pool=Executors.newFixedThreadPool(1);
			NettyWebServer ws=(NettyWebServer)WebServers.createWebServer(thread_pool,new InetSocketAddress(InetAddress.getByName(ip),port));
			ws.add(new HTTPTest());
			ws.staleConnectionTimeout(10000);
	
			ws.start();
			System.out.println("Server started @ "+ip+":"+port);
		
	} 

	@Override
	public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
		System.out.println("Request: "+request.uri());
		response.status(200);
		response.header("Content-type","text/plain");
		response.content("Hello world");
		response.end();
	}
}

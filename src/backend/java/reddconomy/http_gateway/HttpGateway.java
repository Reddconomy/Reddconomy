/*
 * Copyright (c) 2018, Riccardo Balbo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package reddconomy.http_gateway;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reddconomy.Utils;
import reddconomy.api.ApiEndpoints;
import reddconomy.api.ApiResponse;
import webbit_lite.HttpControl;
import webbit_lite.HttpHandler;
import webbit_lite.HttpRequest;
import webbit_lite.HttpResponse;
import webbit_lite.WebServers;
import webbit_lite.netty.NettyWebServer; 

/**
 * HTTP Gateway, exposes api via HTTP requests, handles authentication and validity/integrity of the requests
 * @author Riccardo Balbo
 *
 */
public class HttpGateway implements HttpHandler{

	private final NettyWebServer _WS;
	private final Map<String,ApiEndpoints> _LISTENERS=new HashMap<String,ApiEndpoints>();
	private final String _SECRET;
	private final Gson _JSON=new GsonBuilder().setPrettyPrinting().create();

	public HttpGateway(String secret,String bind_ip,int bind_port) throws Exception{
		_SECRET=secret==null?"":secret;

		// Init webservice
		ExecutorService thread_pool=Executors.newFixedThreadPool(1);
		_WS=(NettyWebServer)WebServers.createWebServer(thread_pool,new InetSocketAddress(InetAddress.getByName(bind_ip),bind_port));
		_WS.add(this);
		_WS.staleConnectionTimeout(10000);

	}
	
	public  Map<String,ApiEndpoints> listeners(){
		return _LISTENERS;
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
			
			System.out.println("Use secret "+_SECRET);
			
			// Calculate expected hash of the request
			String expected_hash=_SECRET.isEmpty()?"":Utils.hmac(_SECRET,uri);
			
			// Get sent hash
			String hash=_SECRET.isEmpty()?"":request.header("Hash");			
			
			System.out.println("Calculated hash "+expected_hash);

			
			// If the two hashes are equals: the secret key is valid and the request is intact
			if(expected_hash.equals(hash)){ 
				// Get api version
				String version="v1";
				if(uri.contains("?"))version=uri.substring(0,uri.indexOf("?")-1);
				if(version.startsWith("/"))version=version.substring(1);
				if(version.endsWith("/"))version=version.substring(0,version.length()-1);
				
				System.out.println("Use version "+version);
				
				ApiEndpoints listener=_LISTENERS.get(version);
				if(listener!=null){
					
					// Extract params from uri
					Map<String,String> GET=new HashMap<String,String>();
					{
						String uri_p[]=uri.split("\\?");
						if(uri_p.length>1){
							String params[]=uri_p[1].split("&");
							for(String param:params){
								if(param.contains("=")){
									String pp[]=param.split("=");
									GET.put(pp[0],pp[1]);
								}else{
									GET.put(param,"true");
								}
							}
						}
					}
		
					System.out.println("Request: "+request.uri());
					System.out.println("Get params: "+GET);
		
					String action=GET.getOrDefault("action","help").toLowerCase();
					ApiResponse resp=listener.onRequest(action,GET);
					String json_resp=_JSON.toJson(resp.toMap());
					
					response.status(200);
					response.header("Content-type","application/json");
					response.content(json_resp);
					response.end();
				}else{
					System.err.println("Version: "+version+" not found");
					response.status(404);
					response.header("Content-type","application/json");				
					response.content(_JSON.toJson(ApiResponse.build().error(404,"Unavailable api version")));
					response.end();
				}
			}else{
				System.out.println("Error hash mismatch expectet "+expected_hash+ " sent " +hash);

				System.err.println("Unauthorized");
				response.status(401);
				response.header("Content-type","application/json");				
				response.content(_JSON.toJson(ApiResponse.build().error(401,"Unauthorized")));
				response.end();
			}
		}catch(Exception e){
			e.printStackTrace();
			response.status(500);
			response.header("Content-type","application/json");				
			response.content(_JSON.toJson(ApiResponse.build().error(500,"Unexpected error")));
			response.end();
		}
	}
}

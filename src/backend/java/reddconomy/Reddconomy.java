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
package reddconomy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reddconomy.api.ApiEndpoints;
import reddconomy.api.ApiResponse;
import reddconomy.api.ReddconomyApiEndpointsV1;
import reddconomy.blockchain.BitcoindConnector;
import reddconomy.blockchain.BlockchainConnector;
import reddconomy.http_gateway.HttpGateway;
import reddconomy.offchain_database.Database;
import reddconomy.offchain_database.SQLLiteDatabase;

public class Reddconomy {

	private static HttpGateway HTTPD;
	private static ApiEndpoints RDDE;
	private static BlockchainConnector BLOCKCHAIN;
	private static Database DATABASE;
	private static final Gson _JSON=new GsonBuilder().setPrettyPrinting().create();

	public static void main(String[] args) throws Throwable {
		
		File config_file=new File("reddconomy.json");		
		if(args.length>0)config_file=new File(String.join(" ",args).replace("/",File.separator));
		
		Map<String,Object> config=new HashMap<String,Object>();

		// Load config file if exists
		if(config_file.exists()){
			BufferedReader reader=new BufferedReader(new FileReader(config_file));
			config=_JSON.fromJson(reader,Map.class);
			reader.close();
		}		
		
		// Add missing options
		config.putIfAbsent("bind_ip","127.0.0.1");
		config.putIfAbsent("bind_port",8099);
		config.putIfAbsent("database","sqlite");
		config.putIfAbsent("sqlite-path","db.sqlite");
		config.putIfAbsent("versions","v1");// v1,v2,v3,...
		config.putIfAbsent("secret",UUID.randomUUID().toString().replace("-",(int)(Math.random()*1000)+""));
		config.putIfAbsent("blockchain_connector","bitcoind");
		config.putIfAbsent("bitcoind-rpc_url","http://127.0.0.1:45443");
		config.putIfAbsent("bitcoind-rpc_user","rpcuser");
		config.putIfAbsent("bitcoind-rpc_password","rpcpassword");
		
		// Write updated config file
		BufferedWriter writer=new BufferedWriter(new FileWriter(config_file));
		_JSON.toJson(config,writer);
		writer.close();
				
		// Start local database
		DATABASE=new SQLLiteDatabase(config.get("sqlite-path").toString());
		DATABASE.open();
		// Connect to RPC daemon
		BLOCKCHAIN=new BitcoindConnector(config.get("bitcoind-rpc_url").toString(),config.get("bitcoind-rpc_user").toString(),config.get("bitcoind-rpc_password").toString());
		// Register api responses
		ApiResponse.registerAll("v1");	
		// Start api backend
		RDDE=new ReddconomyApiEndpointsV1(BLOCKCHAIN,DATABASE);
		RDDE.open();
		// Start HTTP gateway
		String ip=config.get("bind_ip").toString();
		int port=((Number)config.get("bind_port")).intValue();
		HTTPD=new HttpGateway(config.get("secret").toString(),ip,port);
		HTTPD.start();
		//Add api endpoints to httpd
		HTTPD.listeners().put("v1",RDDE);// Version 1
		
		System.out.println("Server started @ "+ip+":"+port);
	}
}

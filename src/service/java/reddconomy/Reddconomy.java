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
	
	public static void main(String[] args) throws Throwable {
		int port=8099;
		String ip="0.0.0.0";
		
		// Start local database
		DATABASE=new SQLLiteDatabase("db.sqlite");
		DATABASE.open();
		// Connect to RPC daemon
		BLOCKCHAIN=new BitcoindConnector("http://reddconomy.frk.wf:45443/","test","test123");
		// Register api responses
		ApiResponse.registerAll("v1");	
		// Start api backend
		RDDE=new ReddconomyApiEndpointsV1(BLOCKCHAIN,DATABASE);
		RDDE.open();
		// Start HTTP gateway
		HTTPD=new HttpGateway("SECRET123",ip,port);
		HTTPD.start();
		//Add api endpoints to httpd
		HTTPD.listeners().put("v1",RDDE);// Version 1
		
		System.out.println("Server started @ "+ip+":"+port);
	}
}

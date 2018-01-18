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
package reddconomy.blockchain;

import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

/**
 * Connects to a local or remote bitcoind daemon or fork (reddcoind, dogecoind ...) via RPC 
 * @author Riccardo Balbo
 */
public class BitcoindConnector implements BlockchainConnector{
	private final JsonRpcHttpClient _CLIENT;

	
	public BitcoindConnector(String rpc_url,String rpc_user,String rpc_password) throws Exception{

		// Init RPC client
		Map<String,String> headers=new HashMap<String,String>();
		String userpw=rpc_user+":"+rpc_password;
		userpw=Base64.getEncoder().encodeToString(userpw.getBytes("UTF-8"));
		
		headers.put("Authorization","Basic "+userpw);
		
		_CLIENT=new JsonRpcHttpClient(new URL(rpc_url));
		_CLIENT.setHeaders(headers);


	}

	@Override
	public synchronized long getReceivedByAddress(String addr) throws Throwable {
		double v=(double)_CLIENT.invoke("getreceivedbyaddress",new Object[]{addr},Object.class);
		return (long)(v*100000000L);
	}
	
	@Override
	public synchronized void sendToAddress(String addr, long amount_long) throws Throwable {
		double amount = (amount_long)/100000000.0;
		double balance=(double)_CLIENT.invoke("getbalance",new Object[]{},Object.class);
		if (balance>amount)_CLIENT.invoke("sendtoaddress",new Object[]{addr,amount},Object.class);
		else throw new Exception("Not enough coins");
	}

	@Override
	public synchronized String getNewAddress() throws Throwable {
		String addr=(String)_CLIENT.invoke("getnewaddress",new Object[]{},Object.class);
		return addr;
	}
	
	@Override
	public synchronized boolean isTestnet() throws Throwable{
		Map<String,Object> info=(Map<String,Object>)_CLIENT.invoke("getinfo",new Object[]{},Object.class);
		return (boolean)info.get("testnet");		
	}
}

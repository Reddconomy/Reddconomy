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
package it.reddconomy.blockchain.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import it.reddconomy.Config;
import it.reddconomy.Utils;
import it.reddconomy.blockchain.BlockchainConnector;
import it.reddconomy.common.fees.BlockchainFee;

/**
 * Connects to a local or remote bitcoind daemon or fork (reddcoind, dogecoind ...) via RPC 
 * @author Riccardo Balbo
 */
public class BitcoindConnector implements BlockchainConnector{
	private JsonRpcHttpClient CLIENT;
	private long LAST_CONFIG_UPDATE;	
	private final Config _CONFIG;
	
	public BitcoindConnector(Config config) throws Exception{
		_CONFIG=config;
		connect();
	}
	
	private void connect() throws UnsupportedEncodingException, MalformedURLException{
		long lastupdate=_CONFIG.lastUpdate();
		if(lastupdate==LAST_CONFIG_UPDATE)return;
		
		LAST_CONFIG_UPDATE=lastupdate;
		
		String rpc_url=_CONFIG.get("bitcoind-rpc_url").toString();
		String rpc_user=_CONFIG.get("bitcoind-rpc_user").toString();
		String rpc_password=_CONFIG.get("bitcoind-rpc_password").toString();
		
		// Init RPC client
		Map<String,String> headers=new HashMap<String,String>();
		String userpw=rpc_user+":"+rpc_password;
		userpw=Base64.getEncoder().encodeToString(userpw.getBytes("UTF-8"));
		
		CLIENT=new JsonRpcHttpClient(new URL(rpc_url));

		headers.put("Authorization","Basic "+userpw);
		CLIENT.setHeaders(headers);

	}
	
	private double convertToBitcoindRep(long v){
		return Utils.convertToUserFriendly(v);
	}
	
	private long convertFromBitcoindRep(double v){
		return Utils.convertToInternal(v);
	}

	@Override
	public synchronized long getReceivedByAddress(String addr) throws Throwable {
		connect();
		int minconf=((Number)_CONFIG.get("minconf")).intValue();

		double v=(double)CLIENT.invoke("getreceivedbyaddress",new Object[]{addr,minconf},Object.class);
		return convertFromBitcoindRep(v);
	}
	
	@Override
	public synchronized Object[] createRawTransaction(String to, 
			long amount_long, 
			long feeXkb
			) throws Throwable {		
		connect();
		
		int minconf=((Number)_CONFIG.get("minconf")).intValue();

		
		Collection<String> outputs=new ArrayList<String>();
		outputs.add(to);	
		
		ArrayList<Map<String,Object>>  inputs=new ArrayList<Map<String,Object>>();
		long c_amount=amount_long;
		
		String change_addr=null;
		long unspent_amount=0;
		Collection<Map<String,Object>> unspents=(Collection<Map<String,Object>>)CLIENT.invoke("listunspent",new Object[]{minconf},Object.class);
		for(Map<String,Object> unspent:unspents){
			if(c_amount==0){
				System.out.println("Funds collected");
				break;
			}else if(c_amount<0){
				System.err.println("FATAL! createTransaction c_amount< 0 !?");
				System.exit(1);
			}
			System.out.println("Use unspent "+unspent);

			long a=convertFromBitcoindRep(((Number)unspent.get("amount")).doubleValue());
			
			if(a==0)continue;
			unspent_amount+=a;
			c_amount-=a;
			if(c_amount<0)c_amount=0;
			
			Map<String,Object> i=new HashMap<String,Object>();
			i.put("txid",(String)unspent.get("txid"));
			i.put("vout",((Number)unspent.get("vout")).intValue());
			change_addr=(String)unspent.get("address");
			inputs.add(i);
		}
		System.out.println(amount_long);
		System.out.println(c_amount);
		System.out.println(change_addr);
		System.out.println(unspent_amount);
		
		if (c_amount>0||change_addr==null) throw new Exception("Not enough coins");
		
		
		// Create a fake transaction to calculate the fee
		Map<String,Object> tr=new LinkedHashMap<String,Object>();
		tr.put(to,convertToBitcoindRep(amount_long));
		long change=unspent_amount-amount_long;
		tr.put(change_addr,convertToBitcoindRep(change));
		System.out.println(tr);
		String rawtr=(String)CLIENT.invoke("createrawtransaction",new Object[]{inputs,tr},Object.class);
		Map<String,Object> signed_rawtr=(Map<String,Object>)CLIENT.invoke("signrawtransaction",new Object[]{rawtr},Object.class);
		if(!(signed_rawtr.get("complete") instanceof Boolean||!(boolean)signed_rawtr.get("complete")) )throw new Exception("Can't sign transaction");
		rawtr=(String)signed_rawtr.get("hex");
		
		
		int kb=(int)Math.ceil(((double)rawtr.length()/2.)/1024.);
		System.out.println("Transaction size: "+kb+" kb");
		
		long paid_in_fees=feeXkb*kb;
		double dpaid_in_fees=convertToBitcoindRep(paid_in_fees);
		System.out.println("Fee "+dpaid_in_fees);
		
		// Create real transaction
		long net=amount_long-paid_in_fees;
		tr.put(to,convertToBitcoindRep(net));
//		tr.put(change_addr,convertToBitcoindRep(change));
		
		System.out.println(tr);
		
		rawtr=(String)CLIENT.invoke("createrawtransaction",new Object[]{inputs,tr},Object.class);
		signed_rawtr=(Map<String,Object>)CLIENT.invoke("signrawtransaction",new Object[]{rawtr},Object.class);
		if(!(signed_rawtr.get("complete") instanceof Boolean||!(boolean)signed_rawtr.get("complete")) )throw new Exception("Can't sign transaction");
		rawtr=(String)signed_rawtr.get("hex");
		
		

		return new Object[]{rawtr,net};
		
	}
	
	@Override
	public synchronized String sendRawTransaction(String raw) throws Throwable{
		connect();
		String trid=(String)CLIENT.invoke("sendrawtransaction",new Object[]{raw},Object.class);
		return trid;
	}

	@Override
	public synchronized BlockchainFee estimateFee() {
		try{
			connect();
			if((boolean)_CONFIG.get("estimate_blockchainfee")){
				try{
					double dfee=((Number)CLIENT.invoke("estimatefee",new Object[]{3},Object.class)).doubleValue();
					if(dfee>0){
						return new BlockchainFee(convertFromBitcoindRep(dfee));
					}
				}catch(Throwable e){
					System.out.println("Can't estimate fee");
				}
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
		return null;		
	}
	
	@Override
	public synchronized String getNewAddress() throws Throwable {
		connect();

		String addr=(String)CLIENT.invoke("getnewaddress",new Object[]{},Object.class);
		return addr;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean isTestnet() throws Throwable{
		connect();

		Map<String,Object> info=(Map<String,Object>)CLIENT.invoke("getinfo",new Object[]{},Object.class);
		return (boolean)info.get("testnet");		
	}

	@Override
	public synchronized long getBalance() throws Throwable {
		connect();
		int minconf=((Number)_CONFIG.get("minconf")).intValue();

		double dv=(double)CLIENT.invoke("getbalance",new Object[]{"",minconf},Object.class);
		return convertFromBitcoindRep(dv);
	}
	
	@Override
	public synchronized void waitForSync(){

		boolean ready=false;
		System.out.println("Wait for blockchain...");

		do{
			try{
				connect();
				getBalance();
				ready=true;
				System.out.println("Blockchain ready.");
			}catch(Throwable e){
				try{
					Thread.sleep(100);
				}catch(InterruptedException e1){
					e1.printStackTrace();
				}
			}
		}while(!ready);
	}

	
}

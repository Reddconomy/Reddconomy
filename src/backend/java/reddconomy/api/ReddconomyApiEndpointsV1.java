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
package reddconomy.api;

import java.util.Collection;
import java.util.Map;

import reddconomy.blockchain.BlockchainConnector;
import reddconomy.data.Deposit;
import reddconomy.data.EmptyData;
import reddconomy.data.NetworkInfo;
import reddconomy.data.OffchainContract;
import reddconomy.data.OffchainWallet;
import reddconomy.data.Withdraw;
import reddconomy.offchain_database.Database;

public class ReddconomyApiEndpointsV1 extends Thread implements ApiEndpoints {

	
	private final Database _DATABASE;

	private BlockchainConnector _WALLET;
	private boolean CLOSED=false;
	
	public ReddconomyApiEndpointsV1(BlockchainConnector conn,Database db) throws Exception{
		_WALLET=conn;
		_DATABASE=db;
	}
	
	public void open(){
		setDaemon(true);
		start();
	}

	public synchronized void close() {
		CLOSED=true;
	}

	public void run() {
		long t=0;
		while(!CLOSED){
			long delta_t=t==0?0:System.currentTimeMillis()-t;
			try{
				Collection<Deposit> deposits=_DATABASE.getIncompletedDepositsAndUpdate(delta_t);
				for(Deposit deposit:deposits){
					String addr=deposit.addr;
					long expected_balance=deposit.expected_balance;
					try{
						if(_WALLET.getReceivedByAddress(addr)>=expected_balance){
							_DATABASE.completeDeposit(addr);
						}
					}catch(Throwable e){
						e.printStackTrace();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			t=System.currentTimeMillis();
			try{
				Thread.sleep(10000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * API v1 Endpoints
	 * 
	 * ?action=deposit&wallid=XXXXXX&amount=XXXXXX
	 *   Response: Deposit
	 * ?action=getdeposit&addr=XXXXX
	 * 	 Response: Deposit
	 * ?action=withdraw&ammount=XXXXXXX&addr=XXXXX
	 * 	 Response: Withdraw
	 * ?action=getwallet&wallid=XXXXXXX
	 *   Response: OffchainWallet
	 * ?action=newcontract&wallid=XXXXXXX&amount=XXXXXXX
	 *   Response: OffchainContract
	 * ?action=acceptcontract&wallid=XXXXXX&contractid=XXXXXX
	 *   Response: OffchainContract
	 * ?action=getcontract&contractid=XXXXXX
	 *   Response: OffchainContract
	 * ?action=sendcoins&amount=XXXX&addr=XXXx
	 * 	 Response: EmptyData
	 * ?action=netinfo
	 *   Response: NetworkInfo
	 */
	public ApiResponse onRequest(String action, Map<String,String> _GET) {
		ApiResponse response=ApiResponse.build();
		switch(action){			
			case "deposit":{
				try{
					String addr=_WALLET.getNewAddress();
					String wallet_id=_GET.get("wallet_id").toString();
					long balance=Long.parseLong(_GET.get("amount").toString());
					Deposit data=_DATABASE.prepareForDeposit(addr,wallet_id,balance);
					response.success(data);
				}catch(Throwable e){					
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "getdeposit":{
				try{
					String addr=_GET.get("addr").toString();
					Deposit deposit=_DATABASE.getDeposit(addr);
					response.success(deposit);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "withdraw":{
				long amount=Long.parseLong(_GET.get("amount").toString());
				String addr=(String)_GET.get("addr");
				String wallet_id=_GET.get("wallid").toString();
				try{
					OffchainWallet wallet=_DATABASE.getOffchainWallet(wallet_id);
					long balance=wallet.balance;
					if(balance>=amount){
						_WALLET.sendToAddress(addr,amount);
						Withdraw data=_DATABASE.withdraw(wallet_id,amount);
						response.success(data);
					}else{
						response.error(401,"Not enough money");
					}
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "getwallet":{
				try{
					String wallet_id=_GET.get("wallid").toString();
					OffchainWallet data=_DATABASE.getOffchainWallet(wallet_id);
					response.success(data);					
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "newcontract":{
				try{
					String wallet_id=_GET.get("wallid").toString();
					long amount=Long.parseLong(_GET.get("amount").toString());
					OffchainContract contract=_DATABASE.createContract(wallet_id,amount);
					response.success(contract);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);

					e.printStackTrace();
				}

				break;
			}
			case "acceptcontract":{
				try{
					String wallet_id=_GET.get("wallid").toString();
					String contractId=_GET.get("contractid").toString();
					OffchainContract contract=_DATABASE.acceptContract(contractId,wallet_id);
					response.success(contract);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}

				break;
			}
			case "sendcoins":{
				try{
					String addr=_GET.get("addr").toString();
					long amount=Long.parseLong(_GET.get("amount").toString());
					_WALLET.sendToAddress(addr,amount);
					response.success(new EmptyData());
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "getcontract":{
				try{
					String contractId=_GET.get("contractid").toString();
					OffchainContract contract=_DATABASE.getContract(contractId);
					response.success(contract);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "netinfo":{
				try{
					NetworkInfo netinfo=new NetworkInfo();
					netinfo.testnet=_WALLET.isTestnet();
					response.success(netinfo);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
			}
			default:
				response.error(405,"Invalid action");

		}
		return response;
	}
}

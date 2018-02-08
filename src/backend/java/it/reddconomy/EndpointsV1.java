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
package it.reddconomy;

import java.util.Collection;
import java.util.Map;

import it.reddconomy.blockchain.BlockchainConnector;
import it.reddconomy.common.ApiResponse;
import it.reddconomy.common.data.Deposit;
import it.reddconomy.common.data.EmptyData;
import it.reddconomy.common.data.Info;
import it.reddconomy.common.data.OffchainContract;
import it.reddconomy.common.data.OffchainWallet;
import it.reddconomy.common.data.Withdraw;
import it.reddconomy.common.fees.BlockchainFee;
import it.reddconomy.core.ReddconomyCore;

public class EndpointsV1 extends Thread implements ApiEndpoints {
	private final long _STARTTIME=System.currentTimeMillis();

	
	private final ReddconomyCore _OFFCHAIN;

	private BlockchainConnector _BLOCKCHAIN;
	private boolean CLOSED=false;
	private final Map<String,Object> _CONFIG;
	
	public EndpointsV1(Map<String,Object> config,BlockchainConnector conn,ReddconomyCore db) throws Exception{
		_BLOCKCHAIN=conn;
		_OFFCHAIN=db;
		_CONFIG=config;
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
				Collection<Deposit> deposits=_OFFCHAIN.getIncompletedDepositsAndUpdate(delta_t);
				for(Deposit deposit:deposits){
					String addr=deposit.addr;
					long expected_balance=deposit.expected_balance;
					try{
						if(_BLOCKCHAIN.getReceivedByAddress(addr)>=expected_balance){
							_OFFCHAIN.completeDeposit(addr);
						}
					}catch(Throwable e){
						e.printStackTrace();
					}
				}
			}catch(Throwable e){
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
	 * /?action=deposit&wallid=XXXXXX&amount=XXXXXX
	 *   Response: Deposit
	 * /?action=getdeposit&addr=XXXXX
	 * 	 Response: Deposit
	 * /?action=withdraw&ammount=XXXXXXX&addr=XXXXX
	 * 	 Response: Withdraw
	 * /?action=getwallet&wallid=XXXXXXX
	 *   Response: OffchainWallet
	 * /?action=newcontract&wallid=XXXXXXX&amount=XXXXXXX
	 *   Response: OffchainContract
	 * /?action=acceptcontract&wallid=XXXXXX&contractid=XXXXXX
	 *   Response: OffchainContract
	 * /?action=getcontract&contractid=XXXXXX
	 *   Response: OffchainContract
	 * /?action=sendcoins&amount=XXXX&addr=XXXx
	 * 	 Response: EmptyData
	 * /?action=info
	 *   Response: Info
	 *   
	 *   Authentication
	 *   Requests are authenticated by adding an header "Hash"
	 *   which contains the base64-hmacsha256 hash of the requested location and resource 
	 *   calculated using the secret key.
	 *   eg.  Hash: base64(hmacsha256(DECRET_KEY,"/?action=deposit&wallid=XXXXXX&amount=XXXXXX"))
	 *   
	 */
	public synchronized ApiResponse onRequest(String action, Map<String,String> _GET) {
		ApiResponse response=ApiResponse.build();
		switch(action){			
			case "deposit":{
				try{
					String wallet_id=_GET.get("wallid").toString();
					long balance=Long.parseLong(_GET.get("amount").toString());
					Deposit data=_OFFCHAIN.prepareForDeposit(wallet_id,balance);
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
					Deposit deposit=_OFFCHAIN.getDeposit(addr);
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
				String from=_GET.get("wallid").toString();
				String to=(String)_GET.get("addr");
				boolean noconfirm=_GET.containsKey("noconfirm");

				
				BlockchainFee net_fee=null;
				
				if(_GET.containsKey("fee")){
					Long fee=Long.parseLong(_GET.get("fee"));
					if(fee>0){
						net_fee=new BlockchainFee(fee);
					}else{
						response.error(500,"Fee is less than 0!?");
						break;
					}
				}

				try{
					Withdraw data=_OFFCHAIN.withdraw(from,amount,to,net_fee,noconfirm);
					response.success(data);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "confirm_withdraw":{
				String id=(_GET.get("id").toString());
				try{
					Withdraw data=_OFFCHAIN.confirmWithdraw(id);
					response.success(data);

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
					OffchainWallet data=_OFFCHAIN.getOffchainWallet(wallet_id);
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
					OffchainContract contract=_OFFCHAIN.createOffchainContract(wallet_id,amount);
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
					long contractId=Long.parseLong(_GET.get("contractid"));
					OffchainContract contract=_OFFCHAIN.acceptOffchainContract(contractId,wallet_id);
					response.success(contract);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}

				break;
			}
//			case "sendcoins":{
//				try{
//					String addr=_GET.get("addr").toString();
//					long amount=Long.parseLong(_GET.get("amount").toString());
//					_BLOCKCHAIN.sendToAddress(addr,amount);
//					response.success(new EmptyData());
//				}catch(Throwable e){
//					String error=e.toString();
//					response.error(500,error);
//					e.printStackTrace();
//				}
//				break;
//			}
			case "getcontract":{
				try{
					long contractId=Long.parseLong(_GET.get("contractid"));
					OffchainContract contract=_OFFCHAIN.getOffchainContract(contractId);
					response.success(contract);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;
			}
			case "info":{
				try{
					Info netinfo=new Info();
					netinfo.testnet=_BLOCKCHAIN.isTestnet();
	
					netinfo.welcome_funds_wallid=_CONFIG.get("welcome_funds_walletid").toString();
					netinfo.fees_collector_wallid=_CONFIG.get("fees_collector_wallid").toString();
					netinfo.generic_wallid=_CONFIG.get("generic_wallid").toString();
					netinfo.null_wallid=_CONFIG.get("null_wallid").toString();
					
					
					
					netinfo.fees=_OFFCHAIN.getFees();
					netinfo.coin=_CONFIG.get("coin").toString();
					netinfo.coin_short=_CONFIG.get("coin_short").toString();
					netinfo.welcome_tip=_OFFCHAIN.getWelcomeTip();
					netinfo.uptime=System.currentTimeMillis()-_STARTTIME;
					
					
//					netinfo.welcome_funds_wallid=OF
					response.success(netinfo);
				}catch(Throwable e){
					String error=e.toString();
					response.error(500,error);
					e.printStackTrace();
				}
				break;

			}
			default:
				response.error(405,"Invalid action");

		}
		return response;
	}
}

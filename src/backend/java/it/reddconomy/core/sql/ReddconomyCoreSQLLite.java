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
package it.reddconomy.core.sql;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import it.reddconomy.Config;
import it.reddconomy.Utils;
import it.reddconomy.blockchain.BlockchainConnector;
import it.reddconomy.common.data.Deposit;
import it.reddconomy.common.data.OffchainContract;
import it.reddconomy.common.data.OffchainWallet;
import it.reddconomy.common.data.Withdraw;
import it.reddconomy.common.data.OffchainContract.TransactionDirection;
import it.reddconomy.common.fees.Fee;
import it.reddconomy.common.fees.Fees;
import it.reddconomy.common.fees.BlockchainFee;
import it.reddconomy.core.ReddconomyCore;


/**
 * Test DB implementation on SQLITE - >>>> DO NOT USE THIS FOR ANYTHING OTHER THAN TESTING <<<< REALLY!
 * @author Riccardo Balbo
 */

public  class ReddconomyCoreSQLLite implements ReddconomyCore {
	protected Connection CONNECTION;

	protected String FEES_WALLET,WELCOME_WALLET,GENERIC_WALLET,NULL_WALLET;	

	
	protected final int _MAX_SHORT_ID_SIZE="9999999999999".length();
	
	protected BlockchainConnector BLOCKCHAIN;
	protected Fees FEES=new Fees();
	protected Config CONFIG;
	protected long WELCOME_TIP;
	
	
	protected Map<Long,Withdraw> PENDING_WITHDRAW=new HashMap<Long,Withdraw>();
	protected long LATEST_WITHDRAW_ID;


	public ReddconomyCoreSQLLite(String path) throws Exception{
		Class.forName("org.sqlite.JDBC");
		CONNECTION=DriverManager.getConnection("jdbc:sqlite:"+path);
	}
	
	
	public Fees getFees(){
		return FEES;
	}

	
	public long getWelcomeTip(){
		return WELCOME_TIP;
	
	}
	
	@Override
	public void open(BlockchainConnector blc, Config config) throws Exception {
		CONFIG=config;
		
		WELCOME_TIP=Utils.convertToInternal(((Number)CONFIG.get("welcome_tip")).doubleValue());
		
		FEES.fromMap(CONFIG,true);

		FEES_WALLET=CONFIG.get("fees_collector_wallid").toString();
		WELCOME_WALLET=CONFIG.get("welcome_funds_walletid").toString();
		GENERIC_WALLET=CONFIG.get("generic_wallid").toString();
		NULL_WALLET=CONFIG.get("null_wallid").toString();

		BLOCKCHAIN=blc;

		SQLResult q;
		
		q=query("SELECT * FROM `reddconomy_wallets`",true,true);
		if(q==null){
			query("CREATE TABLE `reddconomy_wallets` ( "
					+ "`short_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
					+ "`id`  TEXT NOT NULL UNIQUE  ,"
					+ "`balance` INTEGER DEFAULT 0, "
					+ "`status` INTEGER DEFAULT 1 "
					+ " );",false,false);
			query("CREATE UNIQUE INDEX `reddconomy_idIndex` ON  `reddconomy_wallets` (`id`)",false,false);
		}
		
		
		q=query("SELECT * FROM `reddconomy_contracts`",true,true);
		if(q==null){
			query("CREATE TABLE `reddconomy_contracts` ( "
					+ "`id`  INTEGER NOT NULL PRIMARY KEY  AUTOINCREMENT,"
					+ "`createdby` TEXT NOT NULL, "
					+ "`amount` INTEGER DEFAULT 0, "
					+ "`acceptedby` TEXT DEFAULT '',"
					+ "`created` INTEGER NOT NULL,"
					+ "`paid_in_fees` INTEGER NOT NULL DEFAULT 0,"
					+ "`accepted`  INTEGER NOT NULL DEFAULT -1,"
					+ "FOREIGN KEY(`createdby`) REFERENCES reddconomy_wallets(`id`), "
					+ "FOREIGN KEY(`acceptedby`) REFERENCES reddconomy_wallets(`id`) "
					+ ");"
			,false,false);
		}
		
		q=query("SELECT * FROM `reddconomy_deposits`",true,true);
		if(q==null){
			query("CREATE TABLE `reddconomy_deposits` ( "
					+ "`addr`  TEXT  NOT NULL PRIMARY KEY ,"
					+ "`receiver` TEXT NOT NULL, "
					+ "`expected_balance` INTEGER DEFAULT 0, "
					+ "`status` INTEGER DEFAULT 1,"
					+ "`paid_in_fees` INTEGER NOT NULL DEFAULT 0,"
					+ "`created` INTEGER NOT NULL,"
					+ "`expiring` INTEGER DEFAULT 172800000, "
					+ "FOREIGN KEY(`receiver`) REFERENCES reddconomy_wallets(`id`) "
					+ ");"
			,false,false);

		}

		q=query("SELECT * FROM `reddconomy_logs`",true,true);
		if(q==null){
			//NOT IMPLEMENTED
		}

		// Initialize server wallets
		getOffchainWallet(FEES_WALLET);
		getOffchainWallet(WELCOME_WALLET);
		getOffchainWallet(GENERIC_WALLET);
		getOffchainWallet(NULL_WALLET);
			
	}
	
	

	protected boolean isAServerWallet(String wallid){
		return (wallid.equals(GENERIC_WALLET)||
			wallid.equals(WELCOME_WALLET)
			||wallid.equals(FEES_WALLET)
			||isNullWallet(wallid)
				);
	}
	
	protected boolean isNullWallet(String id_or_short){
		return id_or_short.equals(this.NULL_WALLET)||id_or_short.equals("-1");
	}
	
	
	protected OffchainWallet getNullWallet(){
		try{
			long blockchain_total_balance=BLOCKCHAIN.getBalance();
			SQLResult res=query("SELECT SUM(`balance`) FROM `reddconomy_wallets`",true,false);
			long offchain_total_balance=((Number)res.fetchAssoc().values().iterator().next()).longValue();
			System.out.println("Blockchain balance "+blockchain_total_balance);
			System.out.println("Offchain balance "+offchain_total_balance);
			if(blockchain_total_balance<offchain_total_balance){
				System.err.println("FATAL! Something is wrong, you have more offchain coins than blockchain coins!?!");
				System.exit(1);
			}
			long null_balance=blockchain_total_balance-offchain_total_balance;
			OffchainWallet wallet=new OffchainWallet();
			wallet.id=NULL_WALLET;
			wallet.short_id=-1;
			wallet.balance=null_balance;
			return wallet;
			
		}catch(Throwable e){
			e.printStackTrace();
			return null;
		}
		
		
	}
	

	protected String getWalletIdFromShortId(String short_id) throws SQLException{
		if(isNullWallet(short_id))return NULL_WALLET;
		short_id=short_id.replaceAll("[^A-Za-z0-9_\\-]","_");

		SQLResult res=query("SELECT "
				+ "`id` FROM `reddconomy_wallets` WHERE `short_id`='"+short_id+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,Object> o=res.fetchAssoc();
			return o.get("id").toString();			
		}
		return null;
	}
	
	protected String parseWalletId(String id) throws SQLException{
		if(id.startsWith("s:")){
			id=id.substring("s:".length());			
			id=getWalletIdFromShortId(id);
		}else id=id.replaceAll("[^A-Za-z0-9_\\-]","_");
		return id;
	}
	

	
	
	@Override
	public synchronized OffchainWallet getOffchainWallet(String id) throws SQLException{
		if(isNullWallet(id))return getNullWallet();
		
		id=parseWalletId(id);
		OffchainWallet out=new  OffchainWallet();
		out.id=id;
		SQLResult res=query("SELECT "
				+ "`status`,`balance`,`short_id` "
				+ "FROM `reddconomy_wallets` WHERE `id`='"+id+"' LIMIT 0,1",true,false);
		
		if(res==null||res.isEmpty()){
		
			// Paranoia: Make sure it'll take at least 3000 years to exceed the maximum short id even under heavy spam.
			try{
				Thread.sleep(10);
			}catch(InterruptedException e1){
				e1.printStackTrace();
			}
			// -
		
			// NEW WALLET		
			System.out.println("Create new wallet");
			query("INSERT INTO reddconomy_wallets(`id`) VALUES('"+id+"')",false,false);

			// Welcome tip
			System.out.println("Welcome tip: "+getWelcomeTip());
			if(!isAServerWallet(id)&&getWelcomeTip()>0){
				// Add welcome funds
				System.out.println("Create welcome contract");
				try{
					OffchainContract contract=createOffchainContract(WELCOME_WALLET,-WELCOME_TIP);
					acceptOffchainContract(contract.id,id);
					out.balance=WELCOME_TIP;
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				System.out.println("Not elegible");
			}
			
			// Get the newly created wallet
			return getOffchainWallet(id); 			
		}else{
			Map<String,Object> fetched=res.fetchAssoc();
			out.status=((Number)fetched.get("status")).byteValue();
			out.balance=((Number)fetched.get("balance")).longValue();
			out.short_id=((Number)fetched.get("short_id")).longValue();
			if((""+out.short_id).length()>_MAX_SHORT_ID_SIZE){
				System.err.println("Fatal! Maximum short_id reached?!");
				System.exit(1);
			}
		}		
		
		return out;
	}
	
	
	
	@Override
	public synchronized OffchainContract getOffchainContract(long id) throws Exception{
//		id=id.replaceAll("[^A-Za-z0-9]","_");		
		OffchainContract out=new OffchainContract();
		SQLResult res=query("SELECT * FROM `reddconomy_contracts` WHERE `id`='"+id+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,Object> fetched=res.fetchAssoc();
			out.id=((Number)fetched.get("id")).longValue();
			out.amount=((Number)fetched.get("amount")).longValue();
			out.acceptedby=fetched.get("acceptedby").toString();
			out.createdby=fetched.get("createdby").toString();
			out.created=((Number)fetched.get("created")).longValue();
			out.paid_in_fees=((Number)fetched.get("paid_in_fees")).longValue();
			out.accepted=((Number)fetched.get("accepted")).longValue();
			return out;
		}else{
			throw new Exception("Contract does not exist");
		}
	}
	

	@Override
	public synchronized OffchainContract createOffchainContract(String walletid,long amount) throws Exception{
		if(isNullWallet(walletid))throw new Exception("Null wallet can't create contracts");
		
		// Paranoia
		try{
			Thread.sleep(10);
		}catch(InterruptedException e1){
			e1.printStackTrace();
		}
		// -
		walletid=parseWalletId(walletid);
		query("INSERT INTO  reddconomy_contracts(`createdby`,`amount`, `created`) VALUES('"+walletid+"','"+amount+"','"+System.currentTimeMillis()+"')",false,false);			
		SQLResult res=query("SELECT last_insert_rowid()",true,false);
		if(res!=null){
			long contractid=((Number)res.fetchAssoc().values().iterator().next()).longValue();
			return getOffchainContract(contractid);
		}else{			
			throw new Exception("Can't get last contract?");
		}
	}
	

	@Override
	public synchronized OffchainContract acceptOffchainContract(long contractid, String acceptedby_walletid) throws Exception {
		if(isNullWallet(acceptedby_walletid))throw new Exception("Null wallet can't accept contracts");

//		contractid=contractid.replaceAll("[^A-Za-z0-9]","_");		
		acceptedby_walletid=parseWalletId(acceptedby_walletid);
		OffchainContract contract=getOffchainContract(contractid);
		if(contract!=null){
			System.out.println("Accept contract "+contract+"from wallid "+acceptedby_walletid);
			String accp=contract.acceptedby;
			if(accp==null||accp.isEmpty()){
				OffchainWallet acceptedby_wallet=getOffchainWallet(acceptedby_walletid);
				String createdby_walletid=contract.createdby;
				OffchainWallet createdby_wallet=getOffchainWallet(createdby_walletid);

				long amount=contract.amount; if(amount<0)amount=-amount;
			
				long acceptedby_balance=acceptedby_wallet.balance;
				long createdby_balance=createdby_wallet.balance;
				long tare=0;
				
				if(TransactionDirection.get(contract)==TransactionDirection.ACCEPTEDBY2CREATEDBY){
					System.out.println("Type 1");
					if(acceptedby_balance>=amount){
						System.out.println("Amount to deduct from "+acceptedby_walletid+ " = "+amount);

						acceptedby_balance-=amount;
						
						// Fee 
						long net=FEES.getTransactionFee().apply(amount);
						if(!isAServerWallet(acceptedby_walletid)&&!isAServerWallet(createdby_walletid)){							
							tare=amount-net;
							OffchainWallet fees_wallet=getOffchainWallet(FEES_WALLET);
							fees_wallet.balance+=tare;
							query("UPDATE reddconomy_wallets SET "
									+ "`balance`='"+fees_wallet.balance+"' WHERE `id`='"+fees_wallet.id+"'",false,false);
							amount=net;
						}
						//
						System.out.println("Amount to add to "+createdby_walletid+ " = "+amount+ " (net)");
						System.out.println("Paid in fees "+tare);
						
						createdby_balance+=amount;
						query("UPDATE reddconomy_wallets SET `balance`='"+acceptedby_balance+"' WHERE "
								+ "`id`='"+acceptedby_walletid+"'",false,false);
						query("UPDATE reddconomy_wallets SET `balance`='"+createdby_balance+"' WHERE "
								+ "`id`='"+createdby_walletid+"'",false,false);
					}else {
						System.out.println("Not enough coins");
						throw new Exception("Not enought coins. Requested:"+amount+" Remaining balance: "+acceptedby_balance);
					}
				
				}else if(TransactionDirection.get(contract)==TransactionDirection.CREATEDBY2ACCEPTEDBY){
					System.out.println("Type 2");

					if(createdby_balance>=amount){
						System.out.println("Amount to deduct from "+createdby_walletid+ " = "+amount);

						createdby_balance-=amount;
						
						// Fee 
						long net=FEES.getTransactionFee().apply(amount);
						if(!isAServerWallet(acceptedby_walletid)&&!isAServerWallet(createdby_walletid)){							
							tare=amount-net;

							OffchainWallet fees_wallet=getOffchainWallet(FEES_WALLET);
							fees_wallet.balance+=tare;
							query("UPDATE reddconomy_wallets SET "
									+ "`balance`='"+fees_wallet.balance+"' WHERE `id`='"+fees_wallet.id+"'",false,false);
							amount=net;

						}
						//
						System.out.println("Amount to add to "+acceptedby_walletid+ " = "+amount+ " (net)");
						System.out.println("Paid in fees "+tare);
				
						acceptedby_balance+=amount;
						query("UPDATE reddconomy_wallets SET `balance`='"+acceptedby_balance+"' WHERE "
								+ "`id`='"+acceptedby_walletid+"'",false,false);
						query("UPDATE reddconomy_wallets SET `balance`='"+createdby_balance+"' WHERE "
								+ "`id`='"+createdby_walletid+"'",false,false);
					}else {
						System.out.println("Not enough coins");
						throw new Exception("Not enought coins. Requested:"+amount+" Remaining balance: "+createdby_balance);
					}
				}else{
					System.out.println("Type Invalid");
					throw new Exception("Invalid type");

				}
			
				query("UPDATE reddconomy_contracts SET `accepted`= '"+System.currentTimeMillis()+"' ,"
						+" `paid_in_fees`='"+tare+"',"
						+ "`acceptedby`='"+acceptedby_walletid+"' WHERE `id`='"+contractid+"'",false,false);

			}else{
				throw new Exception("Contract already accepted by "+accp);
			}
		}else{
			throw new Exception("Contract "+contractid+" doesn't exist");

		}
		return contract;
	}
	
	

	@Override
	public synchronized Withdraw withdraw(String from,long amount,String to,BlockchainFee net_fee,boolean noconfirm) throws Throwable{
		to=parseWalletId(to);
		from=parseWalletId(from);

		if(net_fee==null)net_fee=BLOCKCHAIN.estimateFee();
		if(net_fee==null)net_fee=getFees().getBlockchainFee();
	

		OffchainWallet wallet=getOffchainWallet(from);
		long balance=wallet.balance;
		if(balance>=amount){
			if(BLOCKCHAIN.getBalance()>=amount){
				long amount_minus_offchainfees=amount;
				if(!isAServerWallet(wallet.id)){
					amount_minus_offchainfees=getFees().getWithdrawFee().apply(amount);
				}
				
				Object rawtr[]=BLOCKCHAIN.createRawTransaction(to,amount_minus_offchainfees,net_fee.getRaw());
				String rawt=(String)rawtr[0];
				long amount_net=(long)rawtr[1];
				
				Withdraw data=new Withdraw();
				data.amount=amount;
				data.from_wallet=from;
				data.to_addr=to;
				data.amount_net=amount_net;
				data.paid_in_fees=amount-amount_minus_offchainfees;
				data.raw=rawt;

				long id=(LATEST_WITHDRAW_ID++);
				data.id=""+id;
				data.confirmed=false;
				
				PENDING_WITHDRAW.put(id,data);
				if(isAServerWallet(wallet.id)||noconfirm)confirmWithdraw(data.id);
				
				
				return data;
			}else{
				throw new Exception("The server doesn't have enough coins");
			}

		}else{
			throw new Exception("Not enough coins");
		}
	}

	@Override
	public synchronized Withdraw confirmWithdraw(String withdraw_id) throws Throwable{
		
		Withdraw data=PENDING_WITHDRAW.remove(Long.parseLong(withdraw_id));
		
		if(data==null)	throw new Exception("Can't find pending withdraw with id "+withdraw_id+". Inexistent or already confirmed.");				
		data.confirmed=true;
		OffchainWallet wallet=getOffchainWallet(data.from_wallet);
		
		if(wallet.balance>=data.amount){
			if(BLOCKCHAIN.getBalance()>=data.amount){
		
				
				if(isNullWallet(wallet.id)){
					data.id=BLOCKCHAIN.sendRawTransaction(data.raw);
					return data;
				}
				
				
				data.id=BLOCKCHAIN.sendRawTransaction(data.raw);
				wallet.balance-=data.amount;
				query("UPDATE reddconomy_wallets SET `balance`='"+wallet.balance+"' WHERE `id`='"+wallet.id+"'",false,false);
				if(!isAServerWallet(wallet.id)){
					OffchainWallet fees_wallet=getOffchainWallet(FEES_WALLET);
					fees_wallet.balance+=data.paid_in_fees;
					query("UPDATE reddconomy_wallets SET `balance`='"+fees_wallet.balance+"' WHERE `id`='"+fees_wallet.id+"'",false,false);
				}
				
			}else throw new Exception("The server doesn't have enough coins");
		}else throw new Exception("Not enough coins");

		
		
		return data;
	}
	
	

	
	
	@Override
	public synchronized Deposit prepareForDeposit(String walletid,long expected_balance)throws Throwable{
		if(isNullWallet(walletid))throw new Exception("You can not deposit on null wallet");
		walletid=parseWalletId(walletid);
		String deposit_addr=BLOCKCHAIN.getNewAddress();
		query("INSERT INTO reddconomy_deposits(`created`,`addr`,`receiver`,`expected_balance`) VALUES('"+System.currentTimeMillis()+"','"+deposit_addr+"','"+walletid+"','"+expected_balance+"')",false,false);
		return getDeposit(deposit_addr);
	}
	

	@Override
	public synchronized Collection<Deposit> getIncompletedDepositsAndUpdate(long tms) throws Exception {
		ArrayList<Deposit> out=new ArrayList<Deposit>();
		SQLResult res=query("SELECT * FROM `reddconomy_deposits` WHERE `status`='1'",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,Object> fetch;
			while(!(fetch=res.fetchAssoc()).isEmpty()){
				
				String addr=fetch.get("addr").toString();
				byte status=((Number)fetch.get("status")).byteValue();
				long exp=((Number)fetch.get("expiring")).longValue();

				
				exp-=tms;
				if(exp<=0){
					status=-1;
				}else{
					Deposit deposit=new Deposit();
					deposit.addr=addr;
					deposit.receiver_wallet_id=fetch.get("receiver").toString();
					deposit.expected_balance=((Number)fetch.get("expected_balance")).longValue();
					deposit.status=status;
					deposit.created=((Number)fetch.get("created")).longValue();
					deposit.expiring=((Number)exp).intValue();
					out.add(deposit);
				}
				
				query("UPDATE reddconomy_deposits SET `expiring`='"+exp+"',`status`='"+status+"' WHERE `addr`='"+addr+"'",false,false);

			}
		}
		return out;
	}
	
	
	@Override
	public synchronized Deposit completeDeposit(String deposit_addr) throws Exception{
		deposit_addr=deposit_addr.replaceAll("[^A-Za-z0-9]","_");
		SQLResult res=query("SELECT `receiver`,`expected_balance` FROM `reddconomy_deposits` WHERE `addr`='"+deposit_addr+"' LIMIT 0,1",true,false);
		if(res==null){
			throw new Exception("Deposit is not ready");
		}else{
			
			Map<String,Object> deposit=res.fetchAssoc();
			long amount=((Number)deposit.get("expected_balance")).longValue();
			
			String wallet_id=deposit.get("receiver").toString();
			
			long net=FEES.getDepositFee().apply(amount);
			long tare=amount-net;
			
			if(!isAServerWallet(wallet_id)){
				
				OffchainWallet fees_wallet=getOffchainWallet(FEES_WALLET);
				fees_wallet.balance+=tare;
				query("UPDATE reddconomy_wallets SET `balance`='"+fees_wallet.balance+"' WHERE `id`='"+fees_wallet.id+"'",false,false);

				amount=net;
			}else{
				tare=0;
			}
			
			query("UPDATE reddconomy_deposits SET `status`='0',`paid_in_fees`='"+tare+"' WHERE `addr`='"+deposit_addr+"'",false,false);

			
			OffchainWallet wallet=this.getOffchainWallet(wallet_id);
			long balance=wallet.balance;
			balance+=amount;
			query("UPDATE reddconomy_wallets SET `balance`='"+balance+"' WHERE `id`='"+wallet_id+"'",false,false);
		}
		return getDeposit(deposit_addr);
	}
	
	@Override
	public synchronized Deposit getDeposit(String deposit_addr) throws Exception{
		Deposit resp=new Deposit();
		deposit_addr=deposit_addr.replaceAll("[^A-Za-z0-9]","_");
		SQLResult res=query("SELECT * FROM `reddconomy_deposits` WHERE `addr`='"+deposit_addr+"' LIMIT 0,1",true,false);
		if(res==null){
			throw new Exception("Deposit is not ready");
		}else{		
			Map<String,Object> deposit=res.fetchAssoc();
			resp.addr=deposit_addr;
			resp.receiver_wallet_id=deposit.get("receiver").toString();
			resp.expected_balance=((Number)deposit.get("expected_balance")).longValue();
			resp.status=((Number)deposit.get("status")).byteValue();
			resp.created=((Number)deposit.get("created")).longValue();
			resp.expiring=((Number)deposit.get("status")).intValue();
			resp.paid_in_fees=((Number)deposit.get("paid_in_fees")).longValue();

		}
		return resp;
	}
	
	/**
	 * Query sql
	 * @param q query
	 * @param expectoutput true if an output is expected (slower)
	 * @param noerror true to ignore errors
	 * @return
	 * @throws SQLException
	 */
	protected synchronized SQLResult query(String q,boolean expectoutput,boolean noerror) throws SQLException{
		SQLResult result=null;
		try {
			if(expectoutput){
				PreparedStatement stm = CONNECTION.prepareStatement(q,ResultSet.TYPE_FORWARD_ONLY,
					    ResultSet.CONCUR_READ_ONLY);
				result=new SQLResult(stm.executeQuery()); 
			}else{
				Statement stm = CONNECTION.createStatement();
				stm.executeUpdate(q);
			}
		} catch (SQLException e) {
            if(!noerror){
            	System.err.println("ERROR: "+q+"\n" + e.getLocalizedMessage());
            	throw e;
            }
		}
		return result;
	}
	
	
	@Override
	public synchronized boolean close(){
		try {
			CONNECTION.close();
			return true;
		} catch (SQLException e) {
            System.err.println("Could not close connection: " + e.getLocalizedMessage());
		}
		return false;
	}



}

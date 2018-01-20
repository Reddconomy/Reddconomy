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
package reddconomy.offchain.sql;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import reddconomy.data.Deposit;
import reddconomy.data.OffchainContract;
import reddconomy.data.OffchainWallet;
import reddconomy.data.Withdraw;
import reddconomy.data.OffchainContract.TransactionDirection;
import reddconomy.offchain.Offchain;
import reddconomy.offchain.fees.Fees;


/**
 * Test DB implementation on SQLITE - >>>> DO NOT USE THIS FOR ANYTHING OTHER THAN TESTING <<<< REALLY!
 * @author Riccardo Balbo
 */

public  class SQLiteOffchainDatabase implements Offchain {
	protected Connection CONNECTION;
	protected Fees FEES;
	protected String FEES_WALLET,WELCOME_WALLET,GENERIC_WALLET;
	protected long WELCOME_TIP;
	
	public SQLiteOffchainDatabase(String path) throws Exception{
		Class.forName("org.sqlite.JDBC");
		CONNECTION=DriverManager.getConnection("jdbc:sqlite:"+path);
	}

	@Override
	public void open(Fees fees,String feescollector_wallet,String welcomefunds_wallet,String generic_wallet,
			long welcome_tip) throws Exception{
		FEES=fees;
		FEES_WALLET=feescollector_wallet;
		WELCOME_WALLET=welcomefunds_wallet;
		GENERIC_WALLET=generic_wallet;
		WELCOME_TIP=welcome_tip;
		
		
		
SQLResult q;
		
		q=query("SELECT * FROM `reddconomy_wallets`",true,false);
		if(q==null||q.isEmpty()){
			query("CREATE TABLE `reddconomy_wallets` ( "
					+ "`id`  TEXT NOT NULL PRIMARY KEY,"
					+ "`balance` INTEGER DEFAULT 0, "
					+ "`status` INTEGER DEFAULT 1 "
					+ " );",false,false);
		}
		
		q=query("SELECT * FROM `reddconomy_contracts`",true,false);
		if(q==null||q.isEmpty()){
			query("CREATE TABLE `reddconomy_contracts` ( "
					+ "`id`  TEXT NOT NULL PRIMARY KEY,"
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
		
		q=query("SELECT * FROM `reddconomy_deposits`",true,false);
		if(q==null||q.isEmpty()){
			query("CREATE TABLE `reddconomy_deposits` ( "
					+ "`addr`  TEXT  NOT NULL PRIMARY KEY,"
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

		// Initialize wallets
		getFeesCollectorWallet();
		getWelcomeFundsWallet();
		getGenericWallet();
	
	}
	
	public long getWelcomeTip(){
		return WELCOME_TIP;
	}
	
	public Fees getFees(){
		return FEES;
	}
	
	@Override
	public OffchainWallet getFeesCollectorWallet() throws SQLException{
		return getOffchainWallet(FEES_WALLET);
	}
	
	@Override
	public OffchainWallet getWelcomeFundsWallet() throws SQLException{
		return getOffchainWallet(WELCOME_WALLET);
	}

	@Override
	public OffchainWallet getGenericWallet() throws SQLException{
		return getOffchainWallet(GENERIC_WALLET);
	}
	
	protected boolean isAServerWallet(String wallid){
		return (wallid.equals(GENERIC_WALLET)||
			wallid.equals(WELCOME_WALLET)
			||wallid.equals(FEES_WALLET)
				);
	}
	

	
	
	@Override
	public synchronized OffchainWallet getOffchainWallet(String id) throws SQLException{
		id=id.replaceAll("[^A-Za-z0-9_\\-]","_");
		OffchainWallet out=new  OffchainWallet();
		out.id=id;
		SQLResult res=query("SELECT "
				+ "`status`,`balance` "
				+ "FROM `reddconomy_wallets` WHERE `id`='"+id+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,Object> fetched=res.fetchAssoc();
			out.status=((Number)fetched.get("status")).byteValue();
			out.balance=((Number)fetched.get("balance")).longValue();
		}else{ // NEW WALLET
			query("INSERT INTO reddconomy_wallets(`id`) VALUES('"+id+"')",false,false);
			
			if(!isAServerWallet(id)&&getWelcomeTip()>0){
				// Add welcome funds
				try{
					OffchainContract contract=createContract(getWelcomeFundsWallet().id,WELCOME_TIP);
					acceptContract(contract.id,id);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return out;
	}
	
	
	
	@Override
	public synchronized OffchainContract getContract(String id) throws SQLException{
		id=id.replaceAll("[^A-Za-z0-9]","_");
		OffchainContract out=new OffchainContract();
		SQLResult res=query("SELECT * FROM `reddconomy_contracts` WHERE `id`='"+id+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,Object> fetched=res.fetchAssoc();
			System.out.println(fetched);
			out.id=fetched.get("id").toString();
			out.amount=((Number)fetched.get("amount")).longValue();
			out.acceptedby=fetched.get("acceptedby").toString();
			out.createdby=fetched.get("createdby").toString();
			out.created=((Number)fetched.get("created")).longValue();
			out.paid_in_fees=((Number)fetched.get("paid_in_fees")).longValue();
			out.accepted=((Number)fetched.get("accepted")).longValue();
			return out;
		}else{
			return null;
		}
	}
	

	@Override
	public synchronized OffchainContract createContract(String walletid,long amount) throws SQLException{
		OffchainContract out=new OffchainContract();
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		String id="0c"+(System.currentTimeMillis()+"___"+walletid+"__"+amount).hashCode();
		id=id.replaceAll("[^A-Za-z0-9]","_");
		
		out.createdby=walletid;
		out.amount=amount;
		out.id=id;
		

		query("INSERT INTO  reddconomy_contracts(`id`,`createdby`,`amount`, `created`) VALUES('"+id+"','"+walletid+"','"+amount+"','"+System.currentTimeMillis()+"')",false,false);			
		return out;
	}
	

	@Override
	public synchronized OffchainContract acceptContract(String contractid, String acceptedby_walletid) throws Exception {
		acceptedby_walletid=acceptedby_walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		contractid=contractid.replaceAll("[^A-Za-z0-9]","_");
		OffchainContract contract=getContract(contractid);
		if(contract!=null){
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
					if(acceptedby_balance>=amount){
						acceptedby_balance-=amount;
						
						// Fee 
						long net=FEES.getTransactionFee().apply(amount);
						tare=amount-net;
						if(!isAServerWallet(acceptedby_walletid)&&!isAServerWallet(createdby_walletid)){							
							OffchainWallet fees_wallet=getFeesCollectorWallet();
							fees_wallet.balance+=tare;
							query("UPDATE reddconomy_wallets SET "
									+ "`balance`='"+fees_wallet.balance+"' WHERE `id`='"+fees_wallet.id+"'",false,false);
						}
						amount=net;
						//
						
						createdby_balance+=amount;
						query("UPDATE reddconomy_wallets SET `balance`='"+acceptedby_balance+"' WHERE "
								+ "`id`='"+acceptedby_walletid+"'",false,false);
						query("UPDATE reddconomy_wallets SET `balance`='"+createdby_balance+"' WHERE "
								+ "`id`='"+createdby_walletid+"'",false,false);
					}					
				}else if(TransactionDirection.get(contract)==TransactionDirection.CREATEDBY2ACCEPTEDBY){
					if(createdby_balance>=amount){
						createdby_balance-=amount;
						
						// Fee 
						long net=FEES.getTransactionFee().apply(amount);
						tare=amount-net;
						if(!isAServerWallet(acceptedby_walletid)&&!isAServerWallet(createdby_walletid)){							
							OffchainWallet fees_wallet=getFeesCollectorWallet();
							fees_wallet.balance+=tare;
							query("UPDATE reddconomy_wallets SET "
									+ "`balance`='"+fees_wallet.balance+"' WHERE `id`='"+fees_wallet.id+"'",false,false);
						}
						amount=net;
						//

						acceptedby_balance+=amount;
						query("UPDATE reddconomy_wallets SET `balance`='"+acceptedby_balance+"' WHERE "
								+ "`id`='"+acceptedby_walletid+"'",false,false);
						query("UPDATE reddconomy_wallets SET `balance`='"+createdby_balance+"' WHERE "
								+ "`id`='"+createdby_walletid+"'",false,false);
					}
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
	public synchronized Withdraw withdraw(String walletid,long amount) throws Exception{
		long net=FEES.getWithdrawFee().apply(amount);
		long tare=amount-net;
		
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		OffchainWallet wallet=getOffchainWallet(walletid);
		long balance=wallet.balance;
		if(balance>=amount){
			balance-=amount;
			query("UPDATE reddconomy_wallets SET `balance`='"+balance+"' WHERE `id`='"+walletid+"'",false,false);
			
			if(!isAServerWallet(walletid)){
				OffchainWallet fees_wallet=this.getFeesCollectorWallet();
				fees_wallet.balance+=tare;
				query("UPDATE reddconomy_wallets SET `balance`='"+fees_wallet.balance+"' WHERE `id`='"+fees_wallet.id+"'",false,false);
				amount=net;
			}else{
				tare=0;
			}
			
		}else{
			throw new Exception("Requested withdraw of "+amount+" but only "+balance+" available");
		}
		
		Withdraw wt=new Withdraw();
		wt.amount=amount;
		wt.paid_in_fees=tare;
		wt.from_wallet=walletid;
		return wt;
	}
	
	

	
	
	@Override
	public synchronized Deposit prepareForDeposit(String deposit_addr,String walletid,long expected_balance)throws Exception{
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		deposit_addr=deposit_addr.replaceAll("[^A-Za-z0-9]","_");
		SQLResult res=query("SELECT * FROM `reddconomy_deposits` WHERE `addr`='"+deposit_addr+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			throw new Exception("Cannot reuse deposit addresses");
		}else
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
				
				OffchainWallet fees_wallet=this.getFeesCollectorWallet();
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

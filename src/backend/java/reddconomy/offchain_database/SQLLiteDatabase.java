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
package reddconomy.offchain_database;


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


/**
 * Test DB implementation on SQLITE - >>>> DO NOT USE THIS FOR ANYTHING OTHER THAN TESTING <<<< REALLY!
 * @author Riccardo Balbo
 */

public  class SQLLiteDatabase implements Database {
	protected Connection CONNECTION;

	/**
	 * 
	 * @param path File in cui salvare il db (es. db.sqlite)
	 * @throws Exception
	 */
	public SQLLiteDatabase(String path) throws Exception{
		Class.forName("org.sqlite.JDBC");
		CONNECTION=DriverManager.getConnection("jdbc:sqlite:"+path);
	}

	
	@Override
	public synchronized void open() throws SQLException{
		
		// Inizializza db
		SQLResult q;
		
		q=query("SELECT * FROM `reddconomy_wallets`",true,true);
		if(q==null){
			/*
			 * id (string, walletid (univoco)) 
			 * balance (int, schei)
			 * status (1=attivo,0=disattivo)
			 * expiring_time: tempo in ms al termine del quale l'account verrà eliminato ma solo se balance=0 e non ha mai accettato nessun contratto, ne fatto alcun deposito o ritiro
			 */
			query("CREATE TABLE `reddconomy_wallets` ( `id`  TEXT NOT NULL PRIMARY KEY,`balance` INTEGER DEFAULT 0, `status` INTEGER DEFAULT 1 , `expiring_time` INTEGER DEFAULT 172800000 );",false,false);
		}
		
		q=query("SELECT * FROM `reddconomy_contracts`",true,true);
		if(q==null){
			/*
			 * id : id del contratto (univoco)
			 * receiver: beneficiario del contratto 
			 * amount: $$ (i soldi si muovono da acceptor a receiver se + o il contrario se -)
			 * acceptor : id di chi ha accettato, se vuoto = contratto non completato 
			 * created: data in cui è stato creato
			 * accepted: data in cui è stato accettato 
			 */
			query("CREATE TABLE `reddconomy_contracts` ( `id`  TEXT NOT NULL PRIMARY KEY,"
					+ "`receiver` TEXT NOT NULL, "
					+ "`amount` INTEGER DEFAULT 0, "
					+ "`acceptor` TEXT DEFAULT '',"
					+ "`created` INTEGER NOT NULL,"
					+ "`accepted`  INTEGER NOT NULL DEFAULT -1,"
					+ "FOREIGN KEY(`receiver`) REFERENCES reddconomy_wallets(`id`), "
					+ "FOREIGN KEY(`acceptor`) REFERENCES reddconomy_wallets(`id`) "
					+ ");"
			,false,false);
		}
		
		q=query("SELECT * FROM `reddconomy_deposits`",true,true);
		if(q==null){
			/**
			 * addr (indirizzo redd su cui ricevere (univoco))
			 * receiver (chi lo riceve)
			 * expected_balance (quanti schei voi)
			 * status (1= pending, 0=completed, -1=expired),
			 * created (datetime, quando è stato creato)
			 * expiring (validità in ms che diminuisce con il passare del tempo, quando 0 -> status viene settato a -1)
			 * 
			 */
			query("CREATE TABLE `reddconomy_deposits` ( `addr`  TEXT  NOT NULL PRIMARY KEY,"
					+ "`receiver` TEXT NOT NULL, "
					+ "`expected_balance` INTEGER DEFAULT 0, "
					+ "`status` INTEGER DEFAULT 1,"
					+ "`created` INTEGER NOT NULL,"
					+ "`expiring` INTEGER DEFAULT 172800000, "
					+ "FOREIGN KEY(`receiver`) REFERENCES reddconomy_wallets(`id`) "
					+ ");"
			,false,false);

		}

		q=query("SELECT * FROM `reddconomy_logs`",true,true);
		if(q==null){
			// WIP
		}

	}
	
	
	@Override
	public synchronized OffchainWallet getOffchainWallet(String id) throws SQLException{
		id=id.replaceAll("[^A-Za-z0-9_\\-]","_");
		OffchainWallet out=new  OffchainWallet();
		out.id=id;
		SQLResult res=query("SELECT `status`,`balance` FROM `reddconomy_wallets` WHERE `id`='"+id+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,Object> fetched=res.fetchAssoc();
			out.status=((Number)fetched.get("status")).byteValue();
			out.balance=((Number)fetched.get("balance")).longValue();
		}else{
			query("INSERT INTO reddconomy_wallets(`id`) VALUES('"+id+"')",false,false);
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
			out.acceptedby=fetched.get("acceptor").toString();
			out.createdby=fetched.get("receiver").toString();
			out.created=((Number)fetched.get("created")).longValue();
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

		query("INSERT INTO  reddconomy_contracts(`id`,`receiver`,`amount`, `created`) VALUES('"+id+"','"+walletid+"','"+amount+"','"+System.currentTimeMillis()+"')",false,false);			
		return out;
	}
	

	@Override
	public synchronized OffchainContract acceptContract(String contractid, String walletid) throws Exception {
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		contractid=contractid.replaceAll("[^A-Za-z0-9]","_");
		OffchainContract contract=getContract(contractid);
		if(contract!=null){
			String accp=contract.acceptedby;
			if(accp==null||accp.isEmpty()){
				OffchainWallet acc_wallet=getOffchainWallet(walletid);
				String rcv_walletid=contract.createdby;
				OffchainWallet rcv_wallet=getOffchainWallet(rcv_walletid);

				long price=contract.amount;
				long acc_balance=acc_wallet.balance;
				long rcv_balance=rcv_wallet.balance;
				if((price>=0&&price<=acc_balance)||(price<0&&-price<=rcv_balance)){
					acc_balance-=price;
					rcv_balance+=price;
					query("UPDATE reddconomy_wallets SET `balance`='"+acc_balance+"' WHERE `id`='"+walletid+"'",false,false);
					query("UPDATE reddconomy_wallets SET `balance`='"+rcv_balance+"' WHERE `id`='"+rcv_walletid+"'",false,false);
					query("UPDATE reddconomy_contracts SET `accepted`= '"+System.currentTimeMillis()+"' ,`acceptor`='"+walletid+"' WHERE `id`='"+contractid+"'",false,false);
				}else{
					if(price>=0)throw new Exception("Not enough money. Wallet id "+walletid+" has balance "+acc_balance+" but contract requires "+price);
					else throw new Exception("Not enough money. Wallet id "+rcv_walletid+" has balance "+rcv_balance+" but contract requires "+price);
				}
					
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
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		OffchainWallet wallet=getOffchainWallet(walletid);
		long balance=wallet.balance;
		if(balance>=amount){
			balance-=amount;
			query("UPDATE reddconomy_wallets SET `balance`='"+balance+"' WHERE `id`='"+walletid+"'",false,false);
		}else{
			throw new Exception("Requested withdraw of "+amount+" but only "+balance+" available");
		}
		Withdraw wt=new Withdraw();
		wt.amount=amount;
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
			query("UPDATE reddconomy_deposits SET `status`='0' WHERE `addr`='"+deposit_addr+"'",false,false);
			
			Map<String,Object> deposit=res.fetchAssoc();
			long amount=((Number)deposit.get("expected_balance")).longValue();
			String wallet_id=deposit.get("receiver").toString();
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
		}
		return resp;
	}
	
	/**
	 * Query sql
	 * @param q query
	 * @param expectoutput true se la query si aspetta un output
	 * @param noerror se true, gli errori vengono totalmente ignorati
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

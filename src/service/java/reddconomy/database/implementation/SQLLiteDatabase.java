package reddconomy.database.implementation;


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
import java.util.concurrent.CopyOnWriteArrayList;

import reddconomy.database.Database;


/**
 * DB implementation on SQLITE - NOT SAFE FOR PRODUCTION
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
					+ "`created` DATETIME DEFAULT CURRENT_TIMESTAMP,"
					+ "`accepted` DATETIME DEFAULT CURRENT_TIMESTAMP ,"
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
					+ "`created` DATETIME DEFAULT CURRENT_TIMESTAMP,"
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
	public synchronized Map<String,Object> getWallet(String id) throws SQLException{
		id=id.replaceAll("[^A-Za-z0-9_\\-]","_");
		Map<String,Object> out=new HashMap<String,Object>();
		out.put("id",id);
		SQLResult res=query("SELECT `status`,`balance` FROM `reddconomy_wallets` WHERE `id`='"+id+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,String> fetched=res.FetchAssoc();
			out.put("status",Integer.parseInt(fetched.get("status")));
			out.put("balance",Long.parseLong(fetched.get("balance")));
		}else{
			query("INSERT INTO reddconomy_wallets(`id`) VALUES('"+id+"')",false,false);
			out.put("status",1);
			out.put("balance",0l);
		}
		return out;
	}
	
	
	
	@Override
	public synchronized Map<String,Object> getContract(String id) throws SQLException{
		id=id.replaceAll("[^A-Za-z0-9]","_");
		Map<String,Object> out=new HashMap<String,Object>();
		SQLResult res=query("SELECT * FROM `reddconomy_contracts` WHERE `id`='"+id+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,String> fetched=res.FetchAssoc();
			System.out.println(fetched);
			out.put("id",fetched.get("id"));
			out.put("amount",Long.parseLong(fetched.get("amount")));
			out.put("acceptor",fetched.get("acceptor"));
			out.put("receiver",fetched.get("receiver"));
			out.put("created",fetched.get("created"));
			out.put("accepted",fetched.get("accepted"));
			return out;
		}else{
			return null;
		}
	}
	

	@Override
	public synchronized String createContract(String walletid,long amount) throws SQLException{
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		String id="0c"+(System.currentTimeMillis()+"___"+walletid+"__"+amount).hashCode();
		id=id.replaceAll("[^A-Za-z0-9]","_");

		query("INSERT INTO  reddconomy_contracts(`id`,`receiver`,`amount`) VALUES('"+id+"','"+walletid+"','"+amount+"')",false,false);			
		return id;
	}
	

	@Override
	public synchronized void acceptContract(String contractid, String walletid) throws Exception {
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		contractid=contractid.replaceAll("[^A-Za-z0-9]","_");
		Map<String,Object> contract=getContract(contractid);
		if(contract!=null){
			String accp=(String)contract.get("acceptor");
			if(accp==null||accp.isEmpty()){
				Map<String,Object> acc_wallet=getWallet(walletid);
				String rcv_walletid=contract.get("receiver").toString();
				Map<String,Object> rcv_wallet=getWallet(rcv_walletid);

				long price=(long)contract.get("amount");
				long acc_balance=(long)acc_wallet.get("balance");
				long rcv_balance=(long)rcv_wallet.get("balance");
				if((price>=0&&price<=acc_balance)||(price<0&&-price<=rcv_balance)){
					acc_balance-=price;
					rcv_balance+=price;
					query("UPDATE reddconomy_wallets SET `balance`='"+acc_balance+"' WHERE `id`='"+walletid+"'",false,false);
					query("UPDATE reddconomy_wallets SET `balance`='"+rcv_balance+"' WHERE `id`='"+rcv_walletid+"'",false,false);
					query("UPDATE reddconomy_contracts SET `accepted`= CURRENT_TIMESTAMP ,`acceptor`='"+walletid+"' WHERE `id`='"+contractid+"'",false,false);
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
	}
	

	@Override
	public synchronized void withdraw(String walletid,long amount) throws Exception{
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		Map<String,Object> wallet=getWallet(walletid);
		long balance=(long)wallet.get("balance");
		if(balance>=amount){
			balance-=amount;
			query("UPDATE reddconomy_wallets SET `balance`='"+balance+"' WHERE `id`='"+walletid+"'",false,false);
		}else{
			throw new Exception("Requested withdraw of "+amount+" but only "+balance+" available");
		}
	}
	
	

	
	
	@Override
	public synchronized void prepareForDeposit(String deposit_addr,String walletid,long expected_balance)throws Exception{
		walletid=walletid.replaceAll("[^A-Za-z0-9_\\-]","_");
		deposit_addr=deposit_addr.replaceAll("[^A-Za-z0-9]","_");
		SQLResult res=query("SELECT * FROM `reddconomy_deposits` WHERE `addr`='"+deposit_addr+"' LIMIT 0,1",true,false);
		if(res!=null&&!res.isEmpty()){
			throw new Exception("Cannot reuse deposit addresses");
		}else
			query("INSERT INTO reddconomy_deposits(`addr`,`receiver`,`expected_balance`) VALUES('"+deposit_addr+"','"+walletid+"','"+expected_balance+"')",false,false);
	}
	

	@Override
	public synchronized Collection<Map<String,Object>> getIncompletedDepositsAndUpdate(long tms) throws Exception {
		ArrayList<Map<String,Object>> out=new ArrayList<Map<String,Object>>();
		SQLResult res=query("SELECT * FROM `reddconomy_deposits` WHERE `status`='1'",true,false);
		if(res!=null&&!res.isEmpty()){
			Map<String,String> fetch;
			while(!(fetch=res.FetchAssoc()).isEmpty()){
				

				String addr=fetch.get("addr");
				int status=Integer.parseInt(fetch.get("status"));
				long exp=Integer.parseInt(fetch.get("expiring"));

		
				
				exp-=tms;
				if(exp<=0){
					status=-1;
				}else{
					Map<String,Object> deposit=new HashMap<String,Object>();
					deposit.put("addr",addr);
					deposit.put("receiver",fetch.get("receiver"));
					deposit.put("expected_balance",Long.parseLong(fetch.get("expected_balance")));
					deposit.put("status",status);
					deposit.put("created",fetch.get("created"));
					deposit.put("expiring",exp);
					out.add(deposit);
				}
				
				query("UPDATE reddconomy_deposits SET `expiring`='"+exp+"',`status`='"+status+"' WHERE `addr`='"+addr+"'",false,false);

			}
		}
		return out;
	}
	
	
	@Override
	public synchronized void completeDeposit(String deposit_addr) throws Exception{
		deposit_addr=deposit_addr.replaceAll("[^A-Za-z0-9]","_");
		SQLResult res=query("SELECT `receiver`,`expected_balance` FROM `reddconomy_deposits` WHERE `addr`='"+deposit_addr+"' LIMIT 0,1",true,false);
		if(res==null){
			throw new Exception("Deposit is not ready");
		}else{
			query("UPDATE reddconomy_deposits SET `status`='0' WHERE `addr`='"+deposit_addr+"'",false,false);
			
			Map<String,String> deposit=res.FetchAssoc();
			long amount=Long.parseLong(deposit.get("expected_balance"));
			String wallet_id=deposit.get("receiver");
			Map<String,Object> wallet=this.getWallet(wallet_id);
			long balance=(long)wallet.get("balance");
			balance+=amount;
			query("UPDATE reddconomy_wallets SET `balance`='"+balance+"' WHERE `id`='"+wallet_id+"'",false,false);
		}

	}
	
	@Override
	public synchronized Map<String,Object> getDeposit(String deposit_addr) throws Exception{
		Map<String,Object> resp=new HashMap<String,Object>();
		deposit_addr=deposit_addr.replaceAll("[^A-Za-z0-9]","_");
		SQLResult res=query("SELECT * FROM `reddconomy_deposits` WHERE `addr`='"+deposit_addr+"' LIMIT 0,1",true,false);
		if(res==null){
			throw new Exception("Deposit is not ready");
		}else{		
			Map<String,String> deposit=res.FetchAssoc();
			resp.put("addr",deposit_addr);
			resp.put("receiver",deposit.get("receiver"));
			resp.put("expected_balance",Long.parseLong(deposit.get("expected_balance")));
			resp.put("status",Integer.parseInt(deposit.get("status")));
			resp.put("created",(deposit.get("created")));
			resp.put("expiring",Integer.parseInt(deposit.get("status")));
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

package reddconomy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reddconomy.database.Database;
import reddconomy.database.implementation.SQLLiteDatabase;

public class Reddconomy extends Thread implements ActionListener{

	private final Database _DATABASE;
	private final Gson _JSON;
	private CentralWallet _WALLET;
	private boolean CLOSED=false;

	public static void main(String[] args) throws Throwable {
		int port=8099;
		String ip="0.0.0.0";
		
		CentralWallet wallet=new Reddcoind("http://reddconomy.frk.wf:45443/","test","test123");
		Reddconomy reddconomy=new Reddconomy(wallet);
		HttpGateway httpd=new HttpGateway(ip,port,reddconomy);
		httpd.start();
		reddconomy.start();
		
		System.out.println("Server started @ "+ip+":"+port);

	}

	public Reddconomy(CentralWallet wallet) throws Exception{
		_JSON=new GsonBuilder().setPrettyPrinting().create();
		_WALLET=wallet;

		// Init LocalDB
		_DATABASE=new SQLLiteDatabase("db.sqlite");
		_DATABASE.open();

		setDaemon(true);

	}

	public synchronized void stopNow() {
		CLOSED=true;
	}

	public synchronized void run() {
		long t=0;
		while(!CLOSED){
			long delta_t=t==0?0:System.currentTimeMillis()-t;
			try{
				Collection<Map<String,Object>> deposits=_DATABASE.getIncompletedDepositsAndUpdate(delta_t);
				for(Map<String,Object> deposit:deposits){
					String addr=deposit.get("addr").toString();
					long expected_balance=(long)deposit.get("expected_balance");
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

	public synchronized String performAction(String action, Map<String,String> _GET) {
		Map<String,Object> resp_obj=new HashMap<String,Object>();
		switch(action){
			// action=deposit&wallid=XXX&ammount=XXXX
			case "deposit":{
				try{
					Map<String,Object> data=new HashMap<String,Object>();
					String addr=_WALLET.getNewAddress();
					data.put("addr",addr);
					resp_obj.put("status",200);
					resp_obj.put("data",data);
					String wallet_id=_GET.get("wallid").toString();
					long balance=Long.parseLong(_GET.get("ammount").toString());
					_DATABASE.prepareForDeposit(addr,wallet_id,balance);
				}catch(Throwable e){
					String error=e.toString();
					resp_obj.put("status",500);
					resp_obj.put("error",error);
					e.printStackTrace();
				}
				break;
			}
			//action=withdraw&ammount=XXXX&addr=XXXXXXXx
			case "withdraw":{
				long ammount=Long.parseLong(_GET.get("ammount").toString());
				String addr=(String)_GET.get("addr");
				String wallet_id=_GET.get("wallid").toString();
				try{
					Map<String,Object> wallet=_DATABASE.getWallet(wallet_id);
					long balance=(long)wallet.get("balance");
					if(balance>=ammount){
						_WALLET.sendToAddress(addr,ammount);
						_DATABASE.withdraw(wallet_id,ammount);
						resp_obj.put("status",200);
						resp_obj.put("data",new HashMap<String,Object>());
					}else{
						resp_obj.put("status",500);
						resp_obj.put("error","Not enought money");
					}
				}catch(Throwable e){
					String error=e.toString();
					resp_obj.put("status",500);
					resp_obj.put("error",error);
					e.printStackTrace();
				}
				break;
			}
			// action=balance&wallid=XXX
			case "balance":{
				try{
					String wallet_id=_GET.get("wallid").toString();
					Map<String,Object> data=_DATABASE.getWallet(wallet_id);
					resp_obj.put("status",200);
					resp_obj.put("data",data);
				}catch(Throwable e){
					String error=e.toString();
					resp_obj.put("status",500);
					resp_obj.put("error",error);
					e.printStackTrace();
				}

				break;
			}
			// action=newcontract&wallid=XXX&ammount=XXXX
			case "newcontract":{
				try{
					Map<String,Object> data=new HashMap<String,Object>();
					String wallet_id=_GET.get("wallid").toString();
					long ammount=Long.parseLong(_GET.get("ammount").toString());
					String contractId=_DATABASE.createContract(wallet_id,ammount);
					data.put("contractId",contractId);
					resp_obj.put("status",200);
					resp_obj.put("data",data);
				}catch(Throwable e){
					String error=e.toString();
					resp_obj.put("status",500);
					resp_obj.put("error",error);
					e.printStackTrace();
				}

				break;
			}
			// action=acceptcontract&wallid=XXXX&contractid=XXXX
			case "acceptcontract":{
				try{
					String wallet_id=_GET.get("wallid").toString();
					String contractId=_GET.get("contractid").toString();
					_DATABASE.acceptContract(contractId,wallet_id);
					resp_obj.put("status",200);
					resp_obj.put("data",new HashMap<String,Object>());
				}catch(Throwable e){
					String error=e.toString();
					resp_obj.put("status",500);
					resp_obj.put("error",error);
					e.printStackTrace();
				}

				break;
			}
			case "sendcoins":{
				try{
					String addr=_GET.get("addr").toString();
					long ammount=Long.parseLong(_GET.get("ammount").toString());
					_WALLET.sendToAddress(addr,ammount);
					resp_obj.put("status",200);
					resp_obj.put("data",new HashMap<String,Object>());
				}catch(Throwable e){
					String error=e.toString();
					resp_obj.put("status",500);
					resp_obj.put("error",error);
					e.printStackTrace();
				}
				break;
			}
			// action=getcontract&contractid=XXXX
			case "getcontract":{
				try{
					String contractId=_GET.get("contractid").toString();
					Map<String,Object> contract=_DATABASE.getContract(contractId);
					resp_obj.put("status",200);
					resp_obj.put("data",contract);
				}catch(Throwable e){
					String error=e.toString();
					resp_obj.put("status",500);
					resp_obj.put("error",error);
					e.printStackTrace();
				}
				break;
			}
			default:
				resp_obj.put("status",500);
				resp_obj.put("error","Invalid action");
		}
		return _JSON.toJson(resp_obj);

	}
}

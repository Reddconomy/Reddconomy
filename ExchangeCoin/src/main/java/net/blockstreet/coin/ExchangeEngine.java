package net.blockstreet.coin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExchangeEngine {
		
		final Gson _JSON;
		public ExchangeEngine() {
			_JSON = new GsonBuilder().setPrettyPrinting().create();
		}
		
		// Le api fondamentali per interfacciarsi a Reddconomy.
		@SuppressWarnings("rawtypes")
		public Map apiCall(String action) throws Exception
		{
			  String urlString = "http://localhost:8099/?action=" + action;
			  URL url = new URL(urlString);
			  HttpURLConnection con = (HttpURLConnection) url.openConnection();
			  
			  BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			  String output;
			  StringBuffer response = new StringBuffer();
			 
			  while ((output = in.readLine()) != null) {
			   response.append(output);
			  }
			  in.close();
			  
			  Map resp=_JSON.fromJson(response.toString(),Map.class);
			  return resp;
		}
		
		public void getTestCoins(String addr, long ammount) throws Exception
		{
			apiCall("gettestcoins&addr=" + addr + "&ammount=" + ammount);
		}
		
		// Ottiene l'indirizzo per il deposito
		@SuppressWarnings("rawtypes")
		public String getAddrDeposit(long balance, String pUUID) throws Exception
		{
			 
			 String action = "deposit&wallid=" + pUUID + "&balance=" + balance;
			 Map data=(Map)apiCall(action).get("data");
			 return (String)data.get("addr");
		}
		
		// Crea contratto
		@SuppressWarnings("rawtypes")
		public String createContract(long ammount, String pUUID) throws Exception
		{
			String action = "newcontract&wallid=" + pUUID + "&ammount=" + ammount;
			Map data=(Map)apiCall(action).get("data");
			return (String)data.get("contractId");
		}
		 
		// Accetta contratto
		public void acceptContract(String contractId, String pUUID) throws Exception
		{
			apiCall("acceptcontract&wallid=" + pUUID + "&contractid=" + contractId);
		}
		 
		// Ottiene saldo di un utente
		@SuppressWarnings("rawtypes")
		public double getBalance(String pUUID) throws Exception
		{
			String action = "balance&wallid=" + pUUID;
			Map data=(Map)apiCall(action).get("data");
			Number balance=(Number)data.get("balance");
			return (balance.longValue())/100000000.0;
		}
}

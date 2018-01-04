package net.reddconomy.plugin;

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
		
		// Fundamental APIs for Reddconomy.
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
		
		// Test, test, test and moar test.
		public void getTestCoins(String addr, long ammount) throws Exception
		{
			apiCall("gettestcoins&addr=" + addr + "&ammount=" + ammount);
		}
		
		// Let's get that deposit address.
		@SuppressWarnings("rawtypes")
		public String getAddrDeposit(long balance, String pUUID) throws Exception
		{
			 
			 String action = "deposit&wallid=" + pUUID + "&balance=" + balance;
			 Map data=(Map)apiCall(action).get("data");
			 return (String)data.get("addr");
		}
		
		// Create contract.
		@SuppressWarnings("rawtypes")
		public String createContract(long ammount, String pUUID) throws Exception
		{
			String action = "newcontract&wallid=" + pUUID + "&ammount=" + ammount;
			Map data=(Map)apiCall(action).get("data");
			return (String)data.get("contractId");
		}
		 
		// Accept contract.
		public void acceptContract(String contractId, String pUUID) throws Exception
		{
			apiCall("acceptcontract&wallid=" + pUUID + "&contractid=" + contractId);
		}
		 
		// Get balance.
		@SuppressWarnings("rawtypes")
		public double getBalance(String pUUID) throws Exception
		{
			String action = "balance&wallid=" + pUUID;
			Map data=(Map)apiCall(action).get("data");
			Number balance=(Number)data.get("balance");
			return (balance.longValue())/100000000.0;
		}
}

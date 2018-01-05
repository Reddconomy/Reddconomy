package net.reddconomy.plugin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.glxn.qrgen.javase.QRCode;

public class ExchangeEngine {
		
		final Gson _JSON;
		public ExchangeEngine() {
			_JSON = new GsonBuilder().setPrettyPrinting().create();
		}
		
		public String coin = "dogecoin";
		
		// Crypto stuff
		public static String hmac(String key, String data) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
	        Mac sha256_HMAC=Mac.getInstance("HmacSHA256");
	        SecretKeySpec secret_key=new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
	        sha256_HMAC.init(secret_key);
	        return new String(Base64.getEncoder().encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))),"UTF-8");
	    }
		
		// Fundamental APIs for Reddconomy.
		@SuppressWarnings("rawtypes")
		public Map apiCall(String action) throws Exception
		{
			  String query = "/?action="+action;
			  String urlString = "http://localhost:8099"+query;
			  URL url = new URL(urlString);
			  String hash = hmac("SECRET123", query);
			  HttpURLConnection httpc=(HttpURLConnection)url.openConnection(); //< la tua connessione
	          httpc.setRequestProperty("Hash",hash);
	          System.out.println(hash);
			  
	          byte chunk[]=new byte[1024*1024];
	          int read;
	          ByteArrayOutputStream bos=new ByteArrayOutputStream();
	          InputStream is=(httpc.getInputStream());
	          
	          while((read=is.read(chunk))!=-1){
	        	  bos.write(chunk,0,read);
	          }
			  
			  is.close();
			  
			  String response=new String(bos.toByteArray(),"UTF-8");
			 
			  Map resp=_JSON.fromJson(response,Map.class);
			  return resp;
		}
		
		// Let's get that deposit address.
		@SuppressWarnings("rawtypes")
		public String getAddrDeposit(long balance, String pUUID) throws Exception
		{
			 
			 String action = "deposit&wallid=" + pUUID + "&ammount=" + balance;
			 Map data=(Map)apiCall(action).get("data");
			 return (String)data.get("addr");
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
		
		// Create contract.
		@SuppressWarnings("rawtypes")
		public String createContract(long ammount, String pUUID) throws Exception
		{
			String action = "newcontract&wallid=" + pUUID + "&ammount=" + ammount;
			Map data=(Map)apiCall(action).get("data");
			return (String)data.get("contractId");
		}
		
		
		
		
		// Get QRCode
		public void getQRCode(String addr)
		{
			QRCode.from("Hello World").file(addr);
			
		}
		 
		// Accept contract.
		public void acceptContract(String contractId, String pUUID) throws Exception
		{
			apiCall("acceptcontract&wallid=" + pUUID + "&contractid=" + contractId);
		}
		
		// Withdraw money
		public void withdraw(long ammount, String addr, String pUUID) throws Exception
		{
			apiCall("withdraw&ammount=" + ammount + "&addr="+addr+"&wallid="+pUUID);
		}
		
		// Test, test, test and moar test.
		public void sendCoins(String addr, long ammount) throws Exception
		{
			apiCall("sendcoins&addr=" + addr + "&ammount=" + ammount);
		}
}

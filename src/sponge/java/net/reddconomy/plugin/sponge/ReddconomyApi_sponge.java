package net.reddconomy.plugin.sponge;

import java.awt.image.BufferedImage;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;

import net.glxn.qrgen.javase.QRCode;

public class ReddconomyApi_sponge {
		
		final Gson _JSON;
		final String _URL;
		
		public ReddconomyApi_sponge(String reddconomy_api_url) {
			_JSON = new GsonBuilder().setPrettyPrinting().create();
			_URL = reddconomy_api_url;
		}
		
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
			  String urlString = _URL+query;
			  URL url = new URL(urlString);
			  String hash = hmac("SECRET123", query);
			  HttpURLConnection httpc=(HttpURLConnection)url.openConnection(); //< la tua connessione
	          httpc.setRequestProperty("Hash",hash);
			  
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
		
		public PendingDepositData_sponge getDepositStatus(String addr) throws Exception
		{
			String action = "getdeposit&addr=" + addr;
			Map data=(Map)apiCall(action).get("data");
			PendingDepositData_sponge output=new PendingDepositData_sponge();
			output.status=((Number) data.get("status")).intValue();
			output.addr=data.get("receiver").toString();
			return output;
		}
		
		// Let's get that deposit address.
		@SuppressWarnings("rawtypes")
		public String getAddrDeposit(long balance, UUID pUUID) throws Exception
		{
			 
			 String action = "deposit&wallid=" + pUUID + "&amount=" + balance;
			 Map data=(Map)apiCall(action).get("data");
			 return (String)data.get("addr");
		}
		
		// Get balance.
		@SuppressWarnings("rawtypes")
		public double getBalance(UUID pUUID) throws Exception
		{
			String action = "balance&wallid=" + pUUID;
			Map data=(Map)apiCall(action).get("data");
			Number balance=(Number)data.get("balance");
			return (balance.longValue())/100000000.0;
		}
		
		// Create contract.
		@SuppressWarnings("rawtypes")
		public String createContract(long amount, UUID pUUID) throws Exception
		{
			String action = "newcontract&wallid=" + pUUID + "&amount=" + amount;
			Map data=(Map)apiCall(action).get("data");
			return (String)data.get("contractId");
		}
		/*
		public void activateRed(Block block)
		{
			switch(block.getType())
			{
			case WOOD_BUTTON:
			case STONE_BUTTON:
			case DISPENSER:
			{
			      block.setData((byte) (block.getData() | 0x8)); // put it back, ready for the next contract.
			      break;
			} }
		}
		
		public void shutdownRed(Block block)
		{
			switch(block.getType())
			{
			case WOOD_BUTTON:
			case STONE_BUTTON:
			case DISPENSER:
			{
			      block.setData((byte) (block.getData() & ~0x8)); // put it back, ready for the next contract.
			      break;
			} }
		}
		
		public void shutdownButton(Block interacted)
		{
			Block blockUP= interacted.getRelative(BlockFace.UP, 1);
			Block blockDOWN= interacted.getRelative(BlockFace.DOWN, 1);
			Block blockLEFT=interacted.getRelative(BlockFace.EAST, 1);
			Block blockRIGHT=interacted.getRelative(BlockFace.WEST, 1);
			Block blockNORTH=interacted.getRelative(BlockFace.NORTH, 1);
			Block blockSOUTH=interacted.getRelative(BlockFace.SOUTH, 1);
			shutdownRed(blockDOWN);
			shutdownRed(blockUP);
			shutdownRed(blockLEFT);
			shutdownRed(blockRIGHT);
			shutdownRed(blockSOUTH);
			shutdownRed(blockNORTH);
		}
		
		public void powerButton(Block interacted)
		{
			Block blockUP= interacted.getRelative(BlockFace.UP, 1);
			Block blockDOWN= interacted.getRelative(BlockFace.DOWN, 1);
			Block blockLEFT=interacted.getRelative(BlockFace.EAST, 1);
			Block blockRIGHT=interacted.getRelative(BlockFace.WEST, 1);
			Block blockNORTH=interacted.getRelative(BlockFace.NORTH, 1);
			Block blockSOUTH=interacted.getRelative(BlockFace.SOUTH, 1);
			activateRed(blockDOWN);
			activateRed(blockUP);
			activateRed(blockLEFT);
			activateRed(blockRIGHT);
			activateRed(blockSOUTH);
			activateRed(blockNORTH);
		}
		
		public BufferedImage QR (String addr, String coin, String amount) throws WriterException
		{
			Map<EncodeHintType,Object> hint=new HashMap<EncodeHintType,Object>();
			com.google.zxing.qrcode.encoder.QRCode code=Encoder.encode(coin!=null&&!coin.isEmpty()?
					coin+":"+addr+"?amount="+amount:amount,ErrorCorrectionLevel.L,hint);
			ByteMatrix matrix=code.getMatrix();
			System.out.println(matrix.getWidth()+"x"+matrix.getHeight());
			BufferedImage bimg=new BufferedImage(matrix.getWidth(),matrix.getHeight(),BufferedImage.TYPE_INT_RGB);
			for(int y=0;y<matrix.getHeight();y++){
				for(int x=0;x<matrix.getWidth();x++){
					boolean v=matrix.get(x,y)==0;
					bimg.setRGB(x,y,v?0xFFFFFF:0x000000);
				}
			}	
			return bimg;
		}
		 */
		// Accept contract.
		public int acceptContract(String contractId, UUID pUUID) throws Exception
		{
			String action = "acceptcontract&wallid=" + pUUID + "&contractid=" + contractId;
			Number status=(Number)apiCall(action).get("status");
			return status.intValue();
		}
		
		// Withdraw money
		public void withdraw(long amount, String addr, UUID pUUID) throws Exception
		{
			apiCall("withdraw&amount=" + amount + "&addr="+addr+"&wallid="+pUUID);
		}
		
		// Test, test, test and moar test.
		public void sendCoins(String addr, long amount) throws Exception
		{
			apiCall("sendcoins&addr=" + addr + "&amount=" + amount);
		}
}

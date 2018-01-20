/*
 * Copyright (c) 2018, Simone Cervino.
 * 
 * This file is part of Reddconomy-sponge.

    Reddconomy-sponge is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Reddconomy-sponge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Reddconomy-sponge.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.reddconomy.plugin;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRedstonePoweredData;
import org.spongepowered.api.data.manipulator.mutable.block.RedstonePoweredData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;

import net.glxn.qrgen.javase.QRCode;
import reddconomy.api.ApiResponse;
import reddconomy.data.Data;
import reddconomy.data.Deposit;
import reddconomy.data.OffchainContract;
import reddconomy.data.OffchainWallet;

public class ReddconomyApi {
		
		final Gson _JSON;
		final String _URL;
		final String _SECRET;
		
		public ReddconomyApi(String apiUrl, String secret) {
			_JSON = new GsonBuilder().setPrettyPrinting().create();
			_URL = apiUrl;
			_SECRET = secret;
			ApiResponse.registerAll("v1");
		}
		
		// Fundamental APIs for Reddconomy.
		@SuppressWarnings("rawtypes")
		public ApiResponse apiCall(String action) throws Exception
		{
			  String version="v1";
			  String query = "/"+version+"/?action="+action;
			  String urlString = _URL+query;
			  URL url = new URL(urlString);
			  System.out.println("SECRET KEY: "+_SECRET);
			  String hash = Utils.hmac(_SECRET, query);
			  System.out.println("Hash: "+hash);
			  HttpURLConnection httpc=(HttpURLConnection)url.openConnection();
	          httpc.setRequestProperty("Hash",hash);
			  //System.out.println(url); // only for debug
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
			  return ApiResponse.build().fromMap(resp);
		}
		
		// Let's get that deposit address.
		@SuppressWarnings("rawtypes")
		public String getAddrDeposit(long balance, UUID pUUID) throws Exception
		{
			 
			 String action = "deposit&wallid=" + pUUID + "&amount=" + balance;
			 ApiResponse r=apiCall(action);
			 if (r.statusCode()==200)
			 {
				 Deposit deposit = r.data();
				 String addr = deposit.addr;
				 return addr;
			 } else return null;
		}
		
		public String srvDeposit(long balance) throws Exception
		{
			String action = "deposit&wallid=[SRV]&amount=" + balance;
			 ApiResponse r=apiCall(action);
			 if (r.statusCode()==200)
			 {
				 Deposit deposit = r.data();
				 String addr = deposit.addr;
				 return addr;
			 } else  return null;
		}
		
		// Checking deposit status
		public PendingDepositData getDepositStatus(String addr) throws Exception
		{
			String action = "getdeposit&addr=" + addr;
			ApiResponse r=apiCall(action);
			Deposit deposit = r.data();
			PendingDepositData output=new PendingDepositData();
			output.status=deposit.status;
			output.addr=deposit.receiver_wallet_id;
			return output;
		}
		
		
		// Get balance.
		@SuppressWarnings("rawtypes")
		public double getBalance(UUID pUUID) throws Exception
		{
			String action = "getwallet&wallid=" + pUUID;
			ApiResponse r=apiCall(action);
			if(r.statusCode()==200)
			{
				OffchainWallet wallet=r.data();
				
				long balance=wallet.balance;
				return balance/100000000.;
			} else return -1;
		}
		
		// Create contract.
		@SuppressWarnings("rawtypes")
		public String createContract(long amount, UUID pUUID) throws Exception
		{
			String action = "newcontract&wallid=" + pUUID + "&amount=" + amount;
			ApiResponse r=apiCall(action);
			if(r.statusCode()==200)
			{
				OffchainContract contract=r.data();
				String cId=contract.id;
				return cId;
			} else  return null;
		}
		
		public String createTipContract(long amount) throws Exception
		{
			String action = "newcontract&wallid=[TIPS]" + "&amount=" + amount;
			ApiResponse r=apiCall(action);
			if (r.statusCode()==200)
			{
				OffchainContract contract = r.data();
				String cId = contract.id;
				return cId;
			} else return null;
		}
		
		// Hard-coded wallet for the server
		public String createServerContract(long amount) throws Exception
		{
			String action = "newcontract&wallid=[SRV]" + "&amount=" + amount;
			ApiResponse r=apiCall(action);
			if(r.statusCode()==200)
			{
				OffchainContract contract=r.data();
				String cId=contract.id;
				return cId;
			} else  return null;
		}
		
		// QR Engine
		public String createQR (String addr, String coin, double amount) throws WriterException
		{
			Map<EncodeHintType,Object> hint=new HashMap<EncodeHintType,Object>();
			com.google.zxing.qrcode.encoder.QRCode code=Encoder.encode(coin!=null&&!coin.isEmpty()?
					coin+":"+addr+"?amount="+amount:""+amount,ErrorCorrectionLevel.L,hint);
			ByteMatrix matrix=code.getMatrix();
			System.out.println(matrix.getWidth()+"x"+matrix.getHeight());
			StringBuilder qr=new StringBuilder();
			for(int y=0;y<matrix.getHeight();y++){
				for(int x=0;x<matrix.getWidth();x++){
					if(matrix.get(x,y)==0)qr.append("\u00A7f\u2588");
                    else qr.append("\u00A70\u2588");

				}
				qr.append("\n");
			}
			return qr.toString();
		}
		 
		// Accept contract.
		public int acceptContract(String contractId, UUID pUUID) throws Exception
		{
			String action = "acceptcontract&wallid=" + pUUID + "&contractid=" + contractId;
			ApiResponse r=apiCall(action);
			return r.statusCode();
		}
		
		// Withdraw money
		public int withdraw(long amount, String addr, UUID pUUID) throws Exception
		{
			String action = "withdraw&amount=" + amount + "&addr="+addr+"&wallid="+pUUID;
			ApiResponse r=apiCall(action);
			return r.statusCode();
		}
		
		// Tips withdraw
		public int tipWithdraw(long amount, String addr, String wallid) throws Exception
		{
			String action = "withdraw&amount=" + amount + "&addr=" + addr + "&wallid=" + wallid;
			ApiResponse r=apiCall(action);
			return r.statusCode();
		}
		
		// Test, test, test and moar test.
		public int sendCoins(String addr, long amount) throws Exception
		{
			String action = "sendcoins&addr=" + addr + "&amount=" + amount;
			ApiResponse r=apiCall(action);
			return r.statusCode();
		}
}

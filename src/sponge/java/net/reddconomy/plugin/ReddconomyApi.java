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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;

import reddconomy.api.ApiResponse;
import reddconomy.data.Deposit;
import reddconomy.data.Info;
import reddconomy.data.OffchainContract;
import reddconomy.data.OffchainWallet;

public class ReddconomyApi{

	private static final Gson _JSON=new GsonBuilder().setPrettyPrinting().create();
	private static  String URL;
	private static  String SECRET;
	private static Info INFO;

	public static void init(String apiUrl,String secret){
		URL=apiUrl;
		SECRET=secret;
		ApiResponse.registerAll("v1");
	}

	// Fundamental APIs for Reddconomy.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static  ApiResponse apiCall(String action) throws Exception {
		String version="v1";
		String query="/"+version+"/?action="+action;
		String urlString=URL+query;
		URL url=new URL(urlString);
		System.out.println("SECRET KEY: "+SECRET);
		String hash=Utils.hmac(SECRET,query);
		System.out.println("Hash: "+hash);
		HttpURLConnection httpc=(HttpURLConnection)url.openConnection();
		httpc.setRequestProperty("Hash",hash);
		//System.out.println(url); // only for debug
		byte chunk[]=new byte[1024*1024];
		int read;
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		InputStream is=(httpc.getInputStream());

		while((read=is.read(chunk))!=-1)	bos.write(chunk,0,read);

		is.close();

		String response=new String(bos.toByteArray(),"UTF-8");
		System.out.println("Response: "+response);
		Map resp=_JSON.fromJson(response,Map.class);
		return ApiResponse.build().fromMap(resp);
	}

	public static void updateInfo(){
		try{
			String action="info";
			ApiResponse r=apiCall(action);
			if(r.statusCode()==200){
				Info info=r.data();
				INFO=info;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static  Info getInfo() throws Exception {
		if(INFO==null)updateInfo();
		return INFO;
	}


	public static String getAddrDeposit(long balance, Object wallid) throws Exception {
		String action="deposit&wallid="+wallid+"&amount="+balance;
		ApiResponse r=apiCall(action);
		if(r.statusCode()==200){
			Deposit deposit=r.data();
			String addr=deposit.addr;
			return addr;
		}else return null;
	}

	// Checking deposit status
	public static PendingDepositData getDepositStatus(String addr) throws Exception {
		String action="getdeposit&addr="+addr;
		ApiResponse r=apiCall(action);
		Deposit deposit=r.data();
		PendingDepositData output=new PendingDepositData();
		output.status=deposit.status;
		output.addr=deposit.receiver_wallet_id;
		return output;
	}

	// Get balance.
	public static double getBalance(Object wallid) throws Exception {
		String action="getwallet&wallid="+wallid;
		ApiResponse r=apiCall(action);
		if(r.statusCode()==200){
			OffchainWallet wallet=r.data();
			long balance=wallet.balance;
			return balance/100000000.;
		}else return -1;
	}


	// Create contract.
	public static String createContract(long amount, Object wallid) throws Exception {
		String action="newcontract&wallid="+wallid+"&amount="+amount;
		ApiResponse r=apiCall(action);
		if(r.statusCode()==200){
			OffchainContract contract=r.data();
			String cId=contract.id;
			return cId;
		}else return null;
	}


	// QR Engine
	public static String createQR(String addr, String coin, long amount) throws WriterException {
		double damount = reddconomy.Utils.convertToUserFriendly(amount);
		Map<EncodeHintType,Object> hint=new HashMap<EncodeHintType,Object>();
		com.google.zxing.qrcode.encoder.QRCode code=Encoder.encode(coin!=null&&!coin.isEmpty()?coin+":"+addr+"?amount="+damount:""+damount,ErrorCorrectionLevel.L,hint);
		ByteMatrix matrix=code.getMatrix();
		System.out.println(matrix.getWidth()+"x"+matrix.getHeight());
		StringBuilder qr=new StringBuilder();
		for(int y=0;y<matrix.getHeight();y++){
			for(int x=0;x<matrix.getWidth();x++){
				if(matrix.get(x,y)==0) qr.append("\u00A7f\u2588");
				else qr.append("\u00A70\u2588");
			}
			qr.append("\n");
		}
		return qr.toString();
	}

	// Accept contract.
	public static int acceptContract(String contractId, Object wallid) throws Exception {
		String action="acceptcontract&wallid="+wallid+"&contractid="+contractId;
		ApiResponse r=apiCall(action);
		return r.statusCode();
	}

	// Withdraw money
	public static int withdraw(long amount, String addr, Object wallid) throws Exception {
		String action="withdraw&amount="+amount+"&addr="+addr+"&wallid="+wallid;
		ApiResponse r=apiCall(action);
		return r.statusCode();
	}


	// Test, test, test and moar test.
	public static int sendCoins(String addr, long amount) throws Exception {
		String action="sendcoins&addr="+addr+"&amount="+amount;
		ApiResponse r=apiCall(action);
		return r.statusCode();
	}
}

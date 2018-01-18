/*
 * @author: Simone C., Riccardo B.;
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

public class ReddconomyApi {
		
		final Gson _JSON;
		final String _URL;
		
		public ReddconomyApi(String apiUrl) {
			_JSON = new GsonBuilder().setPrettyPrinting().create();
			_URL = apiUrl;
		}
		

		
		
	
		
		// Fundamental APIs for Reddconomy.
		@SuppressWarnings("rawtypes")
		public Map apiCall(String action) throws Exception
		{
			  String query = "/?action="+action;
			  String urlString = _URL+query;
			  URL url = new URL(urlString);
			  String hash = Utils.hmac("SECRET123", query);
			  HttpURLConnection httpc=(HttpURLConnection)url.openConnection(); //< la tua connessione
	          httpc.setRequestProperty("Hash",hash);
			  //System.out.println(url);
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
		
		public PendingDepositData getDepositStatus(String addr) throws Exception
		{
			String action = "getdeposit&addr=" + addr;
			Map data=(Map)apiCall(action).get("data");
			PendingDepositData output=new PendingDepositData();
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
		
		public String createServerContract(long amount) throws Exception
		{
			String action = "newcontract&wallid=[SRV]" + "&amount=" + amount;
			Map data=(Map)apiCall(action).get("data");
			return (String)data.get("contractId");
		}
		
		//implying that toggle is true)
		public void RedstoneEngine(Location<World> location, boolean toggle)
		{
			BlockState state = location.getBlock();
			BlockType blocktype = state.getType();
			if (blocktype.equals(BlockTypes.STONE_BUTTON)) {
				if (toggle) {
					BlockState newstate = state.with(Keys.POWERED, true).get();
					location.setBlock(newstate);
				}
				else {
					BlockState newstate = state.with(Keys.POWERED, false).get();
					location.setBlock(newstate);
				}
			}
		}
		
		public void setRed(Location<World> location, BlockType type, String text)
		{

		}
		
		public String createQR (String addr, String coin, String amount) throws WriterException
		{
			Map<EncodeHintType,Object> hint=new HashMap<EncodeHintType,Object>();
			com.google.zxing.qrcode.encoder.QRCode code=Encoder.encode(coin!=null&&!coin.isEmpty()?
					coin+":"+addr+"?amount="+amount:amount,ErrorCorrectionLevel.L,hint);
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

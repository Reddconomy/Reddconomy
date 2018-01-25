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
package net.reddconomy.plugin.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;

import net.reddconomy.plugin.api.ReddconomyApi;
import reddconomy.Utils;
import reddconomy.data.OffchainWallet;

public class FrontendUtils {
	public static String getLine(TileEntity sign, int line)	{
		Optional<SignData> data = sign.getOrCreate(SignData.class);
		return (data.get().lines().get(line)).toPlainSingle();
	}
	
	public static void setLine(TileEntity sign, int line, String text) {
		Optional<SignData> signdata=sign.getOrCreate(SignData.class);
		signdata.get().set(signdata.get().lines().set(line,Text.of(text)));
		sign.offer(signdata.get());
	}

	
	/**
	 * Input: w:WALLID Output: WALLID
	 * Input: PLAYER_NAME  Output: WALLID
	 * Input: s:SHORT_ID Output: s:SHORT_ID
	 * 
	 * @param val Prefixed string
	 * @return A wallid understandable by the backend
	 * @throws Exception
	 */
	public static String getWalletIdFromPrefixedString(String val) throws Exception{
    	if(val.startsWith("w:")){ // A walletid
    		return val.substring("w:".length());
    	}else if(!val.startsWith("s:")){ // a player name
			Player pl=Sponge.getServer().getPlayer(val).get();
			UUID uuid=pl.getUniqueId();
			OffchainWallet wallet=ReddconomyApi.getWallet(uuid);
			return "s:"+wallet.short_id;			
    	}
    	return val;
	}
	
	
    public static void createContractSignFromSign(TileEntity sign) throws Exception{
    	String contract_owner=getLine(sign,3);
    	setLine(sign,3,getWalletIdFromPrefixedString(contract_owner));
    }
    
    

    
	// Useful function to check if a player is an Operator.
	public static boolean isOp(Player player) {
		if(player.hasPermission("Everything.everything")) return true;
		else return false;
	}
    
    public static Collection<Sign> getSurroundingSigns(Location<World> location) {
		ArrayList<Sign> out = new ArrayList<Sign>();
		TileEntity sr_entities[] = new TileEntity[] { 
				location.getRelative(Direction.DOWN).getTileEntity().orElse(null),
				location.getRelative(Direction.UP).getTileEntity().orElse(null),
				location.getRelative(Direction.EAST).getTileEntity().orElse(null),
				location.getRelative(Direction.WEST).getTileEntity().orElse(null),
				location.getRelative(Direction.NORTH).getTileEntity().orElse(null),
				location.getRelative(Direction.SOUTH).getTileEntity().orElse(null) 
		};
		for (TileEntity te : sr_entities) {
			if (te!=null&&te instanceof Sign) {
				out.add((Sign) te);
			}
		}
		return out;
		
	}
	
	public static boolean canPlayerOpen(Location<World> location, String pname){
		Collection<Sign> surrounding_signs=getSurroundingSigns(location);
		for(Sign s:surrounding_signs){
			if (!(getLine(s,3).equals(pname)))
				{
				return false;
				}
		}
		return true;			
	}

	public static String hmac(String key, String data) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        Mac sha256_HMAC=Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key=new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
        sha256_HMAC.init(secret_key);
        return new String(Base64.getEncoder().encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))),"UTF-8");
    }
	
	// QR Engine
	public static String createQR(String addr, String coin, long amount) throws WriterException {
		double damount = Utils.convertToUserFriendly(amount);
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
	
	public static String createQRLink(String API_LINK, String addr, String coin, long amount) {
		double damount = Utils.convertToUserFriendly(amount);
		return API_LINK.replace("{PAYDATA}", (coin!=null&&!coin.isEmpty()?coin+":"+addr+"?amount="+damount:""+damount));
	}
}


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
package it.reddconomy.plugin.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import com.google.zxing.WriterException;

import it.reddconomy.common.data.OffchainWallet;
import it.reddconomy.plugin.Config;
import it.reddconomy.plugin.ReddconomyApi;

public class FrontendUtils {
	
	
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
	

       
	public static void sendQrToPlayer(String addr, long amount,Player player) throws WriterException, Exception{
		if(Config.getValue("qr").toString().equalsIgnoreCase("true")){
			player.sendMessage(Text.of(Qr.createQR(addr,ReddconomyApi.getInfo().coin,amount)));
		}else if(!Config.getValue("qr").toString().equalsIgnoreCase("false")){
			URL qrlink=new URL(Qr.createQRLink(Config.getValue("qr").toString(),addr,ReddconomyApi.getInfo().coin,amount));
			Text qrlink_text=Text.builder("[CLICK HERE] to generate a QR code").color(TextColors.GOLD).onClick(TextActions.openUrl(qrlink)).build();
			player.sendMessage(Text.of(qrlink_text));
		}
	}
    
	// Useful function to check if a player is an Operator.
	public static boolean isOp(Player player) {
		if(player.hasPermission("Everything.everything")) return true;
		else return false;
	}
    


	public static String hmac(String key, String data) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        Mac sha256_HMAC=Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key=new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
        sha256_HMAC.init(secret_key);
        return new String(Base64.getEncoder().encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))),"UTF-8");
    }
	
	public static ItemStack createItem(ItemType type, int quantity, String name, TextColor color)
	{
		ItemStack item = ItemStack.builder().itemType(type).quantity(quantity).build();
		item.offer(Keys.DISPLAY_NAME, Text.of(color,name));
		return item;
	}



	public static String getCoinString() throws Exception {
		return (ReddconomyApi.getInfo().testnet?"test ":"")+ReddconomyApi.getInfo().coin_short;
	}

}


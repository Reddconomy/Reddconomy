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
package it.reddconomy.plugin;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import it.reddconomy.Utils;
import it.reddconomy.plugin.utils.FrontendUtils;
import it.reddconomy.common.ApiResponse;
import it.reddconomy.common.data.Withdraw;
import it.reddconomy.plugin.utils.FrontendUtils;
public class AdminCommands{
	private static long _STARTTIME=System.currentTimeMillis();
	private static String timestampToHumanReadable(long millis){
		return String.format("%02d:%02d:%02d", 
				TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis) -  
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));   
		
	}
	// Complete Backend's info for OPs
	public static void showAdminInfos(Player player) throws Exception {
		player.sendMessage(Text.of(TextColors.BLUE,"[REDDCONOMY INFO]"
							+ "\n",TextColors.GOLD,"Testnet:  ",TextColors.WHITE,ReddconomyApi.getInfo().testnet
							+ "\n",TextColors.GOLD,"Debug:  ",TextColors.WHITE,Config.getValue("debug")
							+ "\n",TextColors.GOLD,"Coin: ",TextColors.WHITE,ReddconomyApi.getInfo().coin
							+ "\n",TextColors.GOLD,"Welcome Tip: ",TextColors.WHITE, Utils.convertToUserFriendly(ReddconomyApi.getInfo().welcome_tip)
							+ "\n",TextColors.GOLD,"Welcome Funds wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().welcome_funds_wallid
							+ "\n",TextColors.GOLD,"Generic wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().generic_wallid
							+ "\n",TextColors.GOLD,"Fees Collector wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().fees_collector_wallid
							+ "\n",TextColors.GOLD,"Null Wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().null_wallid
							+ "\n",TextColors.GOLD,"Deposit fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getDepositFee().toString()
							+ "\n",TextColors.GOLD,"Withdraw fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getWithdrawFee().toString()
							+ "\n",TextColors.GOLD,"Transaction fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getTransactionFee().toString()
							+ "\n",TextColors.GOLD,"Blockchain fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getBlockchainFee().toString()
							+ "\n",TextColors.GOLD,"Backend uptime: ",TextColors.WHITE,timestampToHumanReadable(ReddconomyApi.getInfo().uptime)
							+ "\n",TextColors.GOLD,"Frontend uptime: ",TextColors.WHITE,timestampToHumanReadable(System.currentTimeMillis()-_STARTTIME);
		);
	}
	// Admin help message of Reddconomy
	public static void sendAdminHelpText(Player player) {
		player.sendMessage(Text.of(TextColors.BLUE,"[REDDCONOMY HELP]"
							+ "\n",TextColors.BLUE,"=====[COMMANDS]====="
							+ "\n",TextColors.BLUE,"/$ admin help",TextColors.WHITE,": Shows this"
							+ "\n",TextColors.GOLD,"/$ admin send <amount> <addr>",TextColors.WHITE,": Send coins from the backend."
							+ "\n",TextColors.GOLD,"/$ admin deposit <amount> <wallid>",TextColors.WHITE,": Deposit into wallid."
							+ "\n",TextColors.GOLD,"/$ admin balance <wallid>",TextColors.WHITE,": Get balance of wallid."
							+ "\n",TextColors.GOLD,"/$ admin withdraw <amount> <wallid> <addr>",TextColors.WHITE,": Withdraw from wallid."
							+ "\n",TextColors.GOLD,"/$ admin info",TextColors.WHITE,": Shows info and status about the backend."));
	}

	public static boolean onCommand(Player player, String command, String[] args) throws Exception {
		if(FrontendUtils.isOp(player)){
			String action=args[0];
			if(args.length<1) return false; 
			switch(action){
				case "info":{
					showAdminInfos(player);
					return true;
				}
				case "send":{
					if(args.length<3){ return false; }
					double dval=Double.parseDouble(args[1]);
					String addr=args[2];
					long amount=Utils.convertToInternal(dval);
					int status=ReddconomyApi.sendCoins(addr,amount);
					if(status==200) player.sendMessage(Text.of("Sending "+dval+" to the address: "+addr));
					else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request coins right now. Check the error in console"));
					return true;
				}
				case "deposit":{
					if(args.length<3){ return false; }
					String wallid=FrontendUtils.getWalletIdFromPrefixedString(args[2]);
					double damount=Double.parseDouble(args[1]);
					long amount=Utils.convertToInternal(damount);
					String addr=ReddconomyApi.getAddrDeposit(amount,wallid);
					if(addr!=null){
						FrontendUtils.sendQrToPlayer(addr,amount,player);
						player.sendMessage(Text.of("Deposit "+damount+" "+(ReddconomyApi.getInfo().testnet?"TEST ":" ")+ReddconomyApi.getInfo().coin_short+" to this address: "+addr));
					}else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot create deposit address right now. Check the server console."));
					return true;
				}
				case "balance":{
					if(args.length<2){ return false; }
					String wallid=FrontendUtils.getWalletIdFromPrefixedString(args[1]);
					double balance=Utils.convertToUserFriendly(ReddconomyApi.getWallet(wallid).balance);
					if(balance!=-1) player.sendMessage(Text.of("Wallet "+wallid+" has: "+balance+" "+(ReddconomyApi.getInfo().testnet?"TEST ":" ")+ReddconomyApi.getInfo().coin_short));
					else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request balance right now. Contact an admin."));
					return true;
				}
				case "withdraw":{
					if(args.length<4){ return false; }
					String wallid=FrontendUtils.getWalletIdFromPrefixedString(args[2]);
					String addr=args[3];
					double damount=Double.parseDouble(args[1]);
					long amount=Utils.convertToInternal(damount);
					ApiResponse resp=ReddconomyApi.withdraw(amount,addr,wallid,true);
					if(resp.statusCode()==200){
						Withdraw wt=resp.data();
						player.sendMessage(Text.of(TextColors.BLUE,"Withdrawing.. Txid: "+wt.id));
					}else player.sendMessage(Text.of(TextColors.DARK_RED,"Error:"+resp.status()));
					return true;
				}
				default:
				case "help":{
					sendAdminHelpText(player);
					return true;
				}
			}
		}else player.sendMessage(Text.of(TextColors.DARK_RED,"Forbidden for non-op"));
		return true;

	}

	public static void init(Object obj) {
		
	}

}

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

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import it.reddconomy.Utils;
import it.reddconomy.plugin.utils.FrontendUtils;

public class AdminCommands{
	// Complete Backend's info for OPs
	public static void showAdminInfos(Player player) throws Exception {
		player.sendMessage(Text.of(TextColors.BLUE,"[REDDCONOMY INFO]"));
		player.sendMessage(Text.of(TextColors.GOLD,"Testnet:  ",TextColors.WHITE,ReddconomyApi.getInfo().testnet));
		player.sendMessage(Text.of(TextColors.GOLD,"Debug:  ",TextColors.WHITE,Config.getValue("debug")));

		player.sendMessage(Text.of(TextColors.GOLD,"Coin: ",TextColors.WHITE,ReddconomyApi.getInfo().coin));
		player.sendMessage(Text.of(TextColors.GOLD,"Welcome Tip: ",TextColors.WHITE, Utils.convertToUserFriendly(ReddconomyApi.getInfo().welcome_tip)));
		
		player.sendMessage(Text.of(TextColors.GOLD,"Welcome Funds wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().welcome_funds_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Generic wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().generic_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Fees Collector wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().fees_collector_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Null Wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().null_wallid));
		
		player.sendMessage(Text.of(TextColors.GOLD,"Deposit fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getDepositFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Withdraw fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getWithdrawFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Transaction fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getTransactionFee().toString()));
	}
	// Admin help message of Reddconomy
	public static void sendAdminHelpText(Player player) {
		player.sendMessage(Text.of(TextColors.BLUE,"[REDDCONOMY HELP]"));
		player.sendMessage(Text.of(TextColors.BLUE,"=====[COMMANDS]====="));
		player.sendMessage(Text.of(TextColors.BLUE,"/$ admin help",TextColors.WHITE,": Shows this"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin send <amount> <addr>",TextColors.WHITE,": Send coins from the backend."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin deposit <amount> <wallid>",TextColors.WHITE,": Deposit into wallid."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin balance <wallid>",TextColors.WHITE,": Get balance of wallid."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin withdraw <amount> <wallid> <addr>",TextColors.WHITE,": Withdraw from wallid."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ admin info",TextColors.WHITE,": Shows info and status about the backend."));
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
					int status=ReddconomyApi.withdraw(amount,addr,wallid);
					if(status==200) player.sendMessage(Text.of(TextColors.BLUE,"Withdrawing.. Wait at least 10 minutes"));
					else player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot request a withdraw right now, check the Reddconomy Service error."));
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

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

import java.net.MalformedURLException;
import java.net.URL;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import net.reddconomy.plugin.api.ReddconomyApi;
import reddconomy.Utils;

public class Help {
	// Backend's info
	public static void showInfos(Player player) throws Exception {
		player.sendMessage(Text.of(TextColors.BLUE,"[REDDCONOMY INFO]"));
		player.sendMessage(Text.of(TextColors.GOLD,"On testnet? ",TextColors.WHITE,ReddconomyApi.getInfo().testnet));
		player.sendMessage(Text.of(TextColors.GOLD,"Coin: ",TextColors.WHITE,ReddconomyApi.getInfo().coin));
		player.sendMessage(Text.of(TextColors.GOLD,"Deposit fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getDepositFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Withdraw fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getWithdrawFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Transaction fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getTransactionFee().toString()));
	}

	// Complete Backend's info for OPs
	public static void showAdminInfos(Player player) throws Exception {
		player.sendMessage(Text.of(TextColors.BLUE,"[REDDCONOMY INFO]"));
		player.sendMessage(Text.of(TextColors.GOLD,"On testnet? ",TextColors.WHITE,ReddconomyApi.getInfo().testnet));
		player.sendMessage(Text.of(TextColors.GOLD,"Coin: ",TextColors.WHITE,ReddconomyApi.getInfo().coin));
		player.sendMessage(Text.of(TextColors.GOLD,"Welcome Tip: ",TextColors.WHITE, Utils.convertToUserFriendly(ReddconomyApi.getInfo().welcome_tip)));
		player.sendMessage(Text.of(TextColors.GOLD,"Welcome Tip wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().welcome_funds_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Generic Wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().generic_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Fee Wallet id: ",TextColors.WHITE,ReddconomyApi.getInfo().fees_collector_wallid));
		player.sendMessage(Text.of(TextColors.GOLD,"Deposit fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getDepositFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Withdraw fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getWithdrawFee().toString()));
		player.sendMessage(Text.of(TextColors.GOLD,"Transaction fee: ",TextColors.WHITE,ReddconomyApi.getInfo().fees.getTransactionFee().toString()));
	}

	// Help message of Reddconomy
	public static void sendHelpText(Player player) throws MalformedURLException {

		URL github=new URL("https://github.com/Reddconomy");
		Text moreinfo=Text.builder("Click here for more info!").color(TextColors.GOLD).onClick(TextActions.openUrl(github)).build();
		player.sendMessage(Text.of(TextColors.BLUE,"[REDDCONOMY HELP]"));
		player.sendMessage(Text.of(TextColors.BLUE,"=====[COMMANDS]====="));
		player.sendMessage(Text.of(TextColors.GOLD,"/$",TextColors.WHITE,": Shows info"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ help",TextColors.WHITE,": Shows this"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ deposit <amount>",TextColors.WHITE,": Get the deposit address."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ balance",TextColors.WHITE,": Shows your balance."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ withdraw <amount> <addr>",TextColors.WHITE,": Withdraw money."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ contract new <amount>",TextColors.WHITE,": Create contract. (- sign for giving, no sign for requesting)"));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ contract accept <contractid>",TextColors.WHITE,": Accept a contract."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ tip <amount> <user>",TextColors.WHITE,": Tip an user."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ tipsrv <amount>",TextColors.WHITE,": Tip the server."));
		player.sendMessage(Text.of(TextColors.GOLD,"/$ info",TextColors.WHITE,": Get info from backend."));
		player.sendMessage(Text.of(TextColors.BLUE,"===[CONTRACT SIGNS]==="));
		player.sendMessage(Text.of("In order to make Contract Signs, you have to write in a sign:"));
		player.sendMessage(Text.of(TextColors.GOLD,"FIRST LINE:"," [CONTRACT] | ",TextColors.GOLD,"SECOND LINE: ","<amount>"));
		player.sendMessage(Text.of("Third line is for description, it's not necessary."));
		player.sendMessage(Text.of(TextColors.BLUE,"========[INFOs]========"));
		player.sendMessage(Text.of("Copyright (c) 2018, Riccardo Balbo, Simone Cervino. This plugin and all its components are released under GNU GPL v3 and BSD-2-Clause license."));
		player.sendMessage(moreinfo);
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
}

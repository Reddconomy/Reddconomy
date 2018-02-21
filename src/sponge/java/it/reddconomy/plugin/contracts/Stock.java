package it.reddconomy.plugin.contracts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

import it.reddconomy.Utils;
import it.reddconomy.plugin.Config;
import it.reddconomy.plugin.ReddconomyApi;
import it.reddconomy.plugin.contracts.sign.SignInitialization;
import it.reddconomy.plugin.utils.FrontendUtils;
import it.reddconomy.plugin.utils.SignUtils;
// TODO: Stock Exchange.
public class Stock{
	
	public static HashMap<Player, SignInitialization> stock = new HashMap<Player, SignInitialization>();
	private static Object PLUGIN;
	public static void init(Object plugin){
		if(PLUGIN!=null)return;
		PLUGIN=plugin;		
		Sponge.getEventManager().registerListeners(plugin, new Stock());
	}
	
    public static void createStockSignFromSign(TileEntity sign) throws Exception{
	    	String stock_owner=SignUtils.getLine(sign,2);
	    	SignUtils.setLine(sign,2,FrontendUtils.getWalletIdFromPrefixedString(stock_owner));
    }
	
	public static boolean isStockSign(Player player, InteractItemEvent.Secondary event)
	{
		Optional<Vector3d> opoint=event.getInteractionPoint();
		if(!opoint.isPresent()) return false;
		Vector3d point=opoint.get();
		SignInitialization cdata = new SignInitialization();
		stock.put(player, cdata);
		cdata.sign_location=player.getWorld().getLocation(point);
		Location<World> central_block = cdata.sign_location.getRelative(cdata.sign_location.get(Keys.DIRECTION).get().getOpposite());
		System.out.println(cdata.sign_location);
		if(cdata.sign_location.getTileEntity().isPresent()){
			TileEntity tile = cdata.sign_location.getTileEntity().get();
			if(!(tile instanceof Sign)) return false;
			cdata.sign_lines[0]=SignUtils.getLine(tile,0);
			if(SignUtils.ifStockSkeleton(central_block)&&cdata.sign_lines[0].equals("[STOCK]"))
			return true;
		} else return false;
		return false;
	}
	
	public static boolean isTestSign(Player player, InteractItemEvent.Secondary event)
	{
		Optional<Vector3d> opoint=event.getInteractionPoint();
		if(!opoint.isPresent()) return false;
		Vector3d point=opoint.get();
		Location<World> loc = player.getWorld().getLocation(point);
		if(loc.getTileEntity().isPresent()) {
			TileEntity tile = loc.getTileEntity().get();
			if(!(tile instanceof Sign)) return false;
			if(SignUtils.getLine(tile, 0).equals("test"))
			return true;
		} else return false;
		return false;
	}
	
	// Create contract signs from placed signs
		@Listener
		public void onStockPlace(ChangeSignEvent event) {
			if(!(FrontendUtils.isEnabled("stock"))||!(event.getSource() instanceof Player)) return;
			Task.builder().execute(() -> {
				TileEntity tile=event.getTargetTile();
				Player player=(Player)event.getSource();
				if(SignUtils.getLine(tile,0).equals("[STOCK]")){
					if(FrontendUtils.isOp(player)&&!SignUtils.getLine(tile,2).isEmpty()){
						player.sendMessage(Text.of("You've just created a Stock sign as Admin."));
					}else{
						player.sendMessage(Text.of("You've just placed a Stock sign."));
						SignUtils.setLine(tile,2,player.getName());
					}
					try{
						createStockSignFromSign(tile);
					}catch(Exception e){
						player.sendMessage(Text.of("Unexpected error"));
						SignUtils.setLine(tile,0,"[Err~STOCK]");
						e.printStackTrace();
					}
				}
			}).delay(5,TimeUnit.MILLISECONDS).name("Forcing player name in the Stock sign").submit(PLUGIN);
		}
		
		@Listener(order=Order.PRE)
		public void onStockInteract(InteractItemEvent.Secondary event) throws Exception {
			if(!(FrontendUtils.isEnabled("stock"))||!(event.getSource() instanceof Player)) return;
			
		}
	
		/*
		@Listener(order=Order.PRE)
		public void onSignInteract(InteractItemEvent.Secondary event) throws Exception {
			if(!((boolean)Config.getValue("stock"))||!(event.getSource() instanceof Player)) return;
			SignInitialization cdata = new SignInitialization();
			Player player=(Player)event.getSource();
			if (isStockSign(player, event)) {
					event.setCancelled(true);
					TileEntity tile = cdata.sign_location.getTileEntity().get();
					cdata.sign_lines[1]=SignUtils.getLine(tile,1);
					cdata.sign_lines[2]=SignUtils.getLine(tile,2);
					cdata.sign_lines[3]=SignUtils.getLine(tile,3);

					// Getting the original position of the block
					cdata.sign_direction=cdata.sign_location.get(Keys.DIRECTION).get();
					cdata.sign=cdata.sign_location.getBlockType();

					cdata.player_wallet=ReddconomyApi.getWallet(player.getUniqueId());
					cdata.owner_wallet=ReddconomyApi.getWallet(cdata.sign_lines[3]);

					String[] values=cdata.sign_lines[1].split(",");
					if(values.length<1) return;

					long amount=Utils.convertToInternal(Double.parseDouble(values[0].trim()));
					cdata.delay=100;
					if(values.length>1){
						int parsed_delay=Integer.parseInt(values[1].trim());
						if(parsed_delay>=0) cdata.delay=parsed_delay;
					}
					cdata.contract=ReddconomyApi.createContract(amount,cdata.owner_wallet.id);
					// Confirm Contract
					Text confirmcontract = Text.builder("[REDDCONOMY] CLICK HERE to Confirm Contract.")
							 .color(TextColors.GREEN)
							 .style(TextStyles.BOLD)
							 .onHover(TextActions.showText(Text.of("Contract ID: "+cdata.contract.id
																  +"\nAmount: "+Utils.convertToUserFriendly(cdata.contract.amount)
																  +"\n"+cdata.sign_lines[3])))
							 .onClick(TextActions.runCommand("/$ confirmsign"))
							 .build();
					// Decline Contract
					Text declinecontract = Text.builder("[REDDCONOMY] CLICK HERE to Decline Contract.")
							 .color(TextColors.DARK_RED)
							 .style(TextStyles.BOLD)
							 .onHover(TextActions.showText(Text.of("Contract ID: "+cdata.contract.id
																  +"\nAmount: "+Utils.convertToUserFriendly(cdata.contract.amount)
																  +"\n"+cdata.sign_lines[3])))
							 .onClick(TextActions.runCommand("/$ declinesign"))
							 .build();
					player.sendMessage(Text.of(confirmcontract,"\n",declinecontract));
				}
			}
			*/
		@Listener
		public void testItemFrame(InteractItemEvent.Secondary event)
		{
			Player player = (Player) event.getSource();
			if (isTestSign(player, event))
			{
				 Vector3d point = event.getInteractionPoint().get();
				 Location<World> loc = player.getWorld().getLocation(point);
				 Entity itemframe = SignUtils.searchForItemFrame(loc, point);
				 if (itemframe!=null) {
					 player.sendMessage(Text.of("Item Frame contains: "+itemframe.get(Keys.REPRESENTED_ITEM).get().getType().getId()));
				 }
			}
		}
}

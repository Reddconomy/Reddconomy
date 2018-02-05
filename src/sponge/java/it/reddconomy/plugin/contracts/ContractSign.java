package it.reddconomy.plugin.contracts;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.block.tileentity.carrier.Dropper;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.statistic.ChangeStatisticEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import it.reddconomy.Utils;
import it.reddconomy.common.data.OffchainWallet;
import it.reddconomy.plugin.Config;
import it.reddconomy.plugin.ReddconomyApi;
import it.reddconomy.plugin.utils.FrontendUtils;

public class ContractSign{

	// If the contract sign is placed above one of these blocks, it will be replaced with a redstone block
	// instead of a redstone torch
	public static final Collection<BlockType> _BLOCK_BLACKLIST=Arrays.asList(new BlockType[]{
		BlockTypes.SEA_LANTERN,
		BlockTypes.PISTON,
		BlockTypes.STICKY_PISTON,
		BlockTypes.PISTON_HEAD,
		BlockTypes.PISTON_EXTENSION,
		BlockTypes.CACTUS,
		BlockTypes.TNT,
		BlockTypes.AIR	
	});
	
	public static HashMap<Player, ContractInitialization> contract = new HashMap<Player, ContractInitialization>();
	public static ContractInitialization cdata;
	private final static Text CONTRACT_INVENTORY_NAME = Text.of(TextColors.GOLD,TextStyles.BOLD,"Reddconomy Contracts");
	public static final Collection<Vector3i> _ACTIVATED_SIGNS=new LinkedList<Vector3i>();
	private static Object PLUGIN;
	public static void init(Object plugin){
		if(PLUGIN!=null)return;
		PLUGIN=plugin;		
		Sponge.getEventManager().registerListeners(plugin, new ContractSign());
	}
	
	
	public static String getLine(TileEntity sign, int line)	{
		Optional<SignData> data = sign.getOrCreate(SignData.class);
		return (data.get().lines().get(line)).toPlainSingle();
	}
	
	public static void setLine(TileEntity sign, int line, String text) {
		Optional<SignData> signdata=sign.getOrCreate(SignData.class);
		signdata.get().set(signdata.get().lines().set(line,Text.of(text)));
		sign.offer(signdata.get());
	}
    public static Collection<TileEntity> getNearTileEntities(Location<World> location) {
//		ArrayList<Sign> out = new ArrayList<Sign>();
		TileEntity sr_entities[] = new TileEntity[] { 
				location.getRelative(Direction.DOWN).getTileEntity().orElse(null),
				location.getRelative(Direction.UP).getTileEntity().orElse(null),
				location.getRelative(Direction.EAST).getTileEntity().orElse(null),
				location.getRelative(Direction.WEST).getTileEntity().orElse(null),
				location.getRelative(Direction.NORTH).getTileEntity().orElse(null),
				location.getRelative(Direction.SOUTH).getTileEntity().orElse(null) 
		};
//		for (TileEntity te : sr_entities) {
//			if (te!=null&&te instanceof Sign) {
//				out.add((Sign) te);
//			}
//		}
		return Arrays.asList(sr_entities);
		
	}
    public static Collection<LocatableBlock> getNearBlocks(Location<World> location) {
    	LocatableBlock sr_entities[] = new LocatableBlock[] { 
				location.getRelative(Direction.DOWN).getLocatableBlock().orElse(null),
				location.getRelative(Direction.UP).getLocatableBlock().orElse(null),
				location.getRelative(Direction.EAST).getLocatableBlock().orElse(null),
				location.getRelative(Direction.WEST).getLocatableBlock().orElse(null),
				location.getRelative(Direction.NORTH).getLocatableBlock().orElse(null),
				location.getRelative(Direction.SOUTH).getLocatableBlock().orElse(null)
		};
		return Arrays.asList(sr_entities);
		
	}

    public static void createContractSignFromSign(TileEntity sign) throws Exception{
    	String contract_owner=getLine(sign,3);
    	setLine(sign,3,FrontendUtils.getWalletIdFromPrefixedString(contract_owner));
    }
    

    // Create contract signs from placed signs
	@Listener
	public void onSignPlace(ChangeSignEvent event) {
		if(!((boolean)Config.getValue("csigns"))||!(event.getSource() instanceof Player)) return;

		Task.builder().execute(() -> {
			TileEntity tile=event.getTargetTile();
			Player player=(Player)event.getSource();
			if(getLine(tile,0).equals("[CONTRACT]")){
				if(FrontendUtils.isOp(player)&&!getLine(tile,3).isEmpty()){
					player.sendMessage(Text.of("You've just placed a contract sign as Admin."));
				}else{
					player.sendMessage(Text.of("You've just placed a contract sign."));
					setLine(tile,3,player.getName());
				}
				try{
					createContractSignFromSign(tile);
				}catch(Exception e){
					player.sendMessage(Text.of("Unexpected error"));
					setLine(tile,0,"[Err~CONTRACT]");
					e.printStackTrace();
				}
			}
		}).delay(5,TimeUnit.MILLISECONDS).name("Forcing player name in the contract sign").submit(PLUGIN);
	}

	// In case of GUI, add this
	/*
	 * cdata = new ContractInitialization();
	 * contract.put(player, cdata);
	 *
	 * cdata.variable
	 */
	@Listener(order=Order.PRE)
	public void onSignInteract(InteractItemEvent.Secondary event) throws Exception {
		if(!((boolean)Config.getValue("csigns"))||!(event.getSource() instanceof Player)) return;

		Optional<Vector3d> opoint=event.getInteractionPoint();
		if(!opoint.isPresent()) return;
		Vector3d point=opoint.get();

		Player player=(Player)event.getSource();
		cdata = new ContractInitialization();
		contract.put(player, cdata);
		cdata.location=player.getWorld().getLocation(point);
		System.out.println(cdata.location);
		if(cdata.location.getTileEntity().isPresent()){
			cdata.tile = cdata.location.getTileEntity().get();
			if(!(cdata.tile instanceof Sign)) return;
			cdata.line0=getLine(cdata.tile,0);
			if(cdata.line0.equals("[CONTRACT]")){
				event.setCancelled(true);

				cdata.line1=getLine(cdata.tile,1);
				cdata.line2=getLine(cdata.tile,2);
				cdata.line3=getLine(cdata.tile,3);

				// Getting the original position of the block
				cdata.origdirection=cdata.location.get(Keys.DIRECTION).get();
				cdata.origsign=cdata.location.getBlockType();

				cdata.player_wallet=ReddconomyApi.getWallet(player.getUniqueId());

				cdata.owner_id=cdata.line3;
				cdata.owner_wallet=ReddconomyApi.getWallet(cdata.line3);

				cdata.values=cdata.line1.split(",");
				if(cdata.values.length<1) return;

				cdata.amount=Utils.convertToInternal(Double.parseDouble(cdata.values[0].trim()));
				cdata.delay=100;
				if(cdata.values.length>1){
					cdata.parsed_delay=Integer.parseInt(cdata.values[1].trim());
					if(cdata.parsed_delay>=0) cdata.delay=cdata.parsed_delay;
				}
				cdata.cID=ReddconomyApi.createContract(cdata.amount,cdata.owner_id);
				// Confirm Contract
				player.sendMessage(Text.builder("[REDDCONOMY] Confirm Contract.")
									 .color(TextColors.GREEN)
									 .style(TextStyles.BOLD)
									 .style(TextStyles.UNDERLINE)
									 .onHover(TextActions.showText(Text.of("Confirm the Contract Sign ID: "+cdata.cID)))
									 .onClick(TextActions.runCommand("/$ confirm "))
									 .build());
				// Decline Contract
				player.sendMessage(Text.builder("[REDDCONOMY] Decline Contract.")
						 .color(TextColors.DARK_RED)
						 .style(TextStyles.BOLD)
						 .style(TextStyles.UNDERLINE)
						 .onHover(TextActions.showText(Text.of("Decline the Contract Sign that you clicked.")))
						 .onClick(TextActions.runCommand("/$ decline "))
						 .build());
			}

		}
	}
	
	/*@Listener(order=Order.FIRST)
	public void confirmContractClick(ClickInventoryEvent.Primary event)
	{			
		Player player = (Player) event.getCause().allOf(Player.class).get(0);
		cdata = contract.get(player);
		String confirmation = event.getCursorTransaction().getFinal().getType().getId();
		String inv = event.getTargetInventory().getProperty(InventoryTitle.class, InventoryTitle.PROPERTY_NAME).get().getValue().toString();
		if (inv.equals(CONTRACT_INVENTORY_NAME.toString()))
		if (confirmation.equals("minecraft:emerald_block")) {
			event.setCancelled(true);
			player.sendMessage(Text.of("Contract Confirmed"));
			Task.builder().execute(() -> player.closeInventory()).delay(10, TimeUnit.MILLISECONDS).submit(PLUGIN);
			try{
				// Check if player wallet != owner wallet
				if(player_wallet.short_id!=owner_wallet.short_id||((boolean)Config.getValue("debug"))){
					long cID=ReddconomyApi.createContract(amount,owner_id);
					int status=ReddconomyApi.acceptContract(cID,player_wallet.id);
					// This activates the redstone only if the contract replied with 200
					if(status==200){
						player.sendMessage(Text.of("Contract ID: "+cID));
						player.sendMessage(Text.of("Contract accepted."));
						player.sendMessage(Text.of("You have now: "+Utils.convertToUserFriendly(ReddconomyApi.getWallet(player_wallet.id).balance)+" "+ReddconomyApi.getInfo().coin_short));

						_ACTIVATED_SIGNS.add(location.getBlockPosition());
				
						location.setBlockType(
								_BLOCK_BLACKLIST.contains(location.getRelative(Direction.DOWN).getBlockType())
								?BlockTypes.REDSTONE_BLOCK:BlockTypes.REDSTONE_TORCH);
						Task.builder().execute(() -> {
							if(location.getBlock().getType()!=BlockTypes.REDSTONE_TORCH&&location.getBlock().getType()!=BlockTypes.REDSTONE_BLOCK)return;

							BlockState state=origsign.getDefaultState();
							BlockState newstate=state.with(Keys.DIRECTION,origdirection).get();
							location.setBlock(newstate);
							TileEntity tile2=location.getTileEntity().get();
							setLine(tile2,0,line0);
							setLine(tile2,1,line1);
							setLine(tile2,2,line2);
							setLine(tile2,3,line3);
							_ACTIVATED_SIGNS.remove(location.getBlockPosition());
						}).delay(delay,TimeUnit.MILLISECONDS).name("Powering off Redstone.").submit(PLUGIN);
					}else{
						player.sendMessage(Text.of(TextColors.DARK_RED,"Check your balance. Cannot accept contract"));
					}
				}else{
					player.sendMessage(Text.of(TextColors.DARK_RED,"You can't accept your own contract."));
				}

			}catch(Exception e){
				player.sendMessage(Text.of(TextColors.DARK_RED,"Cannot create/accept contract. Call an admin for more info."));
				e.printStackTrace();

			}
		} else if (confirmation.equals("minecraft:red_mushroom_block")) {
			event.setCancelled(true);
			player.sendMessage(Text.of("Contract Declined"));
			Task.builder().execute(() -> player.closeInventory()).delay(10, TimeUnit.MILLISECONDS).submit(PLUGIN);
		}
	}*/

	// Protect near dispenser/dropper
	@Listener(order=Order.FIRST)
	public void onContainerAdjacentInteract(InteractBlockEvent.Secondary event) throws Exception {
		Player player=(Player)event.getSource();
		if(event.getTargetBlock().getLocation().isPresent()){
			Location<World> location=event.getTargetBlock().getLocation().get();
			if(location.getTileEntity().isPresent()){
				TileEntity tile=location.getTileEntity().get();
				if(tile instanceof Dispenser||tile instanceof Dropper){
					Collection<TileEntity> near_blocks=getNearTileEntities(location);
					OffchainWallet player_wallet=null;
					for(TileEntity block:near_blocks){
						if(block==null||!(block instanceof Sign))continue;
						Sign s=(Sign)block;
						if(player_wallet==null) player_wallet=ReddconomyApi.getWallet(player.getUniqueId());
						OffchainWallet sign_owner=ReddconomyApi.getWallet(getLine(s,3));
						if(player_wallet.short_id!=sign_owner.short_id){
							if(!FrontendUtils.isOp(player)){
								player.sendMessage(Text.of(TextColors.DARK_RED,"[CONTRACT] Only the owner can open this container."));
								event.setCancelled(true);
							}else{
								player.sendMessage(Text.of(TextColors.DARK_RED,"[CONTRACT] You are bypassing container protection as OP."));
							}
							break;
						}
					}			
				}
			}
		}
	}
	
	
	
	@Listener(order=Order.FIRST)
	public void onExplosion(ExplosionEvent.Detonate event) {		
		for(Location<World> l:event.getAffectedLocations()){
			Optional<LocatableBlock> lb=l.getLocatableBlock();
			if(!lb.isPresent())continue;
			LocatableBlock b=lb.get();
			if(b.getBlockState().getType()==BlockTypes.REDSTONE_TORCH||b.getBlockState().getType()==BlockTypes.REDSTONE_BLOCK){
				boolean is_zone_protected=_ACTIVATED_SIGNS.contains(b.getLocation().getBlockPosition());
				if(is_zone_protected){
					event.setCancelled(true);
					break;
				}
			}
		}	
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlockEvent(ChangeBlockEvent.Pre event) {		
		if(event.isCancelled()) return;
		for(Location<World> l:event.getLocations()){
			if(l.getBlockType()==BlockTypes.SLIME){				
				Collection<LocatableBlock> near=getNearBlocks(l);
				for(LocatableBlock b:near){
					if(b.getBlockState().getType()==BlockTypes.REDSTONE_TORCH||b.getBlockState().getType()==BlockTypes.REDSTONE_BLOCK){
						boolean is_zone_protected=_ACTIVATED_SIGNS.contains(b.getLocation().getBlockPosition());
						if(is_zone_protected){
							event.setCancelled(true);
							break;
						}
					}
				}
			}else if((l.getBlockType()==BlockTypes.REDSTONE_TORCH||l.getBlockType()==BlockTypes.REDSTONE_BLOCK)&&_ACTIVATED_SIGNS.contains(l.getBlockPosition())){
				event.setCancelled(true);
				break;
			}
		}		
	}
	
	
	@Listener(order=Order.FIRST)
	public void onChangeBlockEvent(ChangeBlockEvent event) {
		if(event.isCancelled()) return;
		
		for(Transaction<BlockSnapshot> trans:event.getTransactions()){			
			BlockSnapshot oblock=trans.getOriginal();
			BlockSnapshot fblock=trans.getFinal();

			
			if(oblock.getState().getType()!=BlockTypes.REDSTONE_TORCH||oblock.getState().getType()!=BlockTypes.REDSTONE_BLOCK)continue;
				
			Optional<Location<World>> lo=trans.getOriginal().getLocation();
			if(!lo.isPresent()) continue;
			Location<World> l=lo.get();
			

			boolean is_zone_protected=_ACTIVATED_SIGNS.contains(l.getBlockPosition());

			if(is_zone_protected){
				if(	
					fblock.getState().getType()!=(BlockTypes.STANDING_SIGN)
					&&fblock.getState().getType()!=(BlockTypes.WALL_SIGN)
				){
					event.setCancelled(true);
					System.out.println("Can't replace "+l.getBlock()+" with "+fblock);
					if(event.getSource() instanceof Player) ((Player)event.getSource()).sendMessage(Text.of(TextColors.DARK_RED,"[CONTRACT] Block protected."));
					break;
				}
			}
			
		}
	}

	final boolean _DEBUG_PRINT_EVENTS=false;
	@Listener(order=Order.PRE)
	public void debugPrintEvents(Event event) throws Exception {
		if(!_DEBUG_PRINT_EVENTS) return;
		if(event instanceof TargetChunkEvent||
				event instanceof CollideEntityEvent||
				event instanceof MoveEntityEvent||
				event instanceof ChangeStatisticEvent||
				event instanceof CollideBlockEvent||
				event instanceof AnimateHandEvent||
				event instanceof DamageEntityEvent||
				event instanceof ChangeDataHolderEvent) return;
		System.out.println("EV "+event.getClass()+" "+event);

	}
}

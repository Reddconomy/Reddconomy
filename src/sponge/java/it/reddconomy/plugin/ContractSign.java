package it.reddconomy.plugin;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;

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
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.statistic.ChangeStatisticEvent;
import org.spongepowered.api.event.block.TargetBlockEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.chunk.TargetChunkEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import it.reddconomy.Utils;
import it.reddconomy.common.data.OffchainWallet;
import it.reddconomy.plugin.utils.FrontendUtils;

public class ContractSign{

	// If the contract sign is placed above one of these blocks, it will be replaced with a redstone block
	// instead of a redstone torch
	private static final Collection<BlockType> _BLOCK_BLACKLIST=Arrays.asList(new BlockType[]{
		BlockTypes.SEA_LANTERN,
		BlockTypes.PISTON,
		BlockTypes.STICKY_PISTON,
		BlockTypes.PISTON_HEAD,
		BlockTypes.PISTON_EXTENSION,
		BlockTypes.CACTUS,
		BlockTypes.TNT,
		BlockTypes.AIR	
	});
	
	private static final Collection<Vector3i> _ACTIVATED_SIGNS=new LinkedList<Vector3i>();
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

	@Listener(order=Order.PRE)
	public void onSignInteract(InteractItemEvent.Secondary event) throws Exception {
		if(!((boolean)Config.getValue("csigns"))||!(event.getSource() instanceof Player)) return;

		Optional<Vector3d> opoint=event.getInteractionPoint();
		if(!opoint.isPresent()) return;
		Vector3d point=opoint.get();

		Player player=(Player)event.getSource();
		Location<World> location=player.getWorld().getLocation(point);

		if(location.getTileEntity().isPresent()){
			TileEntity tile=location.getTileEntity().get();
			if(!(tile instanceof Sign)) return;
			String line0=getLine(tile,0);
			if(line0.equals("[CONTRACT]")){
				event.setCancelled(true);

				String line1=getLine(tile,1);
				String line2=getLine(tile,2);
				String line3=getLine(tile,3);

				// Getting the original position of the block
				Direction origdirection=location.get(Keys.DIRECTION).get();
				BlockType origsign=location.getBlockType();

				OffchainWallet player_wallet=ReddconomyApi.getWallet(player.getUniqueId());

				String owner_id=line3;
				OffchainWallet owner_wallet=ReddconomyApi.getWallet(line3);

				String[] values=line1.split(",");
				if(values.length<1) return;

				long amount=Utils.convertToInternal(Double.parseDouble(values[0].trim()));
				int delay=100;
				if(values.length>1){
					int parsed_delay=Integer.parseInt(values[1].trim());
					if(parsed_delay>=0) delay=parsed_delay;
				}

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

			}

		}
	}

	// Protect near dispenser/dropper
	@Listener(order=Order.FIRST)
	public void onContainerAdjacentInteract(InteractBlockEvent.Secondary event) throws Exception {
		Player player=(Player)event.getSource();
		OffchainWallet player_wallet=ReddconomyApi.getWallet(player.getUniqueId());

		if(event.getTargetBlock().getLocation().isPresent()){
			Location<World> location=event.getTargetBlock().getLocation().get();
			if(location.getTileEntity().isPresent()){
				TileEntity tile=location.getTileEntity().get();
				if(tile instanceof Dispenser||tile instanceof Dropper){
					Collection<TileEntity> near_blocks=getNearTileEntities(location);
					for(TileEntity block:near_blocks){
						if(block==null||!(block instanceof Sign))continue;
						Sign s=(Sign)block;
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

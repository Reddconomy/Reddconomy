package it.reddconomy.plugin.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

import it.reddconomy.plugin.contracts.ContractSign;

public class SignUtils{
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
		TileEntity sr_entities[] = new TileEntity[] { 
				location.getRelative(Direction.DOWN).getTileEntity().orElse(null),
				location.getRelative(Direction.UP).getTileEntity().orElse(null),
				location.getRelative(Direction.EAST).getTileEntity().orElse(null),
				location.getRelative(Direction.WEST).getTileEntity().orElse(null),
				location.getRelative(Direction.NORTH).getTileEntity().orElse(null),
				location.getRelative(Direction.SOUTH).getTileEntity().orElse(null) 
		};
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
    
    public static boolean canHostWallTorch(Location<World> loc, Direction dir) {
		    	if (!(loc.getRelative(dir).getBlockType().equals(BlockTypes.AIR))&&!(ContractSign._BLOCK_BLACKLIST.contains(loc.getRelative(dir).getBlockType())))
		    		return true;
		    	else return false;
    }
    
    public static BlockState doWallTorch(Location<World> loc, Direction dir) {
		    	return BlockTypes.REDSTONE_TORCH.getDefaultState().with(Keys.DIRECTION, dir.getOpposite()).get();
    }

    public static boolean ifStockSkeleton(Location<World> loc)
    {
	    	if (loc.getBlockType().equals(BlockTypes.EMERALD_BLOCK)
	    			&&loc.getRelative(Direction.UP).getBlockType().equals(BlockTypes.DIAMOND_BLOCK)
	    			&&loc.getRelative(Direction.EAST).getBlockType().equals(BlockTypes.DIAMOND_BLOCK)
	    			&&loc.getRelative(Direction.WEST).getBlockType().equals(BlockTypes.DIAMOND_BLOCK)
	    			&&loc.getRelative(Direction.DOWN).getBlockType().equals(BlockTypes.DIAMOND_BLOCK))
	    	return true;
	    	else return false;
    }
    
    public static Entity searchForItemFrame(Location<World> loc, Vector3d point) {
		 Collection<Entity> entcollec = loc.getExtent().getNearbyEntities(point, 1.1);
		 for (Entity ent:entcollec) {
			 if (ent instanceof ItemFrame) {
			 return ent;
			 } else return null;
		 }
		 return null;
    }
}

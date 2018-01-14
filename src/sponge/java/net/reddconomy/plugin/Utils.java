package net.reddconomy.plugin;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Utils {
	public static String getLine(TileEntity position, int line)	{
		Optional<SignData> data = position.getOrCreate(SignData.class);
		return (data.get().lines().get(line)).toPlainSingle();
	}
	
    public  static void setLine(TileEntity entity, int line, Text text) {
    	Optional<SignData> sign = entity.getOrCreate(SignData.class);
            sign.get().set(sign.get().lines().set(line, text));
            entity.offer(sign.get());
    }
    
    
	public static <T> Collection<T> getSurroundingBlocks(Class<T> oftype, Location<World> location) {
		ArrayList<T> out = new ArrayList<T>();
		TileEntity sr_entities[] = new TileEntity[] { 
				location.getRelative(Direction.DOWN).getTileEntity().get(),
				location.getRelative(Direction.UP).getTileEntity().get(),
				location.getRelative(Direction.EAST).getTileEntity().get(),
				location.getRelative(Direction.WEST).getTileEntity().get(),
				location.getRelative(Direction.NORTH).getTileEntity().get(),
				location.getRelative(Direction.SOUTH).getTileEntity().get() 
		};
		for (TileEntity te : sr_entities) {
			if (te.getClass().isAssignableFrom(oftype)) {
				out.add((T)te);
			}
		}
		return out;
	}
	
	
	public static boolean canPlayerOpen(Location<World> location, String pname){
		Collection<Sign> surrounding_signs=getSurroundingBlocks(Sign.class,location);
		for(Sign s:surrounding_signs){
			if (getLine(s,3).equals(pname))return true;
		}
		return false;			
	}

	
	public static String hmac(String key, String data) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        Mac sha256_HMAC=Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key=new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
        sha256_HMAC.init(secret_key);
        return new String(Base64.getEncoder().encode(sha256_HMAC.doFinal(data.getBytes("UTF-8"))),"UTF-8");
    }
}

/*
 * @author: Simone C., Riccardo B.;
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
}

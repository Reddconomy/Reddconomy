package it.reddconomy.plugin.contracts;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.reddconomy.common.data.OffchainWallet;

public class ContractInitialization {
	public Direction origdirection;
	public BlockType origsign;
	public OffchainWallet player_wallet;
	public OffchainWallet owner_wallet;
	public String owner_id, line0, line1, line2, line3;
	public Location<World> location;
	public String[] values;
	public TileEntity tile;
	public long amount;
	public int delay, parsed_delay;
	public long cID;
}

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
package it.reddconomy.plugin.contracts.sign;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import it.reddconomy.common.data.OffchainContract;
import it.reddconomy.common.data.OffchainWallet;

public class SignInitialization {
	public Direction sign_direction;
	public BlockType sign;
	public OffchainWallet player_wallet;
	public OffchainWallet owner_wallet;
	public OffchainContract contract;
	public String[] sign_lines = new String[4];
	public Location<World> sign_location;
	public int delay;
}

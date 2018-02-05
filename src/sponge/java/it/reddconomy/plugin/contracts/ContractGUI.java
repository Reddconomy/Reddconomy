/*package it.reddconomy.plugin.contracts;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.Inventory2D;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import it.reddconomy.plugin.utils.FrontendUtils;

public class ContractGUI {
	
	final static Text CONTRACT_INVENTORY_NAME = Text.of(TextColors.GOLD,TextStyles.BOLD,"Reddconomy Contracts");
	private static Object PLUGIN;
	public static void init(Object plugin){
		if(PLUGIN!=null)return;
		PLUGIN=plugin;		
		Sponge.getEventManager().registerListeners(plugin, new ContractGUI());
	}
	
	@SuppressWarnings("deprecation")
	public static Inventory2D contractInventory()
	{
		
		Inventory inv = Inventory.builder().of(InventoryArchetypes.MENU_COLUMN)
				.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(CONTRACT_INVENTORY_NAME))
				.build(PLUGIN);
		inv.query(Inventory2D.class)..set(new SlotPos(3,1),FrontendUtils.createItem(ItemTypes.EMERALD_BLOCK, 1, "Confirm Contract", TextColors.GREEN));
		inv2d.set(new SlotPos(7,1),FrontendUtils.createItem(ItemTypes.RED_MUSHROOM_BLOCK, 1, "Decline Contract", TextColors.DARK_RED));
		return inv2d;
	}
	
	@Listener(order=Order.FIRST)
	public void cancelContractShift(ClickInventoryEvent.Shift event)
	{
		String inv = event.getTargetInventory().getProperty(InventoryTitle.class, InventoryTitle.PROPERTY_NAME).get().getValue().toString();
		if (inv.equals(CONTRACT_INVENTORY_NAME.toString()))
		event.setCancelled(true);
	}
	
	@Listener(order=Order.FIRST)
	public void cancelContractSecondary(ClickInventoryEvent.Secondary event)
	{
		String inv = event.getTargetInventory().getProperty(InventoryTitle.class, InventoryTitle.PROPERTY_NAME).get().getValue().toString();
		if (inv.equals(CONTRACT_INVENTORY_NAME.toString()))
		event.setCancelled(true);
	}
}*/

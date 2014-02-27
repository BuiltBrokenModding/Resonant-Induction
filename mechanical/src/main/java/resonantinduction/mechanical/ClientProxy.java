package resonantinduction.mechanical;

import net.minecraftforge.client.MinecraftForgeClient;
import resonantinduction.mechanical.energy.gear.RenderGear;
import resonantinduction.mechanical.energy.gear.RenderGearShaft;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;
import resonantinduction.mechanical.fluid.tank.ItemTankRenderer;
import calclavia.lib.render.item.GlobalItemRenderer;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		GlobalItemRenderer.register(Mechanical.itemGear.itemID, RenderGear.INSTANCE);
		GlobalItemRenderer.register(Mechanical.itemGearShaft.itemID, RenderGearShaft.INSTANCE);
		GlobalItemRenderer.register(Mechanical.itemPipe.itemID, RenderPipe.INSTANCE);
	}

	@Override
	public void init()
	{
		MinecraftForgeClient.registerItemRenderer(Mechanical.blockTank.blockID, new ItemTankRenderer());
	}
}

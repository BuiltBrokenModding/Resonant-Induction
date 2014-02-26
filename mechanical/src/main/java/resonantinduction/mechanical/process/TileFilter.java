package resonantinduction.mechanical.process;

import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.RecipeResource;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.fluid.BlockFluidMixture;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.TileExternalInventory;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.inventory.InventoryUtility;

public class TileFilter extends TileExternalInventory
{
	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (ticks % 60 == 0)
		{
			Vector3 position = new Vector3(this);
			Vector3 checkAbove = position.clone().translate(ForgeDirection.UP);
			Vector3 checkBelow = position.clone().translate(ForgeDirection.DOWN);

			Block bAbove = Block.blocksList[checkAbove.getBlockID(worldObj)];
			Block bBelow = Block.blocksList[checkAbove.getBlockID(worldObj)];

			if (bAbove instanceof BlockFluidMixture && (worldObj.isAirBlock(checkBelow.intX(), checkBelow.intY(), checkBelow.intZ()) || checkBelow.getTileEntity(worldObj) instanceof IFluidHandler))
			{
				worldObj.spawnParticle("dripWater", xCoord + 0.5, yCoord, zCoord + 0.5, 0, 0, 0);

				if (checkBelow.getTileEntity(worldObj) instanceof IFluidHandler)
				{
					IFluidHandler handler = ((IFluidHandler) checkBelow.getTileEntity(worldObj));

					if (handler.fill(ForgeDirection.UP, new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), false) <= 0)
						return;
				}

				/**
				 * Leak the fluid down.
				 */
				BlockFluidMixture fluidBlock = (BlockFluidMixture) bAbove;
				int amount = fluidBlock.getQuantaValue(worldObj, checkAbove.intX(), checkAbove.intY(), checkAbove.intZ());
				int leakAmount = 2;

				/**
				 * Drop item from fluid.
				 */
				for (RecipeResource resoure : MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER, "dust" + LanguageUtility.capitalizeFirst(ResourceGenerator.mixtureToMaterial(fluidBlock.getFluid().getName()))))
				{
					InventoryUtility.dropItemStack(worldObj, checkAbove.clone().add(0.5), resoure.getItemStack().copy());
				}

				// TODO: Check if this is correct?
				int remaining = amount - leakAmount;

				/**
				 * Remove liquid from top.
				 */
				fluidBlock.setQuanta(worldObj, checkAbove.intX(), checkAbove.intY(), checkAbove.intZ(), remaining);

				/**
				 * Add liquid to bottom.
				 */
				if (checkBelow.getTileEntity(worldObj) instanceof IFluidHandler)
				{
					IFluidHandler handler = ((IFluidHandler) checkBelow.getTileEntity(worldObj));
					handler.fill(ForgeDirection.UP, new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), true);
				}
				else
				{
					checkBelow.setBlock(worldObj, Block.waterMoving.blockID);
				}
			}
		}
	}
}
/**
 * 
 */
package resonantinduction.battery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import resonantinduction.PacketHandler;
import resonantinduction.api.IBattery;
import resonantinduction.base.IPacketReceiver;
import resonantinduction.base.SetUtil;
import resonantinduction.base.TileEntityBase;

import com.google.common.io.ByteArrayDataInput;

/**
 * A modular battery with no GUI.
 * 
 * @author AidanBrady
 */
public class TileEntityBattery extends TileEntityBase implements IPacketReceiver
{
	public Set<ItemStack> cachedInventory = new HashSet<ItemStack>();
	
	public SynchronizedBatteryData structure;
	
	public SynchronizedBatteryData prevStructure;
	
	public boolean clientHasStructure;
	
	public int inventoryID = BatteryManager.WILDCARD;

	@Override
	public void updateEntity()
	{
		//DO NOT SUPER, CALCLAVIA!
		
		ticks++;
		
		if(worldObj.isRemote)
		{
			if(structure == null)
			{
				structure = new SynchronizedBatteryData();
			}
		}
		
		if(playersUsing.size() > 0 && ((worldObj.isRemote && !clientHasStructure) || (!worldObj.isRemote && structure == null)))
		{
			for(EntityPlayer player : playersUsing)
			{
				player.closeScreen();
			}
		}
		
		if(!worldObj.isRemote)
		{
			if(inventoryID != -1 && structure == null)
			{
				BatteryManager.updateCache(inventoryID, cachedInventory, this);
			}
			
			if(structure == null && ticks == 5)
			{
				update();
			}
			
			if(prevStructure != structure)
			{
				PacketHandler.sendTileEntityPacketToClients(this, getNetworkedData(new ArrayList()));
			}
			
			prevStructure = structure;
			
			if(structure != null)
			{
				structure.didTick = false;
				
				if(inventoryID != -1)
				{
					BatteryManager.updateCache(inventoryID, structure.inventory, this);
					
					cachedInventory = structure.inventory;
				}
			}
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(structure == null)
        {
	        inventoryID = nbtTags.getInteger("inventoryID");
	
	        if(inventoryID != BatteryManager.WILDCARD)
	        {
	            NBTTagList tagList = nbtTags.getTagList("Items");
	            cachedInventory = new HashSet<ItemStack>();

	            for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
	            {
	                NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(tagCount);

                    cachedInventory.add(ItemStack.loadItemStackFromNBT(tagCompound));
	            }
	        }
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("inventoryID", inventoryID);
        
        NBTTagList tagList = new NBTTagList();

        for(ItemStack itemStack : cachedInventory)
        {
            if(itemStack != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                itemStack.writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);
    }
	
	public void update()
	{
		if(!worldObj.isRemote && (structure == null || !structure.didTick))
		{
			new BatteryUpdateProtocol(this).updateBatteries();
			
			if(structure != null)
			{
				structure.didTick = true;
			}
		}
	}
	
	/**
	 * @return added energy
	 */
	public float addEnergy(float amount)
	{
		//go from top to bottom
		return 0;
	}
	
	/**
	 * @return removed energy
	 */
	public float removeEnergy(float amount)
	{
		Set inverse = SetUtil.inverse(structure.inventory);
		//go from bottom to top
		return 0;
	}

	public float getMaxEnergyStored()
	{
		float max = 0;

		for (ItemStack itemStack : cachedInventory)
		{
			if (itemStack != null)
			{
				if (itemStack.getItem() instanceof IBattery)
				{
					max += ((IBattery) itemStack.getItem()).getMaxEnergyStored();
				}
			}
		}

		return max;
	}
	
	public float getEnergyStored()
	{
		float energy = 0;
		
		for (ItemStack itemStack : cachedInventory)
		{
			if (itemStack != null)
			{
				if (itemStack.getItem() instanceof IBattery)
				{
					energy += ((IBattery) itemStack.getItem()).getEnergyStored(itemStack);
				}
			}
		}
		
		return energy;
	}

	@Override
	public void handle(ByteArrayDataInput input) 
	{
		try {
			if(structure == null)
			{
				structure = new SynchronizedBatteryData();
			}
			
			clientHasStructure = input.readBoolean();
			
			if(clientHasStructure)
			{
				structure.height = input.readInt();
				structure.length = input.readInt();
				structure.width = input.readInt();
			}
		} catch(Exception e) {}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(structure != null);
		
		if(structure != null)
		{
			data.add(structure.height);
			data.add(structure.length);
			data.add(structure.width);
		}
		
		return data;
	}
}
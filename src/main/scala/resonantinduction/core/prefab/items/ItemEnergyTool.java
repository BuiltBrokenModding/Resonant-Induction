package resonantinduction.core.prefab.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import resonant.lib.render.EnumColor;
import resonant.lib.utility.LanguageUtility;
import resonant.lib.utility.nbt.NBTUtility;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.item.IEnergyItem;
import universalelectricity.api.item.IVoltageItem;

/** Prefab for all eletric based tools
 * 
 * @author DarkGurdsman */
@UniversalClass
public class ItemEnergyTool extends ItemTool implements IEnergyItem, IVoltageItem
{
    /** Default battery size */
    protected long batterySize = 500000;
    /** Does this item support energy tiers */
    protected boolean hasTier = false;
    /** Display energy in tool tips */
    protected boolean showEnergy = true;
    /** Number of energy tiers */
    protected int energyTiers = 0;

    public ItemEnergyTool(int id)
    {
        super(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
    {
        if (hasTier)
            list.add(LanguageUtility.getLocal("tooltip.tier") + ": " + (getTier(itemStack) + 1));

        if (showEnergy)
        {
            String color = "";
            long joules = this.getEnergy(itemStack);

            if (joules <= this.getEnergyCapacity(itemStack) / 3)
            {
                color = "\u00a74";
            }
            else if (joules > this.getEnergyCapacity(itemStack) * 2 / 3)
            {
                color = "\u00a72";
            }
            else
            {
                color = "\u00a76";
            }
            list.add(LanguageUtility.getLocal("tooltip.battery.energy").replace("%0", color).replace("%1", EnumColor.GREY.toString()).replace("%v0", UnitDisplay.getDisplayShort(joules, Unit.JOULES)).replace("%v1", UnitDisplay.getDisplayShort(this.getEnergyCapacity(itemStack), Unit.JOULES)));
        }
    }

    @Override
    public void onCreated(ItemStack itemStack, World world, EntityPlayer player)
    {
        super.onCreated(itemStack, world, player);
        this.setEnergy(itemStack, 0);
    }

    @Override
    public long recharge(ItemStack itemStack, long energy, boolean doReceive)
    {
        long rejectedElectricity = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0);
        long energyToReceive = Math.min(energy - rejectedElectricity, getTransferRate(itemStack));

        if (doReceive)
        {
            this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive);
        }

        return energyToReceive;
    }

    @Override
    public long discharge(ItemStack itemStack, long energy, boolean doTransfer)
    {
        long energyToExtract = Math.min(Math.min(this.getEnergy(itemStack), energy), getTransferRate(itemStack));

        if (doTransfer)
        {
            this.setEnergy(itemStack, this.getEnergy(itemStack) - energyToExtract);
        }

        return energyToExtract;
    }

    @Override
    public long getVoltage(ItemStack itemStack)
    {
        return UniversalElectricity.DEFAULT_VOLTAGE;
    }

    @Override
    public void setEnergy(ItemStack itemStack, long joules)
    {
        long electricityStored = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0);
        NBTUtility.getNBTTagCompound(itemStack).setLong("electricity", electricityStored);
    }

    public long getEnergySpace(ItemStack itemStack)
    {
        return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack);
    }

    /** Gets the energy stored in the item. Energy is stored using item NBT */
    @Override
    public long getEnergy(ItemStack itemStack)
    {
        return NBTUtility.getNBTTagCompound(itemStack).getLong("electricity");
    }

    @Override
    public int getDisplayDamage(ItemStack stack)
    {
        return (int) (100 - ((double) this.getEnergy(stack) / (double) getEnergyCapacity(stack)) * 100);
    }

    @Override
    public long getEnergyCapacity(ItemStack theItem)
    {
        return this.batterySize;
    }

    public long getTransferRate(ItemStack itemStack)
    {
        return this.getEnergyCapacity(itemStack) / 100;
    }

    public static ItemStack setTier(ItemStack itemStack, int tier)
    {
        NBTUtility.getNBTTagCompound(itemStack).setByte("tier", (byte) tier);
        return itemStack;
    }

    public static byte getTier(ItemStack itemStack)
    {
        return NBTUtility.getNBTTagCompound(itemStack).getByte("tier");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int i = 0; i >= 0 && i < this.energyTiers; i++)
        {
            par3List.add(CompatibilityModule.getItemWithCharge(setTier(new ItemStack(this), i), 0));
            par3List.add(CompatibilityModule.getItemWithCharge(setTier(new ItemStack(this), i), this.getEnergyCapacity(setTier(new ItemStack(this), i))));
        }
    }
}

package dark.BasicUtilities.pipes;

import universalelectricity.prefab.implement.IRedstoneReceptor;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import dark.BasicUtilities.api.IReadOut;
import dark.BasicUtilities.api.ITankOutputer;
import dark.BasicUtilities.api.Liquid;
import dark.BasicUtilities.api.MHelper;

public class TileEntityEValve extends TileEntity implements ITankOutputer, IReadOut, IRedstoneReceptor
{
    public Liquid type = Liquid.DEFUALT;
    public LiquidTank tank = new LiquidTank(LiquidContainerRegistry.BUCKET_VOLUME);
    public TileEntity[] connected = new TileEntity[6];
    private int count = 0;
    private boolean isPowered = false;

    public void updateEntity()
    {
        super.updateEntity();
        this.connected = MHelper.getSourounding(worldObj, xCoord, yCoord, zCoord);
        if (!this.worldObj.isRemote && count++ == 10 && !isPowered)
        {
            if (tank.getLiquid() == null)
            {
                tank.setLiquid(Liquid.getStack(this.type, 1));
            }
            if (tank.getLiquid() != null && tank.getLiquid().amount < tank.getCapacity())
            {
                for (int i = 0; i < 6; i++)
                {
                    ForgeDirection dir = ForgeDirection.getOrientation(i);
                    if (connected[i] instanceof ITankContainer && !(connected[i] instanceof TileEntityPipe))
                    {
                       // FMLLog.warning("container");
                        ILiquidTank[] tanks = ((ITankContainer) connected[i]).getTanks(dir);
                        for (int t = 0; t < tanks.length; t++)
                        {
                            LiquidStack ll = tanks[t].getLiquid();
                            if (ll != null && Liquid.isStackEqual(ll, this.type))
                            {
                               // FMLLog.warning("draining");
                                int drainVol = this.tank.getCapacity();
                                if (this.tank.getLiquid() != null) drainVol = tank.getCapacity() - tank.getLiquid().amount;
                                LiquidStack drained = ((ITankContainer) connected[i]).drain(t, this.tank.getCapacity(), true);
                                int f = this.tank.fill(drained, true);
                                //FMLLog.warning("leftOver " + f);
                            }
                        }
                    }
                }
            }
            count = 0;
            LiquidStack stack = tank.getLiquid();
            if (stack != null && this.canOutput())
                for (int i = 0; i < 6; i++)
                {

                    if (connected[i] instanceof TileEntityPipe)
                    {
                        //FMLLog.warning("moving to pipe");
                        int ee = ((TileEntityPipe) connected[i]).fill(ForgeDirection.getOrientation(i), stack, true);
                        tank.drain(ee, true);
                    }

                }
        }
    }

    private boolean canOutput()
    {
        // TODO add redstone input here to cause it to stop outputting
        return true;
    }

    @Override
    public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
    {
        return 0;
    }

    @Override
    public int fill(int tankIndex, LiquidStack resource, boolean doFill)
    {
        if (tankIndex != 0 || resource == null) return 0;
        return tank.fill(resource, doFill);
    }

    @Override
    public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return null;
    }

    @Override
    public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
    {
        return null;
    }

    @Override
    public ILiquidTank[] getTanks(ForgeDirection direction)
    {
        return new ILiquidTank[] { this.tank };
    }

    @Override
    public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
    {
        return null;
    }

    public int presureOutput(Liquid type, ForgeDirection dir)
    {
        if (type == this.type) { return type.defaultPresure; }
        return 0;
    }

    @Override
    public boolean canPressureToo(Liquid type, ForgeDirection dir)
    {
        if (type == this.type) return true;
        return false;
    }

    @Override
    public String getMeterReading(EntityPlayer user, ForgeDirection side)
    {
        String output = "";
        LiquidStack stack = tank.getLiquid();
        if (stack != null) output += (stack.amount / LiquidContainerRegistry.BUCKET_VOLUME) + " " + this.type.displayerName+" on = "+!this.isPowered;
        if (stack != null) return output;

        return "0/0 " + this.type.displayerName+" on = "+!this.isPowered;
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.type = Liquid.getLiquid(par1NBTTagCompound.getInteger("type"));
        int vol = par1NBTTagCompound.getInteger("liquid");
        this.tank.setLiquid(Liquid.getStack(type, vol));
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        int s = 0;
        LiquidStack stack = this.tank.getLiquid();
        if (stack != null) s = stack.amount;
        par1NBTTagCompound.setInteger("liquid", s);
        par1NBTTagCompound.setInteger("type", this.type.ordinal());
    }

    public void setType(Liquid dm)
    {
        this.type = dm;

    }

    @Override
    public void onPowerOn()
    {
        this.isPowered = true;

    }

    @Override
    public void onPowerOff()
    {
        this.isPowered = false;

    }

}

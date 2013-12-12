package dark.machines;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.prefab.TranslationHelper;
import universalelectricity.prefab.ore.OreGenReplaceStone;
import universalelectricity.prefab.ore.OreGenerator;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import dark.api.reciepes.MachineRecipeHandler;
import dark.core.DMCreativeTab;
import dark.core.ModObjectRegistry;
import dark.core.basics.BlockGasOre;
import dark.core.basics.BlockOre;
import dark.core.basics.BlockOre.OreData;
import dark.core.basics.EnumMaterial;
import dark.core.basics.EnumOrePart;
import dark.core.basics.GasOreGenerator;
import dark.core.basics.ItemBlockOre;
import dark.core.basics.ItemCommonTool;
import dark.core.basics.ItemOreDirv;
import dark.core.basics.ItemParts;
import dark.core.basics.ItemParts.Parts;
import dark.core.helpers.PacketDataWatcher;
import dark.core.network.PacketHandler;
import dark.core.prefab.ItemBlockHolder;
import dark.core.prefab.ModPrefab;
import dark.core.prefab.entities.EntityTestCar;
import dark.core.prefab.entities.ItemVehicleSpawn;
import dark.core.prefab.fluids.EnumGas;
import dark.core.prefab.machine.BlockMulti;
import dark.core.prefab.machine.TileEntityNBTContainer;
import dark.machines.deco.BlockBasalt;
import dark.machines.deco.BlockColorGlass;
import dark.machines.deco.BlockColorGlowGlass;
import dark.machines.deco.BlockColorSand;
import dark.machines.deco.ItemBlockColored;
import dark.machines.generators.BlockSmallSteamGen;
import dark.machines.generators.BlockSolarPanel;
import dark.machines.items.ItemBattery;
import dark.machines.items.ItemColoredDust;
import dark.machines.items.ItemFluidCan;
import dark.machines.items.ItemReadoutTools;
import dark.machines.items.ItemWrench;
import dark.machines.machines.BlockDebug;
import dark.machines.machines.BlockEnergyStorage;
import dark.machines.machines.ItemBlockEnergyStorage;
import dark.machines.transmit.BlockWire;
import dark.machines.transmit.ItemBlockWire;

/** @author HangCow, DarkGuardsman */
@Mod(modid = DarkMain.MOD_ID, name = DarkMain.MOD_NAME, version = DarkMain.VERSION, dependencies = "after:BuildCraft|Energy", useMetadata = true)
@NetworkMod(channels = { DarkMain.CHANNEL }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class DarkMain extends ModPrefab
{
    // @Mod Prerequisites
    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVIS_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVIS_VERSION + "." + BUILD_VERSION;

    // @Mod
    public static final String MOD_ID = "DarkCore";
    public static final String MOD_NAME = "Darks CoreMachine";

    @SidedProxy(clientSide = "dark.machines.client.ClientProxy", serverSide = "dark.machines.CommonProxy")
    public static CommonProxy proxy;

    public static final String CHANNEL = "DarkPackets";

    @Metadata(DarkMain.MOD_ID)
    public static ModMetadata meta;

    /** Main config file */
    public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "Dark/TheDarkMachine.cfg"));
    private static final String[] LANGUAGES_SUPPORTED = new String[] { "en_US" };
    /** Can over pressure of devices do area damage */
    public static boolean overPressureDamage, zeroRendering, zeroAnimation, zeroGraphics;

    public static BlockMulti blockMulti;

    @Instance(MOD_ID)
    private static DarkMain instance;

    public static CoreRecipeLoader recipeLoader;

    public static final String[] dyeColorNames = new String[] { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "Silver", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White" };
    public static final Color[] dyeColors = new Color[] { Color.black, Color.red, Color.green, new Color(139, 69, 19), Color.BLUE, new Color(75, 0, 130), Color.cyan, new Color(192, 192, 192), Color.gray, Color.pink, new Color(0, 255, 0), Color.yellow, new Color(135, 206, 250), Color.magenta, Color.orange, Color.white };

    public static DarkMain getInstance()
    {
        if (instance == null)
        {
            instance = new DarkMain();
        }
        return instance;
    }

    @EventHandler
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        super.preInit(event);
        NetworkRegistry.instance().registerGuiHandler(this, proxy);
        MinecraftForge.EVENT_BUS.register(PacketDataWatcher.instance);
        proxy.preInit();
    }

    @EventHandler
    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        EntityRegistry.registerGlobalEntityID(EntityTestCar.class, "TestCar", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerModEntity(EntityTestCar.class, "TestCar", 60, this, 64, 1, true);

        for (EnumGas gas : EnumGas.values())
        {
            FluidRegistry.registerFluid(gas.getGas());
        }
        if (CoreRecipeLoader.blockGas != null)
        {
            EnumGas.NATURAL_GAS.getGas().setBlockID(CoreRecipeLoader.blockGas);
        }
        if (CoreRecipeLoader.blockGas != null)
        {
            GameRegistry.registerWorldGenerator(new GasOreGenerator());
        }
        if (CoreRecipeLoader.blockOre != null)
        {
            for (OreData data : OreData.values())
            {
                if (data.doWorldGen)
                {
                    OreGenReplaceStone gen = data.getGeneratorSettings();
                    if (gen != null)
                    {
                        OreGenerator.addOre(gen);
                    }
                }
            }
        }
        if (CoreRecipeLoader.itemParts != null)
        {
            for (Parts part : Parts.values())
            {
                OreDictionary.registerOre(part.name, new ItemStack(CoreRecipeLoader.itemParts, 1, part.ordinal()));
            }
        }
        if (CoreRecipeLoader.itemMetals != null)
        {
            MinecraftForge.EVENT_BUS.register(CoreRecipeLoader.itemMetals);
        }
        FMLLog.info(" Loaded: " + TranslationHelper.loadLanguages(LANGUAGE_PATH, LANGUAGES_SUPPORTED) + " Languages.");
        proxy.init();
    }

    @EventHandler
    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
        proxy.postInit();
        if (CoreRecipeLoader.itemParts instanceof ItemParts)
        {
            DMCreativeTab.tabMining.itemStack = new ItemStack(CoreRecipeLoader.itemParts.itemID, 1, ItemParts.Parts.MiningIcon.ordinal());
        }
        if (CoreRecipeLoader.itemMetals instanceof ItemOreDirv)
        {
            DMCreativeTab.tabIndustrial.itemStack = EnumMaterial.getStack(EnumMaterial.IRON, EnumOrePart.GEARS, 1);
        }
        MachineRecipeHandler.parseOreNames(CONFIGURATION);
        CONFIGURATION.save();
    }

    @Override
    public void registerObjects()
    {
        if (recipeLoader == null)
        {
            recipeLoader = new CoreRecipeLoader();
        }
        /* CONFIGS */
        CONFIGURATION.load();

        GameRegistry.registerTileEntity(TileEntityNBTContainer.class, "DMNBTSaveBlock");

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            DarkMain.zeroAnimation = CONFIGURATION.get("Graphics", "DisableAllAnimation", false, "Disables active animations by any non-active models").getBoolean(false);
            DarkMain.zeroRendering = CONFIGURATION.get("Graphics", "DisableAllRendering", false, "Replaces all model renderers with single block forms").getBoolean(false);
            DarkMain.zeroGraphics = CONFIGURATION.get("Graphics", "DisableAllGraphics", false, "Disables extra effects that models and renders have. Such as particles, and text").getBoolean(false);
        }
        /* BLOCKS */
        Block m = ModObjectRegistry.createNewBlock("DMBlockMulti", DarkMain.MOD_ID, BlockMulti.class, false);
        if (m instanceof BlockMulti)
        {
            blockMulti = (BlockMulti) m;
        }
        CoreRecipeLoader.blockSteamGen = ModObjectRegistry.createNewBlock("DMBlockSteamMachine", DarkMain.MOD_ID, BlockSmallSteamGen.class, ItemBlockHolder.class);
        CoreRecipeLoader.blockOre = ModObjectRegistry.createNewBlock("DMBlockOre", DarkMain.MOD_ID, BlockOre.class, ItemBlockOre.class);
        CoreRecipeLoader.blockWire = ModObjectRegistry.createNewBlock("DMBlockWire", DarkMain.MOD_ID, BlockWire.class, ItemBlockWire.class);
        CoreRecipeLoader.blockDebug = ModObjectRegistry.createNewBlock("DMBlockDebug", DarkMain.MOD_ID, BlockDebug.class, ItemBlockHolder.class);
        CoreRecipeLoader.blockStainGlass = ModObjectRegistry.createNewBlock("DMBlockStainedGlass", DarkMain.MOD_ID, BlockColorGlass.class, ItemBlockColored.class);
        CoreRecipeLoader.blockColorSand = ModObjectRegistry.createNewBlock("DMBlockColorSand", DarkMain.MOD_ID, BlockColorSand.class, ItemBlockColored.class);
        CoreRecipeLoader.blockBasalt = ModObjectRegistry.createNewBlock("DMBlockBasalt", DarkMain.MOD_ID, BlockBasalt.class, ItemBlockColored.class);
        CoreRecipeLoader.blockGlowGlass = ModObjectRegistry.createNewBlock("DMBlockGlowGlass", DarkMain.MOD_ID, BlockColorGlowGlass.class, ItemBlockColored.class);
        CoreRecipeLoader.blockSolar = ModObjectRegistry.createNewBlock("DMBlockSolar", DarkMain.MOD_ID, BlockSolarPanel.class, ItemBlockHolder.class);
        CoreRecipeLoader.blockGas = ModObjectRegistry.createNewBlock("DMBlockGas", DarkMain.MOD_ID, BlockGasOre.class, ItemBlockHolder.class);
        CoreRecipeLoader.blockBatBox = ModObjectRegistry.createNewBlock("DMBlockBatBox", DarkMain.MOD_ID, BlockEnergyStorage.class, ItemBlockEnergyStorage.class);

        /* ITEMS */
        CoreRecipeLoader.itemTool = ModObjectRegistry.createNewItem("DMReadoutTools", DarkMain.MOD_ID, ItemReadoutTools.class, true);
        CoreRecipeLoader.itemMetals = ModObjectRegistry.createNewItem("DMOreDirvParts", DarkMain.MOD_ID, ItemOreDirv.class, true);
        CoreRecipeLoader.battery = ModObjectRegistry.createNewItem("DMItemBattery", DarkMain.MOD_ID, ItemBattery.class, true);
        CoreRecipeLoader.wrench = ModObjectRegistry.createNewItem("DMWrench", DarkMain.MOD_ID, ItemWrench.class, true);
        CoreRecipeLoader.itemParts = ModObjectRegistry.createNewItem("DMCraftingParts", DarkMain.MOD_ID, ItemParts.class, true);
        CoreRecipeLoader.itemGlowingSand = ModObjectRegistry.createNewItem("DMItemGlowingSand", DarkMain.MOD_ID, ItemColoredDust.class, true);
        CoreRecipeLoader.itemDiggingTool = ModObjectRegistry.createNewItem("ItemDiggingTools", DarkMain.MOD_ID, ItemCommonTool.class, true);
        CoreRecipeLoader.itemVehicleTest = ModObjectRegistry.createNewItem("ItemVehicleTest", DarkMain.MOD_ID, ItemVehicleSpawn.class, true);
        CoreRecipeLoader.itemFluidCan = ModObjectRegistry.createNewItem("ItemFluidCan", DarkMain.MOD_ID, ItemFluidCan.class, true);
        //Config saves in post init to allow for other feature to access it
       
    }

    @Override
    public void loadModMeta()
    {
        /* MCMOD.INFO FILE BUILDER? */
        meta.modId = MOD_ID;
        meta.name = MOD_NAME;
        meta.description = "Main mod for several of the mods created by DarkGuardsman and his team. Adds basic features, functions, ores, items, and blocks";
        meta.url = "http://www.universalelectricity.com/coremachine";

        meta.logoFile = TEXTURE_DIRECTORY + "GP_Banner.png";
        meta.version = VERSION;
        meta.authorList = Arrays.asList(new String[] { "DarkGuardsman", "HangCow", "Elrath18", "Archadia" });
        meta.credits = "Please see the website.";
        meta.autogenerated = false;
    }

    @ForgeSubscribe
    public void onWorldSave(WorldEvent.Save event)
    {
        //SaveManager.save(!event.world.isRemote);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        //SaveManager.save(true);
    }

    @Override
    public String getDomain()
    {
        return "dark";
    }

    @Override
    public void loadRecipes()
    {
        if (recipeLoader == null)
        {
            recipeLoader = new CoreRecipeLoader();
        }
        recipeLoader.loadRecipes();
    }

}

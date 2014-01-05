package resonantinduction;

import resonantinduction.transport.transformer.PartTransformer;
import resonantinduction.transport.wire.flat.PartFlatSwitchWire;
import resonantinduction.transport.wire.flat.PartFlatWire;
import resonantinduction.transport.wire.framed.PartFramedSwitchWire;
import resonantinduction.transport.wire.framed.PartFramedWire;
import resonantinduction.utility.multimeter.PartMultimeter;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartRI implements IPartFactory
{
	public static MultipartRI INSTANCE;

	public static final String[] PART_TYPES = { "resonant_induction_wire", "resonant_induction_switch_wire", "resonant_induction_flat_wire", "resonant_induction_flat_switch_wire", "resonant_induction_multimeter", "resonant_induction_transformer" };

	public MultipartRI()
	{
		MultiPartRegistry.registerParts(this, PART_TYPES);
		MultipartGenerator.registerTrait("universalelectricity.api.energy.IConductor", "resonantinduction.transport.wire.trait.TraitConductor");
		MultipartGenerator.registerTrait("cofh.api.energy.IEnergyHandler", "resonantinduction.transport.wire.trait.TraitEnergyHandler");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.transport.wire.trait.TraitEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name == "resonant_induction_wire")
		{
			return new PartFramedWire();
		}
		else if (name == "resonant_induction_switch_wire")
		{
			return new PartFramedSwitchWire();
		}

		else if (name == "resonant_induction_flat_wire")
		{
			return new PartFlatWire();
		}
		else if (name == "resonant_induction_flat_switch_wire")
		{
			return new PartFlatSwitchWire();
		}
		else if (name == "resonant_induction_multimeter")
		{
			return new PartMultimeter();
		}
		else if (name == "resonant_induction_transformer")
		{
			return new PartTransformer();
		}

		return null;
	}
}

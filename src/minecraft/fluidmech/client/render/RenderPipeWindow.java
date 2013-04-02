package fluidmech.client.render;

import hydraulic.api.ColorCode;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;

import fluidmech.client.model.ModelLargePipe;
import fluidmech.client.render.pipeextentions.IPipeExtentionRender;
import fluidmech.common.FluidMech;
import fluidmech.common.machines.pipes.IPipeExtention;
import fluidmech.common.machines.pipes.TileEntityPipe;
import fluidmech.common.machines.pipes.TileEntityPipe;

public class RenderPipeWindow implements IPipeExtentionRender
{
	private ModelLargePipe SixPipe;
	private boolean[] renderSide = new boolean[6];

	public RenderPipeWindow()
	{
		SixPipe = new ModelLargePipe();
	}

	@Override
	public void renderAModelAt(RenderPipe renderPipe, TileEntityPipe pipe, Vector3 location, float size, ForgeDirection facingDirection)
	{
		renderPipe.bindTextureForPipe(renderPipe.getPipeTexture(0));
		this.render(facingDirection.ordinal());
		System.out.println("Rendered Window Pipe");

	}

	public void render(int side)
	{
		switch (side)
		{
			case 0:
				SixPipe.renderBottom();
				break;
			case 1:
				SixPipe.renderTop();
				break;
			case 3:
				SixPipe.renderFront();
				break;
			case 2:
				SixPipe.renderBack();
				break;
			case 5:
				SixPipe.renderRight();
				break;
			case 4:
				SixPipe.renderLeft();
				break;
		}
	}

}
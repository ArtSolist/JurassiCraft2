package org.jurassicraft.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jurassicraft.server.container.DNAExtractorContainer;

@SideOnly(Side.CLIENT)
public class DNAExtractorGui extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("jurassicraft:textures/gui/dna_extractor.png");
    private final InventoryPlayer playerInventory;
    private IInventory extractor;

    public DNAExtractorGui(InventoryPlayer playerInv, IInventory dnaSequencer) {
        super(new DNAExtractorContainer(playerInv, (TileEntity) dnaSequencer));
        this.playerInventory = playerInv;
        this.extractor = dnaSequencer;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = this.extractor.getDisplayName().getUnformattedText();
        this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
        this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        int progress = this.getProgress(24);
        this.drawTexturedModalRect(x + 77, y + 36, 176, 0, progress + 1, 16);
    }

    private int getProgress(int scale) {
        int j = this.extractor.getField(0);
        int k = this.extractor.getField(1);
        return k != 0 && j != 0 ? j * scale / k : 0;
    }
}

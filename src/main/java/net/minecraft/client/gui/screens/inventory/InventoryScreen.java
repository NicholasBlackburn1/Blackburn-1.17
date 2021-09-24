package net.minecraft.client.gui.screens.inventory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.blackburn.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;



public class InventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener
{   
    public static ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION;
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private float xMouse;
    private float yMouse;
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean recipeBookComponentInitialized;
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    public InventoryScreen(Player p_98839_)
    {
        super(p_98839_.inventoryMenu, p_98839_.getInventory(), new TranslatableComponent("container.crafting"));
        this.passEvents = true;
        this.titleLabelX = 97;
    }

    public void m_181908_()
    {
        if (this.minecraft.gameMode.hasInfiniteItems())
        {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
        }
        else
        {
            this.recipeBookComponent.tick();
        }
    }

    public static void registerTextures(Minecraft pMc)
    {

        // ALLOWS ME TO Swich the loading image based on day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        switch (calendar.get(Calendar.DAY_OF_WEEK) ) {
            case 1:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo.png");
              
                break;

            case 2:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo2.png");
                break;

            case 3:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo3.png");
                break;

            case 4:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo4.png");
                break;

            case 5:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo5.png");
                break;
                
            case 6:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo6.png");
                break;
            
                       
            case 7:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo7.png");
                break;
        
            
        
            default:
                MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo.png");
                break;
        }
        
        pMc.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LogoTexture());
    }
    
    

    protected void init()
    {
        if (this.minecraft.gameMode.hasInfiniteItems())
        {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
        }
        else
        {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.recipeBookComponentInitialized = true;
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_98880_) ->
            {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                ((ImageButton)p_98880_).setPosition(this.leftPos + 104, this.height / 2 - 22);
                this.buttonClicked = true;
            }));
            this.addWidget(this.recipeBookComponent);
            registerTextures(minecraft);
            this.setInitialFocus(this.recipeBookComponent);
        }
    }

    protected void renderLabels(PoseStack pMatrixStack, int pX, int pY)
    {
        this.font.draw(pMatrixStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {   
        blit(pMatrixStack,Const.px, Const.py, Const.pUOffset, Const.pVOffset, Const.pWidth, Const.pHight,Const.pTextureWidth,Const.pTextureHeight);
        this.renderBackground(pMatrixStack);
        this.doRenderEffects = !this.recipeBookComponent.isVisible();

        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow)
        {
            this.renderBg(pMatrixStack, pPartialTicks, pMouseX, pMouseY);
            this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        }
        else
        {
            this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            this.recipeBookComponent.renderGhostRecipe(pMatrixStack, this.leftPos, this.topPos, false, pPartialTicks);
        }

        this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
        this.recipeBookComponent.renderTooltip(pMatrixStack, this.leftPos, this.topPos, pMouseX, pMouseY);
        this.xMouse = (float)pMouseX;
        this.yMouse = (float)pMouseY;
    }

    protected void renderBg(PoseStack pMatrixStack, float pPartialTicks, int pX, int pY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventory(i + 51, j + 75, 30, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, float pMouseX, float pMouseY, LivingEntity pLivingEntity)
    {
        float f = (float)Math.atan((double)(pMouseX / 40.0F));
        float f1 = (float)Math.atan((double)(pMouseY / 40.0F));
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)pPosX, (double)pPosY, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float)pScale, (float)pScale, (float)pScale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion);
        float f2 = pLivingEntity.yBodyRot;
        float f3 = pLivingEntity.getYRot();
        float f4 = pLivingEntity.getXRot();
        float f5 = pLivingEntity.yHeadRotO;
        float f6 = pLivingEntity.yHeadRot;
        pLivingEntity.yBodyRot = 180.0F + f * 20.0F;
        pLivingEntity.setYRot(180.0F + f * 40.0F);
        pLivingEntity.setXRot(-f1 * 20.0F);
        pLivingEntity.yHeadRot = pLivingEntity.getYRot();
        pLivingEntity.yHeadRotO = pLivingEntity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() ->
        {
            entityrenderdispatcher.render(pLivingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        pLivingEntity.yBodyRot = f2;
        pLivingEntity.setYRot(f3);
        pLivingEntity.setXRot(f4);
        pLivingEntity.yHeadRotO = f5;
        pLivingEntity.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double p_98863_)
    {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, p_98863_);
    }

    public boolean mouseClicked(double pMouseX, double p_98842_, int pMouseY)
    {
        if (this.recipeBookComponent.mouseClicked(pMouseX, p_98842_, pMouseY))
        {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        else
        {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(pMouseX, p_98842_, pMouseY);
        }
    }

    public boolean mouseReleased(double pMouseX, double p_98894_, int pMouseY)
    {
        if (this.buttonClicked)
        {
            this.buttonClicked = false;
            return true;
        }
        else
        {
            return super.mouseReleased(pMouseX, p_98894_, pMouseY);
        }
    }

    protected boolean hasClickedOutside(double pMouseX, double p_98846_, int pMouseY, int p_98848_, int pGuiLeft)
    {
        boolean flag = pMouseX < (double)pMouseY || p_98846_ < (double)p_98848_ || pMouseX >= (double)(pMouseY + this.imageWidth) || p_98846_ >= (double)(p_98848_ + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(pMouseX, p_98846_, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pGuiLeft) && flag;
    }

    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType)
    {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        this.recipeBookComponent.slotClicked(pSlot);
    }

    public void recipesUpdated()
    {
        this.recipeBookComponent.recipesUpdated();
    }

    public void removed()
    {
        if (this.recipeBookComponentInitialized)
        {
            this.recipeBookComponent.removed();
        }

        super.removed();
    }

    public RecipeBookComponent getRecipeBookComponent()
    {
        return this.recipeBookComponent;
    }

    private static InputStream getLogoInputStream(ResourceManager resourceManager, VanillaPackResources vanillapack) throws IOException
        {
            return resourceManager.hasResource(MOJANG_STUDIOS_LOGO_LOCATION) ? resourceManager.getResource(MOJANG_STUDIOS_LOGO_LOCATION).getInputStream() : vanillapack.getResource(PackType.CLIENT_RESOURCES, MOJANG_STUDIOS_LOGO_LOCATION);
        }

        
    static class LogoTexture extends SimpleTexture
    {
        public LogoTexture()
        {
            super(MOJANG_STUDIOS_LOGO_LOCATION);
        }

        protected SimpleTexture.TextureImage getTextureImage(ResourceManager pResourceManager)
        {
            Minecraft minecraft = Minecraft.getInstance();
            VanillaPackResources vanillapackresources = minecraft.getClientPackSource().getVanillaPack();

            try
            {
                InputStream inputstream = getLogoInputStream(pResourceManager, vanillapackresources);
                SimpleTexture.TextureImage simpletexture$textureimage;

                try
                {
                    simpletexture$textureimage = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
                }
                catch (Throwable throwable1)
                {
                    if (inputstream != null)
                    {
                        try
                        {
                            inputstream.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                if (inputstream != null)
                {
                    inputstream.close();
                }

                return simpletexture$textureimage;
            }
            catch (IOException ioexception1)
            {
                return new SimpleTexture.TextureImage(ioexception1);
            }
        }
    }
}

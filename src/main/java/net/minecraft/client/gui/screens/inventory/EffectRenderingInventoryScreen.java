package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
   protected boolean doRenderEffects;

   public EffectRenderingInventoryScreen(T p_98701_, Inventory p_98702_, Component p_98703_) {
      super(p_98701_, p_98702_, p_98703_);
   }

   protected void init() {
      super.init();
      this.checkEffectRendering();
   }

   protected void checkEffectRendering() {
      if (this.minecraft.player.getActiveEffects().isEmpty()) {
         this.leftPos = (this.width - this.imageWidth) / 2;
         this.doRenderEffects = false;
      } else {
         this.leftPos = 160 + (this.width - this.imageWidth - 200) / 2;
         this.doRenderEffects = true;
      }

   }

   public void render(PoseStack p_98705_, int p_98706_, int p_98707_, float p_98708_) {
      super.render(p_98705_, p_98706_, p_98707_, p_98708_);
      if (this.doRenderEffects) {
         this.renderEffects(p_98705_);
      }

   }

   private void renderEffects(PoseStack p_98716_) {
      int i = this.leftPos - 124;
      Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
      if (!collection.isEmpty()) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         int j = 33;
         if (collection.size() > 5) {
            j = 132 / (collection.size() - 1);
         }

         Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
         this.renderBackgrounds(p_98716_, i, j, iterable);
         this.renderIcons(p_98716_, i, j, iterable);
         this.renderLabels(p_98716_, i, j, iterable);
      }
   }

   private void renderBackgrounds(PoseStack p_98710_, int p_98711_, int p_98712_, Iterable<MobEffectInstance> p_98713_) {
      RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
      int i = this.topPos;

      for(MobEffectInstance mobeffectinstance : p_98713_) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.blit(p_98710_, p_98711_, i, 0, 166, 140, 32);
         i += p_98712_;
      }

   }

   private void renderIcons(PoseStack p_98718_, int p_98719_, int p_98720_, Iterable<MobEffectInstance> p_98721_) {
      MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
      int i = this.topPos;

      for(MobEffectInstance mobeffectinstance : p_98721_) {
         MobEffect mobeffect = mobeffectinstance.getEffect();
         TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
         RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
         blit(p_98718_, p_98719_ + 6, i + 7, this.getBlitOffset(), 18, 18, textureatlassprite);
         i += p_98720_;
      }

   }

   private void renderLabels(PoseStack p_98723_, int p_98724_, int p_98725_, Iterable<MobEffectInstance> p_98726_) {
      int i = this.topPos;

      for(MobEffectInstance mobeffectinstance : p_98726_) {
         String s = I18n.get(mobeffectinstance.getEffect().getDescriptionId());
         if (mobeffectinstance.getAmplifier() >= 1 && mobeffectinstance.getAmplifier() <= 9) {
            s = s + " " + I18n.get("enchantment.level." + (mobeffectinstance.getAmplifier() + 1));
         }

         this.font.drawShadow(p_98723_, s, (float)(p_98724_ + 10 + 18), (float)(i + 6), 16777215);
         String s1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F);
         this.font.drawShadow(p_98723_, s1, (float)(p_98724_ + 10 + 18), (float)(i + 6 + 10), 8355711);
         i += p_98725_;
      }

   }
}
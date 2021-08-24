package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
   public KeyMapping selectedKey;
   public long lastKeySelection;
   private ControlList controlList;
   private Button resetButton;

   public ControlsScreen(Screen p_97519_, Options p_97520_) {
      super(p_97519_, p_97520_, new TranslatableComponent("controls.title"));
   }

   protected void init() {
      this.addRenderableWidget(new Button(this.width / 2 - 155, 18, 150, 20, new TranslatableComponent("options.mouse_settings"), (p_97540_) -> {
         this.minecraft.setScreen(new MouseSettingsScreen(this, this.options));
      }));
      this.addRenderableWidget(Option.AUTO_JUMP.createButton(this.options, this.width / 2 - 155 + 160, 18, 150));
      this.controlList = new ControlList(this, this.minecraft);
      this.addWidget(this.controlList);
      this.resetButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("controls.resetAll"), (p_97538_) -> {
         for(KeyMapping keymapping : this.options.keyMappings) {
            keymapping.setKey(keymapping.getDefaultKey());
         }

         KeyMapping.resetMapping();
      }));
      this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (p_97535_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public boolean mouseClicked(double p_97522_, double p_97523_, int p_97524_) {
      if (this.selectedKey != null) {
         this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(p_97524_));
         this.selectedKey = null;
         KeyMapping.resetMapping();
         return true;
      } else {
         return super.mouseClicked(p_97522_, p_97523_, p_97524_);
      }
   }

   public boolean keyPressed(int p_97526_, int p_97527_, int p_97528_) {
      if (this.selectedKey != null) {
         if (p_97526_ == 256) {
            this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
         } else {
            this.options.setKey(this.selectedKey, InputConstants.getKey(p_97526_, p_97527_));
         }

         this.selectedKey = null;
         this.lastKeySelection = Util.getMillis();
         KeyMapping.resetMapping();
         return true;
      } else {
         return super.keyPressed(p_97526_, p_97527_, p_97528_);
      }
   }

   public void render(PoseStack p_97530_, int p_97531_, int p_97532_, float p_97533_) {
      this.renderBackground(p_97530_);
      this.controlList.render(p_97530_, p_97531_, p_97532_, p_97533_);
      drawCenteredString(p_97530_, this.font, this.title, this.width / 2, 8, 16777215);
      boolean flag = false;

      for(KeyMapping keymapping : this.options.keyMappings) {
         if (!keymapping.isDefault()) {
            flag = true;
            break;
         }
      }

      this.resetButton.active = flag;
      super.render(p_97530_, p_97531_, p_97532_, p_97533_);
   }
}
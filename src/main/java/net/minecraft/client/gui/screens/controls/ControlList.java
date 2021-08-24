package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public class ControlList extends ContainerObjectSelectionList<ControlList.Entry> {
   final ControlsScreen controlsScreen;
   int maxNameWidth;

   public ControlList(ControlsScreen p_97399_, Minecraft p_97400_) {
      super(p_97400_, p_97399_.width + 45, p_97399_.height, 43, p_97399_.height - 32, 20);
      this.controlsScreen = p_97399_;
      KeyMapping[] akeymapping = ArrayUtils.clone(p_97400_.options.keyMappings);
      Arrays.sort((Object[])akeymapping);
      String s = null;

      for(KeyMapping keymapping : akeymapping) {
         String s1 = keymapping.getCategory();
         if (!s1.equals(s)) {
            s = s1;
            this.addEntry(new ControlList.CategoryEntry(new TranslatableComponent(s1)));
         }

         Component component = new TranslatableComponent(keymapping.getName());
         int i = p_97400_.font.width(component);
         if (i > this.maxNameWidth) {
            this.maxNameWidth = i;
         }

         this.addEntry(new ControlList.KeyEntry(keymapping, component));
      }

   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 15;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 32;
   }

   @OnlyIn(Dist.CLIENT)
   public class CategoryEntry extends ControlList.Entry {
      final Component name;
      private final int width;

      public CategoryEntry(Component p_97428_) {
         this.name = p_97428_;
         this.width = ControlList.this.minecraft.font.width(this.name);
      }

      public void render(PoseStack p_97430_, int p_97431_, int p_97432_, int p_97433_, int p_97434_, int p_97435_, int p_97436_, int p_97437_, boolean p_97438_, float p_97439_) {
         ControlList.this.minecraft.font.draw(p_97430_, this.name, (float)(ControlList.this.minecraft.screen.width / 2 - this.width / 2), (float)(p_97432_ + p_97435_ - 9 - 1), 16777215);
      }

      public boolean changeFocus(boolean p_97442_) {
         return false;
      }

      public List<? extends GuiEventListener> children() {
         return Collections.emptyList();
      }

      public List<? extends NarratableEntry> narratables() {
         return ImmutableList.of(new NarratableEntry() {
            public NarratableEntry.NarrationPriority narrationPriority() {
               return NarratableEntry.NarrationPriority.HOVERED;
            }

            public void updateNarration(NarrationElementOutput p_169579_) {
               p_169579_.add(NarratedElementType.TITLE, CategoryEntry.this.name);
            }
         });
      }
   }

   @OnlyIn(Dist.CLIENT)
   public abstract static class Entry extends ContainerObjectSelectionList.Entry<ControlList.Entry> {
   }

   @OnlyIn(Dist.CLIENT)
   public class KeyEntry extends ControlList.Entry {
      private final KeyMapping key;
      private final Component name;
      private final Button changeButton;
      private final Button resetButton;

      KeyEntry(final KeyMapping p_97451_, final Component p_97452_) {
         this.key = p_97451_;
         this.name = p_97452_;
         this.changeButton = new Button(0, 0, 75, 20, p_97452_, (p_97479_) -> {
            ControlList.this.controlsScreen.selectedKey = p_97451_;
         }) {
            protected MutableComponent createNarrationMessage() {
               return p_97451_.isUnbound() ? new TranslatableComponent("narrator.controls.unbound", p_97452_) : new TranslatableComponent("narrator.controls.bound", p_97452_, super.createNarrationMessage());
            }
         };
         this.resetButton = new Button(0, 0, 50, 20, new TranslatableComponent("controls.reset"), (p_97475_) -> {
            ControlList.this.minecraft.options.setKey(p_97451_, p_97451_.getDefaultKey());
            KeyMapping.resetMapping();
         }) {
            protected MutableComponent createNarrationMessage() {
               return new TranslatableComponent("narrator.controls.reset", p_97452_);
            }
         };
      }

      public void render(PoseStack p_97463_, int p_97464_, int p_97465_, int p_97466_, int p_97467_, int p_97468_, int p_97469_, int p_97470_, boolean p_97471_, float p_97472_) {
         boolean flag = ControlList.this.controlsScreen.selectedKey == this.key;
         float f = (float)(p_97466_ + 90 - ControlList.this.maxNameWidth);
         ControlList.this.minecraft.font.draw(p_97463_, this.name, f, (float)(p_97465_ + p_97468_ / 2 - 9 / 2), 16777215);
         this.resetButton.x = p_97466_ + 190;
         this.resetButton.y = p_97465_;
         this.resetButton.active = !this.key.isDefault();
         this.resetButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
         this.changeButton.x = p_97466_ + 105;
         this.changeButton.y = p_97465_;
         this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
         boolean flag1 = false;
         if (!this.key.isUnbound()) {
            for(KeyMapping keymapping : ControlList.this.minecraft.options.keyMappings) {
               if (keymapping != this.key && this.key.same(keymapping)) {
                  flag1 = true;
                  break;
               }
            }
         }

         if (flag) {
            this.changeButton.setMessage((new TextComponent("> ")).append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
         } else if (flag1) {
            this.changeButton.setMessage(this.changeButton.getMessage().copy().withStyle(ChatFormatting.RED));
         }

         this.changeButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
      }

      public List<? extends GuiEventListener> children() {
         return ImmutableList.of(this.changeButton, this.resetButton);
      }

      public List<? extends NarratableEntry> narratables() {
         return ImmutableList.of(this.changeButton, this.resetButton);
      }

      public boolean mouseClicked(double p_97459_, double p_97460_, int p_97461_) {
         if (this.changeButton.mouseClicked(p_97459_, p_97460_, p_97461_)) {
            return true;
         } else {
            return this.resetButton.mouseClicked(p_97459_, p_97460_, p_97461_);
         }
      }

      public boolean mouseReleased(double p_97481_, double p_97482_, int p_97483_) {
         return this.changeButton.mouseReleased(p_97481_, p_97482_, p_97483_) || this.resetButton.mouseReleased(p_97481_, p_97482_, p_97483_);
      }
   }
}
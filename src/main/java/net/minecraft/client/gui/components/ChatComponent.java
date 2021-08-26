package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatComponent extends GuiComponent
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage<Component>> allMessages = Lists.newArrayList();
    private final List<GuiMessage<FormattedCharSequence>> trimmedMessages = Lists.newArrayList();
    private final Deque<Component> chatQueue = Queues.newArrayDeque();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private long lastMessage;
    private int lastChatWidth = 0;

    public ChatComponent(Minecraft p_93768_)
    {
        this.minecraft = p_93768_;
    }

    public void render(PoseStack p_93781_, int p_93782_)
    {
        int i = this.getWidth();

        if (this.lastChatWidth != i)
        {
            this.lastChatWidth = i;
            this.rescaleChat();
        }

        if (!this.isChatHidden())
        {
            this.processPendingMessages();
            int j = this.getLinesPerPage();
            int k = this.trimmedMessages.size();

            if (k > 0)
            {
                boolean flag = false;

                if (this.isChatFocused())
                {
                    flag = true;
                }

                float f = (float)this.getScale();
                int l = Mth.ceil((float)this.getWidth() / f);
                p_93781_.pushPose();
                p_93781_.translate(4.0D, 8.0D, 0.0D);
                p_93781_.scale(f, f, 1.0F);
                double d0 = this.minecraft.options.chatOpacity * (double)0.9F + (double)0.1F;
                double d1 = this.minecraft.options.textBackgroundOpacity;
                double d2 = 9.0D * (this.minecraft.options.chatLineSpacing + 1.0D);
                double d3 = -8.0D * (this.minecraft.options.chatLineSpacing + 1.0D) + 4.0D * this.minecraft.options.chatLineSpacing;
                int i1 = 0;

                for (int j1 = 0; j1 + this.chatScrollbarPos < this.trimmedMessages.size() && j1 < j; ++j1)
                {
                    GuiMessage<FormattedCharSequence> guimessage = this.trimmedMessages.get(j1 + this.chatScrollbarPos);

                    if (guimessage != null)
                    {
                        int k1 = p_93782_ - guimessage.getAddedTime();

                        if (k1 < 200 || flag)
                        {
                            double d4 = flag ? 1.0D : getTimeFactor(k1);
                            int i2 = (int)(255.0D * d4 * d0);
                            int j2 = (int)(255.0D * d4 * d1);
                            ++i1;

                            if (i2 > 3)
                            {
                                int k2 = 0;
                                double d5 = (double)(-j1) * d2;
                                p_93781_.pushPose();
                                p_93781_.translate(0.0D, 0.0D, 50.0D);

                                if (this.minecraft.options.ofChatBackground == 5)
                                {
                                    l = this.minecraft.font.width(guimessage.getMessage()) - 2;
                                }

                                if (this.minecraft.options.ofChatBackground != 3)
                                {
                                    fill(p_93781_, -4, (int)(d5 - d2), 0 + l + 4, (int)d5, j2 << 24);
                                }

                                RenderSystem.enableBlend();
                                p_93781_.translate(0.0D, 0.0D, 50.0D);

                                if (!this.minecraft.options.ofChatShadow)
                                {
                                    this.minecraft.font.draw(p_93781_, guimessage.getMessage(), 0.0F, (float)((int)(d5 + d3)), 16777215 + (i2 << 24));
                                }
                                else
                                {
                                    this.minecraft.font.drawShadow(p_93781_, guimessage.getMessage(), 0.0F, (float)((int)(d5 + d3)), 16777215 + (i2 << 24));
                                }

                                RenderSystem.disableBlend();
                                p_93781_.popPose();
                            }
                        }
                    }
                }

                if (!this.chatQueue.isEmpty())
                {
                    int l2 = (int)(128.0D * d0);
                    int j3 = (int)(255.0D * d1);
                    p_93781_.pushPose();
                    p_93781_.translate(0.0D, 0.0D, 50.0D);
                    fill(p_93781_, -2, 0, l + 4, 9, j3 << 24);
                    RenderSystem.enableBlend();
                    p_93781_.translate(0.0D, 0.0D, 50.0D);
                    this.minecraft.font.drawShadow(p_93781_, new TranslatableComponent("chat.queue", this.chatQueue.size()), 0.0F, 1.0F, 16777215 + (l2 << 24));
                    p_93781_.popPose();
                    RenderSystem.disableBlend();
                }

                if (flag)
                {
                    int i3 = 9;
                    int k3 = k * i3;
                    int l3 = i1 * i3;
                    int i4 = this.chatScrollbarPos * l3 / k;
                    int l1 = l3 * l3 / k3;

                    if (k3 != l3)
                    {
                        int j4 = i4 > 0 ? 170 : 96;
                        int k4 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        p_93781_.translate(-4.0D, 0.0D, 0.0D);
                        fill(p_93781_, 0, -i4, 2, -i4 - l1, k4 + (j4 << 24));
                        fill(p_93781_, 2, -i4, 1, -i4 - l1, 13421772 + (j4 << 24));
                    }
                }

                p_93781_.popPose();
            }
        }
    }

    private boolean isChatHidden()
    {
        return this.minecraft.options.chatVisibility == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int pCounter)
    {
        double d0 = (double)pCounter / 200.0D;
        d0 = 1.0D - d0;
        d0 = d0 * 10.0D;
        d0 = Mth.clamp(d0, 0.0D, 1.0D);
        return d0 * d0;
    }

    public void clearMessages(boolean pClearSentMsgHistory)
    {
        this.chatQueue.clear();
        this.trimmedMessages.clear();
        this.allMessages.clear();

        if (pClearSentMsgHistory)
        {
            this.recentChat.clear();
        }
    }

    public void addMessage(Component pChatComponent)
    {
        this.addMessage(pChatComponent, 0);
    }

    public void addMessage(Component pChatComponent, int p_93789_)
    {
        this.addMessage(pChatComponent, p_93789_, this.minecraft.gui.getGuiTicks(), false);
        LOGGER.info("[CHAT] {}", (Object)pChatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void addMessage(Component pChatComponent, int p_93792_, int p_93793_, boolean p_93794_)
    {
        if (p_93792_ != 0)
        {
            this.removeById(p_93792_);
        }

        int i = Mth.floor((double)this.getWidth() / this.getScale());
        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(pChatComponent, i, this.minecraft.font);
        boolean flag = this.isChatFocused();

        for (FormattedCharSequence formattedcharsequence : list)
        {
            if (flag && this.chatScrollbarPos > 0)
            {
                this.newMessageSinceScroll = true;
                this.scrollChat(1.0D);
            }

            this.trimmedMessages.add(0, new GuiMessage<>(p_93793_, formattedcharsequence, p_93792_));
        }

        while (this.trimmedMessages.size() > 100)
        {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }

        if (!p_93794_)
        {
            this.allMessages.add(0, new GuiMessage<>(p_93793_, pChatComponent, p_93792_));

            while (this.allMessages.size() > 100)
            {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    public void rescaleChat()
    {
        this.trimmedMessages.clear();
        this.resetChatScroll();

        for (int i = this.allMessages.size() - 1; i >= 0; --i)
        {
            GuiMessage<Component> guimessage = this.allMessages.get(i);
            this.addMessage(guimessage.getMessage(), guimessage.getId(), guimessage.getAddedTime(), true);
        }
    }

    public List<String> getRecentChat()
    {
        return this.recentChat;
    }

    public void addRecentChat(String pMessage)
    {
        if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(pMessage))
        {
            this.recentChat.add(pMessage);
        }
    }

    public void resetChatScroll()
    {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(double pPosInc)
    {
        this.chatScrollbarPos = (int)((double)this.chatScrollbarPos + pPosInc);
        int i = this.trimmedMessages.size();

        if (this.chatScrollbarPos > i - this.getLinesPerPage())
        {
            this.chatScrollbarPos = i - this.getLinesPerPage();
        }

        if (this.chatScrollbarPos <= 0)
        {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean handleChatQueueClicked(double p_93773_, double p_93774_)
    {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden() && !this.chatQueue.isEmpty())
        {
            double d0 = p_93773_ - 2.0D;
            double d1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - p_93774_ - 40.0D;

            if (d0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d1 < 0.0D && d1 > (double)Mth.floor(-9.0D * this.getScale()))
            {
                this.addMessage(this.chatQueue.remove());
                this.lastMessage = System.currentTimeMillis();
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Nullable
    public Style getClickedComponentStyleAt(double p_93801_, double p_93802_)
    {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden())
        {
            double d0 = p_93801_ - 2.0D;
            double d1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - p_93802_ - 40.0D;
            d0 = (double)Mth.floor(d0 / this.getScale());
            d1 = (double)Mth.floor(d1 / (this.getScale() * (this.minecraft.options.chatLineSpacing + 1.0D)));

            if (!(d0 < 0.0D) && !(d1 < 0.0D))
            {
                int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());

                if (d0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d1 < (double)(9 * i + i))
                {
                    int j = (int)(d1 / 9.0D + (double)this.chatScrollbarPos);

                    if (j >= 0 && j < this.trimmedMessages.size())
                    {
                        GuiMessage<FormattedCharSequence> guimessage = this.trimmedMessages.get(j);
                        return this.minecraft.font.getSplitter().componentStyleAtWidth(guimessage.getMessage(), (int)d0);
                    }
                }

                return null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    private boolean isChatFocused()
    {
        return this.minecraft.screen instanceof ChatScreen;
    }

    public void removeById(int pId)
    {
        this.trimmedMessages.removeIf((p_93805_1_) ->
        {
            return p_93805_1_.getId() == pId;
        });
        this.allMessages.removeIf((p_93777_1_) ->
        {
            return p_93777_1_.getId() == pId;
        });
    }

    public int getWidth()
    {
        int i = getWidth(this.minecraft.options.chatWidth);
        Window window = Minecraft.getInstance().getWindow();
        int j = (int)((double)(window.getWidth() - 3) / window.getGuiScale());
        return Mth.clamp(i, 0, j);
    }

    public int getHeight()
    {
        return getHeight((this.isChatFocused() ? this.minecraft.options.chatHeightFocused : this.minecraft.options.chatHeightUnfocused) / (this.minecraft.options.chatLineSpacing + 1.0D));
    }

    public double getScale()
    {
        return this.minecraft.options.chatScale;
    }

    public static int getWidth(double p_93799_)
    {
        int i = 320;
        int j = 40;
        return Mth.floor(p_93799_ * 280.0D + 40.0D);
    }

    public static int getHeight(double p_93812_)
    {
        int i = 180;
        int j = 20;
        return Mth.floor(p_93812_ * 160.0D + 20.0D);
    }

    public int getLinesPerPage()
    {
        return this.getHeight() / 9;
    }

    private long getChatRateMillis()
    {
        return (long)(this.minecraft.options.chatDelay * 1000.0D);
    }

    private void processPendingMessages()
    {
        if (!this.chatQueue.isEmpty())
        {
            long i = System.currentTimeMillis();

            if (i - this.lastMessage >= this.getChatRateMillis())
            {
                this.addMessage(this.chatQueue.remove());
                this.lastMessage = i;
            }
        }
    }

    public void enqueueMessage(Component p_93809_)
    {
        if (this.minecraft.options.chatDelay <= 0.0D)
        {
            this.addMessage(p_93809_);
        }
        else
        {
            long i = System.currentTimeMillis();

            if (i - this.lastMessage >= this.getChatRateMillis())
            {
                this.addMessage(p_93809_);
                this.lastMessage = i;
            }
            else
            {
                this.chatQueue.add(p_93809_);
            }
        }
    }
}

package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientAdvancements
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final AdvancementList advancements = new AdvancementList();
    private final Map<Advancement, AdvancementProgress> progress = Maps.newHashMap();
    @Nullable
    private ClientAdvancements.Listener listener;
    @Nullable
    private Advancement selectedTab;

    public ClientAdvancements(Minecraft p_104395_)
    {
        this.minecraft = p_104395_;
    }

    public void update(ClientboundUpdateAdvancementsPacket pPacket)
    {
        if (pPacket.shouldReset())
        {
            this.advancements.clear();
            this.progress.clear();
        }

        this.advancements.remove(pPacket.getRemoved());
        this.advancements.add(pPacket.getAdded());

        for (Entry<ResourceLocation, AdvancementProgress> entry : pPacket.getProgress().entrySet())
        {
            Advancement advancement = this.advancements.get(entry.getKey());

            if (advancement != null)
            {
                AdvancementProgress advancementprogress = entry.getValue();
                advancementprogress.m_8198_(advancement.getCriteria(), advancement.getRequirements());
                this.progress.put(advancement, advancementprogress);

                if (this.listener != null)
                {
                    this.listener.onUpdateAdvancementProgress(advancement, advancementprogress);
                }

                if (!pPacket.shouldReset() && advancementprogress.isDone() && advancement.getDisplay() != null && advancement.getDisplay().shouldShowToast())
                {
                    this.minecraft.getToasts().addToast(new AdvancementToast(advancement));
                }
            }
            else
            {
                LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
            }
        }
    }

    public AdvancementList getAdvancements()
    {
        return this.advancements;
    }

    public void setSelectedTab(@Nullable Advancement pAdvancement, boolean pTellServer)
    {
        ClientPacketListener clientpacketlistener = this.minecraft.getConnection();

        if (clientpacketlistener != null && pAdvancement != null && pTellServer)
        {
            clientpacketlistener.send(ServerboundSeenAdvancementsPacket.openedTab(pAdvancement));
        }

        if (this.selectedTab != pAdvancement)
        {
            this.selectedTab = pAdvancement;

            if (this.listener != null)
            {
                this.listener.onSelectedTabChanged(pAdvancement);
            }
        }
    }

    public void setListener(@Nullable ClientAdvancements.Listener pListener)
    {
        this.listener = pListener;
        this.advancements.setListener(pListener);

        if (pListener != null)
        {
            for (Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet())
            {
                pListener.onUpdateAdvancementProgress(entry.getKey(), entry.getValue());
            }

            pListener.onSelectedTabChanged(this.selectedTab);
        }
    }

    public interface Listener extends AdvancementList.Listener
    {
        void onUpdateAdvancementProgress(Advancement pAdvancement, AdvancementProgress pProgress);

        void onSelectedTabChanged(@Nullable Advancement pAdvancement);
    }
}

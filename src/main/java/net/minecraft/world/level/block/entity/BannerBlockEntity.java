package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BannerBlockEntity extends BlockEntity implements Nameable
{
    public static final int MAX_PATTERNS = 6;
    public static final String TAG_PATTERNS = "Patterns";
    public static final String TAG_PATTERN = "Pattern";
    public static final String TAG_COLOR = "Color";
    @Nullable
    private Component name;
    private DyeColor baseColor;
    @Nullable
    private ListTag itemPatterns;
    private boolean receivedData;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public BannerBlockEntity(BlockPos p_155035_, BlockState p_155036_)
    {
        super(BlockEntityType.BANNER, p_155035_, p_155036_);
        this.baseColor = ((AbstractBannerBlock)p_155036_.getBlock()).getColor();
    }

    public BannerBlockEntity(BlockPos p_155038_, BlockState p_155039_, DyeColor p_155040_)
    {
        this(p_155038_, p_155039_);
        this.baseColor = p_155040_;
    }

    @Nullable
    public static ListTag getItemPatterns(ItemStack pStack)
    {
        ListTag listtag = null;
        CompoundTag compoundtag = pStack.getTagElement("BlockEntityTag");

        if (compoundtag != null && compoundtag.contains("Patterns", 9))
        {
            listtag = compoundtag.getList("Patterns", 10).copy();
        }

        return listtag;
    }

    public void fromItem(ItemStack pStack, DyeColor pColor)
    {
        this.itemPatterns = getItemPatterns(pStack);
        this.baseColor = pColor;
        this.patterns = null;
        this.receivedData = true;
        this.name = pStack.hasCustomHoverName() ? pStack.getHoverName() : null;
    }

    public Component getName()
    {
        return (Component)(this.name != null ? this.name : new TranslatableComponent("block.minecraft.banner"));
    }

    @Nullable
    public Component getCustomName()
    {
        return this.name;
    }

    public void setCustomName(Component pName)
    {
        this.name = pName;
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        super.save(pCompound);

        if (this.itemPatterns != null)
        {
            pCompound.put("Patterns", this.itemPatterns);
        }

        if (this.name != null)
        {
            pCompound.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        return pCompound;
    }

    public void load(CompoundTag p_155042_)
    {
        super.load(p_155042_);

        if (p_155042_.contains("CustomName", 8))
        {
            this.name = Component.Serializer.fromJson(p_155042_.getString("CustomName"));
        }

        this.itemPatterns = p_155042_.getList("Patterns", 10);
        this.patterns = null;
        this.receivedData = true;
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    public CompoundTag getUpdateTag()
    {
        return this.save(new CompoundTag());
    }

    public static int getPatternCount(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTagElement("BlockEntityTag");
        return compoundtag != null && compoundtag.contains("Patterns") ? compoundtag.getList("Patterns", 10).size() : 0;
    }

    public List<Pair<BannerPattern, DyeColor>> getPatterns()
    {
        if (this.patterns == null && this.receivedData)
        {
            this.patterns = createPatterns(this.baseColor, this.itemPatterns);
        }

        return this.patterns;
    }

    public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor pColor, @Nullable ListTag pNbtList)
    {
        List<Pair<BannerPattern, DyeColor>> list = Lists.newArrayList();
        list.add(Pair.of(BannerPattern.BASE, pColor));

        if (pNbtList != null)
        {
            for (int i = 0; i < pNbtList.size(); ++i)
            {
                CompoundTag compoundtag = pNbtList.getCompound(i);
                BannerPattern bannerpattern = BannerPattern.byHash(compoundtag.getString("Pattern"));

                if (bannerpattern != null)
                {
                    int j = compoundtag.getInt("Color");
                    list.add(Pair.of(bannerpattern, DyeColor.byId(j)));
                }
            }
        }

        return list;
    }

    public static void removeLastPattern(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTagElement("BlockEntityTag");

        if (compoundtag != null && compoundtag.contains("Patterns", 9))
        {
            ListTag listtag = compoundtag.getList("Patterns", 10);

            if (!listtag.isEmpty())
            {
                listtag.remove(listtag.size() - 1);

                if (listtag.isEmpty())
                {
                    pStack.removeTagKey("BlockEntityTag");
                }
            }
        }
    }

    public ItemStack getItem()
    {
        ItemStack itemstack = new ItemStack(BannerBlock.byColor(this.baseColor));

        if (this.itemPatterns != null && !this.itemPatterns.isEmpty())
        {
            itemstack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }

        if (this.name != null)
        {
            itemstack.setHoverName(this.name);
        }

        return itemstack;
    }

    public DyeColor getBaseColor()
    {
        return this.baseColor;
    }
}

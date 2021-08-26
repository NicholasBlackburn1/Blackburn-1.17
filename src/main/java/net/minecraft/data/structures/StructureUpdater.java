package net.minecraft.data.structures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureUpdater implements SnbtToNbt.Filter
{
    private static final Logger LOGGER = LogManager.getLogger();

    public CompoundTag apply(String p_126503_, CompoundTag p_126504_)
    {
        return p_126503_.startsWith("data/minecraft/structures/") ? update(p_126503_, p_126504_) : p_126504_;
    }

    public static CompoundTag update(String p_176823_, CompoundTag p_176824_)
    {
        return updateStructure(p_176823_, patchVersion(p_176824_));
    }

    private static CompoundTag patchVersion(CompoundTag pNbt)
    {
        if (!pNbt.contains("DataVersion", 99))
        {
            pNbt.putInt("DataVersion", 500);
        }

        return pNbt;
    }

    private static CompoundTag updateStructure(String pName, CompoundTag pNbt)
    {
        StructureTemplate structuretemplate = new StructureTemplate();
        int i = pNbt.getInt("DataVersion");
        int j = 2678;

        if (i < 2678)
        {
            LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", i, 2678, pName);
        }

        CompoundTag compoundtag = NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, pNbt, i);
        structuretemplate.load(compoundtag);
        return structuretemplate.save(new CompoundTag());
    }
}

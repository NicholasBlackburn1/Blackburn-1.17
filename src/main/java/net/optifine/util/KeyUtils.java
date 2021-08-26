package net.optifine.util;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.KeyMapping;

public class KeyUtils
{
    public static void fixKeyConflicts(KeyMapping[] keys, KeyMapping[] keysPrio)
    {
        Set<String> set = new HashSet<>();

        for (int i = 0; i < keysPrio.length; ++i)
        {
            KeyMapping keymapping = keysPrio[i];
            set.add(keymapping.saveString());
        }

        Set<KeyMapping> set1 = new HashSet<>(Arrays.asList(keys));
        set1.removeAll(Arrays.asList(keysPrio));

        for (KeyMapping keymapping1 : set1)
        {
            String s = keymapping1.saveString();

            if (set.contains(s))
            {
                keymapping1.setKey(InputConstants.UNKNOWN);
            }
        }
    }
}

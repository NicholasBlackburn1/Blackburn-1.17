package com.mojang.blaze3d.platform;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SnooperAccess {
   void setFixedData(String p_166445_, Object p_166446_);
}
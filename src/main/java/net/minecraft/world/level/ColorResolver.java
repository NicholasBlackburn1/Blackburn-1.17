package net.minecraft.world.level;

import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.level.biome.Biome;

public interface ColorResolver {
   @DontObfuscate
   int getColor(Biome p_130046_, double p_130047_, double p_130048_);
}
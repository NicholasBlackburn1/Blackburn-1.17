package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class RootSystemConfiguration implements FeatureConfiguration {
   public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create((p_161129_) -> {
      return p_161129_.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter((p_161153_) -> {
         return p_161153_.treeFeature;
      }), Codec.intRange(1, 64).fieldOf("required_vertical_space_for_tree").forGetter((p_161151_) -> {
         return p_161151_.requiredVerticalSpaceForTree;
      }), Codec.intRange(1, 64).fieldOf("root_radius").forGetter((p_161149_) -> {
         return p_161149_.rootRadius;
      }), ResourceLocation.CODEC.fieldOf("root_replaceable").forGetter((p_161147_) -> {
         return p_161147_.rootReplaceable;
      }), BlockStateProvider.CODEC.fieldOf("root_state_provider").forGetter((p_161145_) -> {
         return p_161145_.rootStateProvider;
      }), Codec.intRange(1, 256).fieldOf("root_placement_attempts").forGetter((p_161143_) -> {
         return p_161143_.rootPlacementAttempts;
      }), Codec.intRange(1, 4096).fieldOf("root_column_max_height").forGetter((p_161141_) -> {
         return p_161141_.rootColumnMaxHeight;
      }), Codec.intRange(1, 64).fieldOf("hanging_root_radius").forGetter((p_161139_) -> {
         return p_161139_.hangingRootRadius;
      }), Codec.intRange(0, 16).fieldOf("hanging_roots_vertical_span").forGetter((p_161137_) -> {
         return p_161137_.hangingRootsVerticalSpan;
      }), BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider").forGetter((p_161135_) -> {
         return p_161135_.hangingRootStateProvider;
      }), Codec.intRange(1, 256).fieldOf("hanging_root_placement_attempts").forGetter((p_161133_) -> {
         return p_161133_.hangingRootPlacementAttempts;
      }), Codec.intRange(1, 64).fieldOf("allowed_vertical_water_for_tree").forGetter((p_161131_) -> {
         return p_161131_.allowedVerticalWaterForTree;
      })).apply(p_161129_, RootSystemConfiguration::new);
   });
   public final Supplier<ConfiguredFeature<?, ?>> treeFeature;
   public final int requiredVerticalSpaceForTree;
   public final int rootRadius;
   public final ResourceLocation rootReplaceable;
   public final BlockStateProvider rootStateProvider;
   public final int rootPlacementAttempts;
   public final int rootColumnMaxHeight;
   public final int hangingRootRadius;
   public final int hangingRootsVerticalSpan;
   public final BlockStateProvider hangingRootStateProvider;
   public final int hangingRootPlacementAttempts;
   public final int allowedVerticalWaterForTree;

   public RootSystemConfiguration(Supplier<ConfiguredFeature<?, ?>> p_161116_, int p_161117_, int p_161118_, ResourceLocation p_161119_, BlockStateProvider p_161120_, int p_161121_, int p_161122_, int p_161123_, int p_161124_, BlockStateProvider p_161125_, int p_161126_, int p_161127_) {
      this.treeFeature = p_161116_;
      this.requiredVerticalSpaceForTree = p_161117_;
      this.rootRadius = p_161118_;
      this.rootReplaceable = p_161119_;
      this.rootStateProvider = p_161120_;
      this.rootPlacementAttempts = p_161121_;
      this.rootColumnMaxHeight = p_161122_;
      this.hangingRootRadius = p_161123_;
      this.hangingRootsVerticalSpan = p_161124_;
      this.hangingRootStateProvider = p_161125_;
      this.hangingRootPlacementAttempts = p_161126_;
      this.allowedVerticalWaterForTree = p_161127_;
   }
}
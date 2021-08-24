package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChunkToProtochunkFix extends DataFix {
   private static final int NUM_SECTIONS = 16;

   public ChunkToProtochunkFix(Schema p_15285_, boolean p_15286_) {
      super(p_15285_, p_15286_);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.CHUNK);
      Type<?> type1 = this.getOutputSchema().getType(References.CHUNK);
      Type<?> type2 = type.findFieldType("Level");
      Type<?> type3 = type1.findFieldType("Level");
      Type<?> type4 = type2.findFieldType("TileTicks");
      OpticFinder<?> opticfinder = DSL.fieldFinder("Level", type2);
      OpticFinder<?> opticfinder1 = DSL.fieldFinder("TileTicks", type4);
      return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("ChunkToProtoChunkFix", type, this.getOutputSchema().getType(References.CHUNK), (p_15298_) -> {
         return p_15298_.updateTyped(opticfinder, type3, (p_145246_) -> {
            Optional<? extends Stream<? extends Dynamic<?>>> optional = p_145246_.getOptionalTyped(opticfinder1).flatMap((p_145248_) -> {
               return p_145248_.write().result();
            }).flatMap((p_145250_) -> {
               return p_145250_.asStreamOpt().result();
            });
            Dynamic<?> dynamic = p_145246_.get(DSL.remainderFinder());
            boolean flag = dynamic.get("TerrainPopulated").asBoolean(false) && (!dynamic.get("LightPopulated").asNumber().result().isPresent() || dynamic.get("LightPopulated").asBoolean(false));
            dynamic = dynamic.set("Status", dynamic.createString(flag ? "mobs_spawned" : "empty"));
            dynamic = dynamic.set("hasLegacyStructureData", dynamic.createBoolean(true));
            Dynamic<?> dynamic1;
            if (flag) {
               Optional<ByteBuffer> optional1 = dynamic.get("Biomes").asByteBufferOpt().result();
               if (optional1.isPresent()) {
                  ByteBuffer bytebuffer = optional1.get();
                  int[] aint = new int[256];

                  for(int i = 0; i < aint.length; ++i) {
                     if (i < bytebuffer.capacity()) {
                        aint[i] = bytebuffer.get(i) & 255;
                     }
                  }

                  dynamic = dynamic.set("Biomes", dynamic.createIntList(Arrays.stream(aint)));
               }

               Dynamic<?> dynamic2 = dynamic;
               List<ShortList> list = IntStream.range(0, 16).mapToObj((p_145242_) -> {
                  return new ShortArrayList();
               }).collect(Collectors.toList());
               if (optional.isPresent()) {
                  optional.get().forEach((p_145256_) -> {
                     int j = p_145256_.get("x").asInt(0);
                     int k = p_145256_.get("y").asInt(0);
                     int l = p_145256_.get("z").asInt(0);
                     short short1 = packOffsetCoordinates(j, k, l);
                     list.get(k >> 4).add(short1);
                  });
                  dynamic = dynamic.set("ToBeTicked", dynamic.createList(list.stream().map((p_145253_) -> {
                     return dynamic2.createList(p_145253_.stream().map(dynamic2::createShort));
                  })));
               }

               dynamic1 = DataFixUtils.orElse(p_145246_.set(DSL.remainderFinder(), dynamic).write().result(), dynamic);
            } else {
               dynamic1 = dynamic;
            }

            return type3.readTyped(dynamic1).result().orElseThrow(() -> {
               return new IllegalStateException("Could not read the new chunk");
            }).getFirst();
         });
      }), this.writeAndRead("Structure biome inject", this.getInputSchema().getType(References.STRUCTURE_FEATURE), this.getOutputSchema().getType(References.STRUCTURE_FEATURE)));
   }

   private static short packOffsetCoordinates(int p_15291_, int p_15292_, int p_15293_) {
      return (short)(p_15291_ & 15 | (p_15292_ & 15) << 4 | (p_15293_ & 15) << 8);
   }
}
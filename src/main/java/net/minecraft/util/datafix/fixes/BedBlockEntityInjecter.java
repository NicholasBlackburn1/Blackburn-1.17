package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class BedBlockEntityInjecter extends DataFix
{
    public BedBlockEntityInjecter(Schema p_14701_, boolean p_14702_)
    {
        super(p_14701_, p_14702_);
    }

    public TypeRewriteRule makeRule()
    {
        Type<?> type = this.getOutputSchema().getType(References.CHUNK);
        Type<?> type1 = type.findFieldType("Level");
        Type<?> type2 = type1.findFieldType("TileEntities");

        if (!(type2 instanceof ListType))
        {
            throw new IllegalStateException("Tile entity type is not a list type.");
        }
        else
        {
            ListType<?> listtype = (ListType)type2;
            return this.cap(type1, listtype);
        }
    }

    private <TE> TypeRewriteRule cap(Type<?> pLevelType, ListType<TE> pTileType)
    {
        Type<TE> type = pTileType.getElement();
        OpticFinder<?> opticfinder = DSL.fieldFinder("Level", pLevelType);
        OpticFinder<List<TE>> opticfinder1 = DSL.fieldFinder("TileEntities", pTileType);
        int i = 416;
        return TypeRewriteRule.seq(this.fixTypeEverywhere("InjectBedBlockEntityType", this.getInputSchema().findChoiceType(References.BLOCK_ENTITY), this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY), (p_14717_) ->
        {
            return (p_145085_) -> {
                return (com.mojang.datafixers.util.Pair)p_145085_;
            };
        }), this.fixTypeEverywhereTyped("BedBlockEntityInjecter", this.getOutputSchema().getType(References.CHUNK), (p_14708_) ->
        {
            Typed<?> typed = p_14708_.getTyped(opticfinder);
            Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
            int j = dynamic.get("xPos").asInt(0);
            int k = dynamic.get("zPos").asInt(0);
            List<TE> list = Lists.newArrayList(typed.getOrCreate(opticfinder1));
            List <? extends Dynamic<? >> list1 = dynamic.get("Sections").asList(Function.identity());

            for (int l = 0; l < list1.size(); ++l)
            {
                Dynamic<?> dynamic1 = list1.get(l);
                int i1 = dynamic1.get("Y").asInt(0);
                Stream<Integer> stream = dynamic1.get("Blocks").asStream().map((p_145087_) ->
                {
                    return p_145087_.asInt(0);
                });
                int j1 = 0;

                for (int k1 : (Iterable<Integer>)() -> stream.iterator())
                {
                    if (416 == (k1 & 255) << 4)
                    {
                        int l1 = j1 & 15;
                        int i2 = j1 >> 8 & 15;
                        int j2 = j1 >> 4 & 15;
                        Map < Dynamic<?>, Dynamic<? >> map = Maps.newHashMap();
                        map.put(dynamic1.createString("id"), dynamic1.createString("minecraft:bed"));
                        map.put(dynamic1.createString("x"), dynamic1.createInt(l1 + (j << 4)));
                        map.put(dynamic1.createString("y"), dynamic1.createInt(i2 + (i1 << 4)));
                        map.put(dynamic1.createString("z"), dynamic1.createInt(j2 + (k << 4)));
                        map.put(dynamic1.createString("color"), dynamic1.createShort((short)14));
                        list.add(type.read(dynamic1.createMap(map)).result().orElseThrow(() ->
                        {
                            return new IllegalStateException("Could not parse newly created bed block entity.");
                        }).getFirst());
                    }

                    ++j1;
                }
            }

            return !list.isEmpty() ? p_14708_.set(opticfinder, typed.set(opticfinder1, list)) : p_14708_;
        }));
    }
}

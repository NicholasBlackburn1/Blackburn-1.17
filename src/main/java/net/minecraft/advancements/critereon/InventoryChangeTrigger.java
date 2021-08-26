package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("inventory_changed");

    public ResourceLocation getId()
    {
        return ID;
    }

    public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        JsonObject jsonobject = GsonHelper.getAsJsonObject(pJson, "slots", new JsonObject());
        MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(jsonobject.get("occupied"));
        MinMaxBounds.Ints minmaxbounds$ints1 = MinMaxBounds.Ints.fromJson(jsonobject.get("full"));
        MinMaxBounds.Ints minmaxbounds$ints2 = MinMaxBounds.Ints.fromJson(jsonobject.get("empty"));
        ItemPredicate[] aitempredicate = ItemPredicate.fromJsonArray(pJson.get("items"));
        return new InventoryChangeTrigger.TriggerInstance(pEntityPredicate, minmaxbounds$ints, minmaxbounds$ints1, minmaxbounds$ints2, aitempredicate);
    }

    public void trigger(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack)
    {
        int i = 0;
        int j = 0;
        int k = 0;

        for (int l = 0; l < pInventory.getContainerSize(); ++l)
        {
            ItemStack itemstack = pInventory.getItem(l);

            if (itemstack.isEmpty())
            {
                ++j;
            }
            else
            {
                ++k;

                if (itemstack.getCount() >= itemstack.getMaxStackSize())
                {
                    ++i;
                }
            }
        }

        this.trigger(pPlayer, pInventory, pStack, i, j, k);
    }

    private void trigger(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack, int p_43157_, int p_43158_, int p_43159_)
    {
        this.trigger(pPlayer, (p_43166_) ->
        {
            return p_43166_.matches(pInventory, pStack, p_43157_, p_43158_, p_43159_);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final MinMaxBounds.Ints slotsOccupied;
        private final MinMaxBounds.Ints slotsFull;
        private final MinMaxBounds.Ints slotsEmpty;
        private final ItemPredicate[] predicates;

        public TriggerInstance(EntityPredicate.Composite p_43181_, MinMaxBounds.Ints p_43182_, MinMaxBounds.Ints p_43183_, MinMaxBounds.Ints p_43184_, ItemPredicate[] p_43185_)
        {
            super(InventoryChangeTrigger.ID, p_43181_);
            this.slotsOccupied = p_43182_;
            this.slotsFull = p_43183_;
            this.slotsEmpty = p_43184_;
            this.predicates = p_43185_;
        }

        public static InventoryChangeTrigger.TriggerInstance m_43197_(ItemPredicate... p_43198_)
        {
            return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, p_43198_);
        }

        public static InventoryChangeTrigger.TriggerInstance m_43199_(ItemLike... p_43200_)
        {
            ItemPredicate[] aitempredicate = new ItemPredicate[p_43200_.length];

            for (int i = 0; i < p_43200_.length; ++i)
            {
                aitempredicate[i] = new ItemPredicate((Tag<Item>)null, ImmutableSet.of(p_43200_[i].asItem()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, (Potion)null, NbtPredicate.ANY);
            }

            return m_43197_(aitempredicate);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);

            if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny())
            {
                JsonObject jsonobject1 = new JsonObject();
                jsonobject1.add("occupied", this.slotsOccupied.serializeToJson());
                jsonobject1.add("full", this.slotsFull.serializeToJson());
                jsonobject1.add("empty", this.slotsEmpty.serializeToJson());
                jsonobject.add("slots", jsonobject1);
            }

            if (this.predicates.length > 0)
            {
                JsonArray jsonarray = new JsonArray();

                for (ItemPredicate itempredicate : this.predicates)
                {
                    jsonarray.add(itempredicate.serializeToJson());
                }

                jsonobject.add("items", jsonarray);
            }

            return jsonobject;
        }

        public boolean matches(Inventory pInventory, ItemStack pStack, int pFull, int pEmpty, int pOccupied)
        {
            if (!this.slotsFull.matches(pFull))
            {
                return false;
            }
            else if (!this.slotsEmpty.matches(pEmpty))
            {
                return false;
            }
            else if (!this.slotsOccupied.matches(pOccupied))
            {
                return false;
            }
            else
            {
                int i = this.predicates.length;

                if (i == 0)
                {
                    return true;
                }
                else if (i != 1)
                {
                    List<ItemPredicate> list = new ObjectArrayList<>(this.predicates);
                    int j = pInventory.getContainerSize();

                    for (int k = 0; k < j; ++k)
                    {
                        if (list.isEmpty())
                        {
                            return true;
                        }

                        ItemStack itemstack = pInventory.getItem(k);

                        if (!itemstack.isEmpty())
                        {
                            list.removeIf((p_43194_) ->
                            {
                                return p_43194_.matches(itemstack);
                            });
                        }
                    }

                    return list.isEmpty();
                }
                else
                {
                    return !pStack.isEmpty() && this.predicates[0].matches(pStack);
                }
            }
        }
    }
}

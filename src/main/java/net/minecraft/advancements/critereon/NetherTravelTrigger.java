package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class NetherTravelTrigger extends SimpleCriterionTrigger<NetherTravelTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("nether_travel");

   public ResourceLocation getId() {
      return ID;
   }

   public NetherTravelTrigger.TriggerInstance createInstance(JsonObject p_58447_, EntityPredicate.Composite p_58448_, DeserializationContext p_58449_) {
      LocationPredicate locationpredicate = LocationPredicate.fromJson(p_58447_.get("entered"));
      LocationPredicate locationpredicate1 = LocationPredicate.fromJson(p_58447_.get("exited"));
      DistancePredicate distancepredicate = DistancePredicate.fromJson(p_58447_.get("distance"));
      return new NetherTravelTrigger.TriggerInstance(p_58448_, locationpredicate, locationpredicate1, distancepredicate);
   }

   public void trigger(ServerPlayer p_58440_, Vec3 p_58441_) {
      this.trigger(p_58440_, (p_58445_) -> {
         return p_58445_.matches(p_58440_.getLevel(), p_58441_, p_58440_.getX(), p_58440_.getY(), p_58440_.getZ());
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final LocationPredicate entered;
      private final LocationPredicate exited;
      private final DistancePredicate distance;

      public TriggerInstance(EntityPredicate.Composite p_58459_, LocationPredicate p_58460_, LocationPredicate p_58461_, DistancePredicate p_58462_) {
         super(NetherTravelTrigger.ID, p_58459_);
         this.entered = p_58460_;
         this.exited = p_58461_;
         this.distance = p_58462_;
      }

      public static NetherTravelTrigger.TriggerInstance travelledThroughNether(DistancePredicate p_58470_) {
         return new NetherTravelTrigger.TriggerInstance(EntityPredicate.Composite.ANY, LocationPredicate.ANY, LocationPredicate.ANY, p_58470_);
      }

      public boolean matches(ServerLevel p_58464_, Vec3 p_58465_, double p_58466_, double p_58467_, double p_58468_) {
         if (!this.entered.matches(p_58464_, p_58465_.x, p_58465_.y, p_58465_.z)) {
            return false;
         } else if (!this.exited.matches(p_58464_, p_58466_, p_58467_, p_58468_)) {
            return false;
         } else {
            return this.distance.matches(p_58465_.x, p_58465_.y, p_58465_.z, p_58466_, p_58467_, p_58468_);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_58472_) {
         JsonObject jsonobject = super.serializeToJson(p_58472_);
         jsonobject.add("entered", this.entered.serializeToJson());
         jsonobject.add("exited", this.exited.serializeToJson());
         jsonobject.add("distance", this.distance.serializeToJson());
         return jsonobject;
      }
   }
}
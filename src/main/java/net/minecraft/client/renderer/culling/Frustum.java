package net.minecraft.client.renderer.culling;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Frustum {
   private final Vector4f[] frustumData = new Vector4f[6];
   private double camX;
   private double camY;
   private double camZ;

   public Frustum(Matrix4f p_113000_, Matrix4f p_113001_) {
      this.calculateFrustum(p_113000_, p_113001_);
   }

   public void prepare(double p_113003_, double p_113004_, double p_113005_) {
      this.camX = p_113003_;
      this.camY = p_113004_;
      this.camZ = p_113005_;
   }

   private void calculateFrustum(Matrix4f p_113027_, Matrix4f p_113028_) {
      Matrix4f matrix4f = p_113028_.copy();
      matrix4f.multiply(p_113027_);
      matrix4f.transpose();
      this.getPlane(matrix4f, -1, 0, 0, 0);
      this.getPlane(matrix4f, 1, 0, 0, 1);
      this.getPlane(matrix4f, 0, -1, 0, 2);
      this.getPlane(matrix4f, 0, 1, 0, 3);
      this.getPlane(matrix4f, 0, 0, -1, 4);
      this.getPlane(matrix4f, 0, 0, 1, 5);
   }

   private void getPlane(Matrix4f p_113021_, int p_113022_, int p_113023_, int p_113024_, int p_113025_) {
      Vector4f vector4f = new Vector4f((float)p_113022_, (float)p_113023_, (float)p_113024_, 1.0F);
      vector4f.transform(p_113021_);
      vector4f.normalize();
      this.frustumData[p_113025_] = vector4f;
   }

   public boolean isVisible(AABB p_113030_) {
      return this.cubeInFrustum(p_113030_.minX, p_113030_.minY, p_113030_.minZ, p_113030_.maxX, p_113030_.maxY, p_113030_.maxZ);
   }

   private boolean cubeInFrustum(double p_113007_, double p_113008_, double p_113009_, double p_113010_, double p_113011_, double p_113012_) {
      float f = (float)(p_113007_ - this.camX);
      float f1 = (float)(p_113008_ - this.camY);
      float f2 = (float)(p_113009_ - this.camZ);
      float f3 = (float)(p_113010_ - this.camX);
      float f4 = (float)(p_113011_ - this.camY);
      float f5 = (float)(p_113012_ - this.camZ);
      return this.cubeInFrustum(f, f1, f2, f3, f4, f5);
   }

   private boolean cubeInFrustum(float p_113014_, float p_113015_, float p_113016_, float p_113017_, float p_113018_, float p_113019_) {
      for(int i = 0; i < 6; ++i) {
         Vector4f vector4f = this.frustumData[i];
         if (!(vector4f.dot(new Vector4f(p_113014_, p_113015_, p_113016_, 1.0F)) > 0.0F) && !(vector4f.dot(new Vector4f(p_113017_, p_113015_, p_113016_, 1.0F)) > 0.0F) && !(vector4f.dot(new Vector4f(p_113014_, p_113018_, p_113016_, 1.0F)) > 0.0F) && !(vector4f.dot(new Vector4f(p_113017_, p_113018_, p_113016_, 1.0F)) > 0.0F) && !(vector4f.dot(new Vector4f(p_113014_, p_113015_, p_113019_, 1.0F)) > 0.0F) && !(vector4f.dot(new Vector4f(p_113017_, p_113015_, p_113019_, 1.0F)) > 0.0F) && !(vector4f.dot(new Vector4f(p_113014_, p_113018_, p_113019_, 1.0F)) > 0.0F) && !(vector4f.dot(new Vector4f(p_113017_, p_113018_, p_113019_, 1.0F)) > 0.0F)) {
            return false;
         }
      }

      return true;
   }
}
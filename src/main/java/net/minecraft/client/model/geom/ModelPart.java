package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.optifine.Config;
import net.optifine.IRandomEntity;
import net.optifine.RandomEntities;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.model.ModelSprite;
import net.optifine.render.BoxVertexPositions;
import net.optifine.render.VertexPosition;
import net.optifine.shaders.Shaders;

public final class ModelPart
{
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public boolean visible = true;
    public final List<ModelPart.Cube> cubes;
    public final Map<String, ModelPart> children;
    public List<ModelPart> childModelsList;
    public List<ModelSprite> spriteList = new ArrayList<>();
    public boolean mirrorV = false;
    public float scaleX = 1.0F;
    public float scaleY = 1.0F;
    public float scaleZ = 1.0F;
    private ResourceLocation textureLocation = null;
    private String id = null;
    private ModelUpdater modelUpdater;
    private LevelRenderer renderGlobal = Config.getRenderGlobal();
    public float textureWidth = 64.0F;
    public float textureHeight = 32.0F;
    private int textureOffsetX;
    private int textureOffsetY;
    public boolean mirror;

    public ModelPart setTextureOffset(int x, int y)
    {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public ModelPart setTextureSize(int textureWidthIn, int textureHeightIn)
    {
        this.textureWidth = (float)textureWidthIn;
        this.textureHeight = (float)textureHeightIn;
        return this;
    }

    public ModelPart(List<ModelPart.Cube> p_171306_, Map<String, ModelPart> p_171307_)
    {
        if (p_171306_ instanceof ImmutableList)
        {
            p_171306_ = new ArrayList<>(p_171306_);
        }

        this.cubes = p_171306_;
        this.children = p_171307_;
        this.childModelsList = new ArrayList<>(this.children.values());
    }

    public PartPose storePose()
    {
        return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
    }

    public void loadPose(PartPose p_171323_)
    {
        this.x = p_171323_.x;
        this.y = p_171323_.y;
        this.z = p_171323_.z;
        this.xRot = p_171323_.xRot;
        this.yRot = p_171323_.yRot;
        this.zRot = p_171323_.zRot;
    }

    public void copyFrom(ModelPart pModelRenderer)
    {
        this.xRot = pModelRenderer.xRot;
        this.yRot = pModelRenderer.yRot;
        this.zRot = pModelRenderer.zRot;
        this.x = pModelRenderer.x;
        this.y = pModelRenderer.y;
        this.z = pModelRenderer.z;
    }

    public ModelPart getChilds(String p_171325_)
    {
        ModelPart modelpart = this.children.get(p_171325_);

        if (modelpart == null)
        {
            throw new NoSuchElementException("Can't find part " + p_171325_);
        }
        else
        {
            return modelpart;
        }
    }

    public void setPos(float pRotationPointX, float pRotationPointY, float pRotationPointZ)
    {
        this.x = pRotationPointX;
        this.y = pRotationPointY;
        this.z = pRotationPointZ;
    }

    public void setRotation(float p_171328_, float p_171329_, float p_171330_)
    {
        this.xRot = p_171328_;
        this.yRot = p_171329_;
        this.zRot = p_171330_;
    }

    public void render(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay)
    {
        this.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void render(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float p_104311_, float p_104312_, float p_104313_, float p_104314_)
    {
        if (this.visible && (!this.cubes.isEmpty() || !this.children.isEmpty() || !this.spriteList.isEmpty()))
        {
            RenderType rendertype = null;
            MultiBufferSource.BufferSource multibuffersource$buffersource = null;

            if (this.textureLocation != null)
            {
                if (this.renderGlobal.renderOverlayEyes)
                {
                    return;
                }

                multibuffersource$buffersource = pBuffer.getRenderTypeBuffer();

                if (multibuffersource$buffersource != null)
                {
                    VertexConsumer vertexconsumer = pBuffer.getSecondaryBuilder();
                    rendertype = multibuffersource$buffersource.getLastRenderType();
                    pBuffer = multibuffersource$buffersource.getBuffer(this.textureLocation, pBuffer);

                    if (vertexconsumer != null)
                    {
                        pBuffer = VertexMultiConsumer.create(vertexconsumer, pBuffer);
                    }
                }
            }

            if (this.modelUpdater != null)
            {
                this.modelUpdater.update();
            }

            pMatrixStack.pushPose();
            this.translateAndRotate(pMatrixStack);
            this.compile(pMatrixStack.last(), pBuffer, pPackedLight, pPackedOverlay, p_104311_, p_104312_, p_104313_, p_104314_);
            int j = this.childModelsList.size();

            for (int i = 0; i < j; ++i)
            {
                ModelPart modelpart = this.childModelsList.get(i);
                modelpart.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, p_104311_, p_104312_, p_104313_, p_104314_);
            }

            int k = this.spriteList.size();

            for (int l = 0; l < k; ++l)
            {
                ModelSprite modelsprite = this.spriteList.get(l);
                modelsprite.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, p_104311_, p_104312_, p_104313_, p_104314_);
            }

            pMatrixStack.popPose();

            if (rendertype != null)
            {
                multibuffersource$buffersource.getBuffer(rendertype);
            }
        }
    }

    public void visit(PoseStack p_171310_, ModelPart.Visitor p_171311_)
    {
        this.visit(p_171310_, p_171311_, "");
    }

    private void visit(PoseStack p_171313_, ModelPart.Visitor p_171314_, String p_171315_)
    {
        if (!this.cubes.isEmpty() || !this.children.isEmpty())
        {
            p_171313_.pushPose();
            this.translateAndRotate(p_171313_);
            PoseStack.Pose posestack$pose = p_171313_.last();

            for (int i = 0; i < this.cubes.size(); ++i)
            {
                p_171314_.visit(posestack$pose, p_171315_, i, this.cubes.get(i));
            }

            String s = p_171315_ + "/";
            this.children.forEach((p_315395_3_, p_315395_4_) ->
            {
                p_315395_4_.visit(p_171313_, p_171314_, s + p_315395_3_);
            });
            p_171313_.popPose();
        }
    }

    public void translateAndRotate(PoseStack pMatrixStack)
    {
        pMatrixStack.translate((double)(this.x / 16.0F), (double)(this.y / 16.0F), (double)(this.z / 16.0F));

        if (this.zRot != 0.0F)
        {
            pMatrixStack.mulPose(Vector3f.ZP.rotation(this.zRot));
        }

        if (this.yRot != 0.0F)
        {
            pMatrixStack.mulPose(Vector3f.YP.rotation(this.yRot));
        }

        if (this.xRot != 0.0F)
        {
            pMatrixStack.mulPose(Vector3f.XP.rotation(this.xRot));
        }
    }

    private void compile(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        boolean flag = Config.isShaders() && Shaders.useVelocityAttrib && Config.isMinecraftThread();
        int i = this.cubes.size();

        for (int j = 0; j < i; ++j)
        {
            ModelPart.Cube modelpart$cube = this.cubes.get(j);
            VertexPosition[][] avertexposition = (VertexPosition[][])null;

            if (flag)
            {
                IRandomEntity irandomentity = RandomEntities.getRandomEntityRendered();

                if (irandomentity != null)
                {
                    avertexposition = modelpart$cube.getBoxVertexPositions(irandomentity.getId());
                }
            }

            modelpart$cube.compile(pMatrixEntry, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha, avertexposition);
        }
    }

    public ModelPart.Cube getRandomCube(Random pRandom)
    {
        return this.cubes.get(pRandom.nextInt(this.cubes.size()));
    }

    public boolean isEmpty()
    {
        return this.cubes.isEmpty();
    }

    public Stream<ModelPart> getAllParts()
    {
        return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::getAllParts));
    }

    public void addSprite(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd)
    {
        this.spriteList.add(new ModelSprite(this, this.textureOffsetX, this.textureOffsetY, posX, posY, posZ, sizeX, sizeY, sizeZ, sizeAdd));
    }

    public ResourceLocation getTextureLocation()
    {
        return this.textureLocation;
    }

    public void setTextureLocation(ResourceLocation textureLocation)
    {
        this.textureLocation = textureLocation;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void addBox(int[][] faceUvs, float x, float y, float z, float dx, float dy, float dz, float delta)
    {
        this.cubes.add(new ModelPart.Cube(faceUvs, x, y, z, dx, dy, dz, delta, delta, delta, this.mirror, this.textureWidth, this.textureHeight));
    }

    public void addBox(float x, float y, float z, float width, float height, float depth, float delta)
    {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, delta, delta, this.mirror, false);
    }

    private void addBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirror, boolean dummyIn)
    {
        this.cubes.add(new ModelPart.Cube(texOffX, texOffY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, mirror, this.textureWidth, this.textureHeight));
    }

    public ModelPart getChildModelDeep(String name)
    {
        if (name == null)
        {
            return null;
        }
        else if (this.children.containsKey(name))
        {
            return this.getChilds(name);
        }
        else
        {
            if (this.children != null)
            {
                for (String s : this.children.keySet())
                {
                    ModelPart modelpart = this.children.get(s);
                    ModelPart modelpart1 = modelpart.getChildModelDeep(name);

                    if (modelpart1 != null)
                    {
                        return modelpart1;
                    }
                }
            }

            return null;
        }
    }

    public ModelPart getChild(String id)
    {
        if (id == null)
        {
            return null;
        }
        else
        {
            if (this.children != null)
            {
                for (String s : this.children.keySet())
                {
                    ModelPart modelpart = this.children.get(s);

                    if (id.equals(modelpart.getId()))
                    {
                        return modelpart;
                    }
                }
            }

            return null;
        }
    }

    public ModelPart getChildDeep(String id)
    {
        if (id == null)
        {
            return null;
        }
        else
        {
            ModelPart modelpart = this.getChild(id);

            if (modelpart != null)
            {
                return modelpart;
            }
            else
            {
                if (this.children != null)
                {
                    for (String s : this.children.keySet())
                    {
                        ModelPart modelpart1 = this.children.get(s);
                        ModelPart modelpart2 = modelpart1.getChildDeep(id);

                        if (modelpart2 != null)
                        {
                            return modelpart2;
                        }
                    }
                }

                return null;
            }
        }
    }

    public void setModelUpdater(ModelUpdater modelUpdater)
    {
        this.modelUpdater = modelUpdater;
    }

    public void addChildModel(String name, ModelPart part)
    {
        if (part != null)
        {
            this.children.put(name, part);
            this.childModelsList = new ArrayList<>(this.children.values());
        }
    }

    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append("id: " + this.id + ", boxes: " + (this.cubes != null ? this.cubes.size() : null) + ", submodels: " + (this.children != null ? this.children.size() : null));
        return stringbuffer.toString();
    }

    public static class Cube
    {
        private final ModelPart.Polygon[] polygons;
        public final float minX;
        public final float minY;
        public final float minZ;
        public final float maxX;
        public final float maxY;
        public final float maxZ;
        private BoxVertexPositions boxVertexPositions;

        public Cube(int p_104343_, int p_104344_, float p_104345_, float p_104346_, float p_104347_, float p_104348_, float p_104349_, float p_104350_, float p_104351_, float p_104352_, float p_104353_, boolean p_104354_, float p_104355_, float p_104356_)
        {
            this.minX = p_104345_;
            this.minY = p_104346_;
            this.minZ = p_104347_;
            this.maxX = p_104345_ + p_104348_;
            this.maxY = p_104346_ + p_104349_;
            this.maxZ = p_104347_ + p_104350_;
            this.polygons = new ModelPart.Polygon[6];
            float f = p_104345_ + p_104348_;
            float f1 = p_104346_ + p_104349_;
            float f2 = p_104347_ + p_104350_;
            p_104345_ = p_104345_ - p_104351_;
            p_104346_ = p_104346_ - p_104352_;
            p_104347_ = p_104347_ - p_104353_;
            f = f + p_104351_;
            f1 = f1 + p_104352_;
            f2 = f2 + p_104353_;

            if (p_104354_)
            {
                float f3 = f;
                f = p_104345_;
                p_104345_ = f3;
            }

            ModelPart.Vertex modelpart$vertex7 = new ModelPart.Vertex(p_104345_, p_104346_, p_104347_, 0.0F, 0.0F);
            ModelPart.Vertex modelpart$vertex = new ModelPart.Vertex(f, p_104346_, p_104347_, 0.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex1 = new ModelPart.Vertex(f, f1, p_104347_, 8.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex2 = new ModelPart.Vertex(p_104345_, f1, p_104347_, 8.0F, 0.0F);
            ModelPart.Vertex modelpart$vertex3 = new ModelPart.Vertex(p_104345_, p_104346_, f2, 0.0F, 0.0F);
            ModelPart.Vertex modelpart$vertex4 = new ModelPart.Vertex(f, p_104346_, f2, 0.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex5 = new ModelPart.Vertex(f, f1, f2, 8.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex6 = new ModelPart.Vertex(p_104345_, f1, f2, 8.0F, 0.0F);
            float f4 = (float)p_104343_;
            float f5 = (float)p_104343_ + p_104350_;
            float f6 = (float)p_104343_ + p_104350_ + p_104348_;
            float f7 = (float)p_104343_ + p_104350_ + p_104348_ + p_104348_;
            float f8 = (float)p_104343_ + p_104350_ + p_104348_ + p_104350_;
            float f9 = (float)p_104343_ + p_104350_ + p_104348_ + p_104350_ + p_104348_;
            float f10 = (float)p_104344_;
            float f11 = (float)p_104344_ + p_104350_;
            float f12 = (float)p_104344_ + p_104350_ + p_104349_;
            this.polygons[2] = new ModelPart.Polygon(new ModelPart.Vertex[] {modelpart$vertex4, modelpart$vertex3, modelpart$vertex7, modelpart$vertex}, f5, f10, f6, f11, p_104355_, p_104356_, p_104354_, Direction.DOWN);
            this.polygons[3] = new ModelPart.Polygon(new ModelPart.Vertex[] {modelpart$vertex1, modelpart$vertex2, modelpart$vertex6, modelpart$vertex5}, f6, f11, f7, f10, p_104355_, p_104356_, p_104354_, Direction.UP);
            this.polygons[1] = new ModelPart.Polygon(new ModelPart.Vertex[] {modelpart$vertex7, modelpart$vertex3, modelpart$vertex6, modelpart$vertex2}, f4, f11, f5, f12, p_104355_, p_104356_, p_104354_, Direction.WEST);
            this.polygons[4] = new ModelPart.Polygon(new ModelPart.Vertex[] {modelpart$vertex, modelpart$vertex7, modelpart$vertex2, modelpart$vertex1}, f5, f11, f6, f12, p_104355_, p_104356_, p_104354_, Direction.NORTH);
            this.polygons[0] = new ModelPart.Polygon(new ModelPart.Vertex[] {modelpart$vertex4, modelpart$vertex, modelpart$vertex1, modelpart$vertex5}, f6, f11, f8, f12, p_104355_, p_104356_, p_104354_, Direction.EAST);
            this.polygons[5] = new ModelPart.Polygon(new ModelPart.Vertex[] {modelpart$vertex3, modelpart$vertex4, modelpart$vertex5, modelpart$vertex6}, f8, f11, f9, f12, p_104355_, p_104356_, p_104354_, Direction.SOUTH);
        }

        public Cube(int[][] faceUvs, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirorIn, float texWidth, float texHeight)
        {
            this.minX = x;
            this.minY = y;
            this.minZ = z;
            this.maxX = x + width;
            this.maxY = y + height;
            this.maxZ = z + depth;
            this.polygons = new ModelPart.Polygon[6];
            float f = x + width;
            float f1 = y + height;
            float f2 = z + depth;
            x = x - deltaX;
            y = y - deltaY;
            z = z - deltaZ;
            f = f + deltaX;
            f1 = f1 + deltaY;
            f2 = f2 + deltaZ;

            if (mirorIn)
            {
                float f3 = f;
                f = x;
                x = f3;
            }

            ModelPart.Vertex modelpart$vertex7 = new ModelPart.Vertex(x, y, z, 0.0F, 0.0F);
            ModelPart.Vertex modelpart$vertex = new ModelPart.Vertex(f, y, z, 0.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex1 = new ModelPart.Vertex(f, f1, z, 8.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex2 = new ModelPart.Vertex(x, f1, z, 8.0F, 0.0F);
            ModelPart.Vertex modelpart$vertex3 = new ModelPart.Vertex(x, y, f2, 0.0F, 0.0F);
            ModelPart.Vertex modelpart$vertex4 = new ModelPart.Vertex(f, y, f2, 0.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex5 = new ModelPart.Vertex(f, f1, f2, 8.0F, 8.0F);
            ModelPart.Vertex modelpart$vertex6 = new ModelPart.Vertex(x, f1, f2, 8.0F, 0.0F);
            this.polygons[2] = this.makeTexturedQuad(new ModelPart.Vertex[] {modelpart$vertex4, modelpart$vertex3, modelpart$vertex7, modelpart$vertex}, faceUvs[1], true, texWidth, texHeight, mirorIn, Direction.DOWN);
            this.polygons[3] = this.makeTexturedQuad(new ModelPart.Vertex[] {modelpart$vertex1, modelpart$vertex2, modelpart$vertex6, modelpart$vertex5}, faceUvs[0], true, texWidth, texHeight, mirorIn, Direction.UP);
            this.polygons[1] = this.makeTexturedQuad(new ModelPart.Vertex[] {modelpart$vertex7, modelpart$vertex3, modelpart$vertex6, modelpart$vertex2}, faceUvs[5], false, texWidth, texHeight, mirorIn, Direction.WEST);
            this.polygons[4] = this.makeTexturedQuad(new ModelPart.Vertex[] {modelpart$vertex, modelpart$vertex7, modelpart$vertex2, modelpart$vertex1}, faceUvs[2], false, texWidth, texHeight, mirorIn, Direction.NORTH);
            this.polygons[0] = this.makeTexturedQuad(new ModelPart.Vertex[] {modelpart$vertex4, modelpart$vertex, modelpart$vertex1, modelpart$vertex5}, faceUvs[4], false, texWidth, texHeight, mirorIn, Direction.EAST);
            this.polygons[5] = this.makeTexturedQuad(new ModelPart.Vertex[] {modelpart$vertex3, modelpart$vertex4, modelpart$vertex5, modelpart$vertex6}, faceUvs[3], false, texWidth, texHeight, mirorIn, Direction.SOUTH);
        }

        private ModelPart.Polygon makeTexturedQuad(ModelPart.Vertex[] positionTextureVertexs, int[] faceUvs, boolean reverseUV, float textureWidth, float textureHeight, boolean mirrorIn, Direction directionIn)
        {
            if (faceUvs == null)
            {
                return null;
            }
            else
            {
                return reverseUV ? new ModelPart.Polygon(positionTextureVertexs, (float)faceUvs[2], (float)faceUvs[3], (float)faceUvs[0], (float)faceUvs[1], textureWidth, textureHeight, mirrorIn, directionIn) : new ModelPart.Polygon(positionTextureVertexs, (float)faceUvs[0], (float)faceUvs[1], (float)faceUvs[2], (float)faceUvs[3], textureWidth, textureHeight, mirrorIn, directionIn);
            }
        }

        public VertexPosition[][] getBoxVertexPositions(int key)
        {
            if (this.boxVertexPositions == null)
            {
                this.boxVertexPositions = new BoxVertexPositions();
            }

            return this.boxVertexPositions.get(key);
        }

        public void compile(PoseStack.Pose p_171333_, VertexConsumer p_171334_, int p_171335_, int p_171336_, float p_171337_, float p_171338_, float p_171339_, float p_171340_)
        {
            this.compile(p_171333_, p_171334_, p_171335_, p_171336_, p_171337_, p_171338_, p_171339_, p_171340_, (VertexPosition[][])null);
        }

        public void compile(PoseStack.Pose matrixEntryIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, VertexPosition[][] boxPos)
        {
            Matrix4f matrix4f = matrixEntryIn.pose();
            Matrix3f matrix3f = matrixEntryIn.normal();
            int i = this.polygons.length;

            for (int j = 0; j < i; ++j)
            {
                ModelPart.Polygon modelpart$polygon = this.polygons[j];

                if (modelpart$polygon != null)
                {
                    if (boxPos != null)
                    {
                        bufferIn.setQuadVertexPositions(boxPos[j]);
                    }

                    Vector3f vector3f = bufferIn.getTempVec3f(modelpart$polygon.normal);
                    vector3f.transform(matrix3f);
                    float f = vector3f.x();
                    float f1 = vector3f.y();
                    float f2 = vector3f.z();

                    for (ModelPart.Vertex modelpart$vertex : modelpart$polygon.vertices)
                    {
                        float f3 = modelpart$vertex.pos.x() / 16.0F;
                        float f4 = modelpart$vertex.pos.y() / 16.0F;
                        float f5 = modelpart$vertex.pos.z() / 16.0F;
                        float f6 = matrix4f.getTransformX(f3, f4, f5, 1.0F);
                        float f7 = matrix4f.getTransformY(f3, f4, f5, 1.0F);
                        float f8 = matrix4f.getTransformZ(f3, f4, f5, 1.0F);
                        bufferIn.vertex(f6, f7, f8, red, green, blue, alpha, modelpart$vertex.u, modelpart$vertex.v, packedOverlayIn, packedLightIn, f, f1, f2);
                    }
                }
            }
        }
    }

    static class Polygon
    {
        public final ModelPart.Vertex[] vertices;
        public final Vector3f normal;

        public Polygon(ModelPart.Vertex[] p_104362_, float p_104363_, float p_104364_, float p_104365_, float p_104366_, float p_104367_, float p_104368_, boolean p_104369_, Direction p_104370_)
        {
            this.vertices = p_104362_;
            float f = 0.0F / p_104367_;
            float f1 = 0.0F / p_104368_;

            if (Config.isAntialiasing())
            {
                f = 0.05F / p_104367_;
                f1 = 0.05F / p_104368_;

                if (p_104365_ < p_104363_)
                {
                    f = -f;
                }

                if (p_104366_ < p_104364_)
                {
                    f1 = -f1;
                }
            }

            p_104362_[0] = p_104362_[0].remap(p_104365_ / p_104367_ - f, p_104364_ / p_104368_ + f1);
            p_104362_[1] = p_104362_[1].remap(p_104363_ / p_104367_ + f, p_104364_ / p_104368_ + f1);
            p_104362_[2] = p_104362_[2].remap(p_104363_ / p_104367_ + f, p_104366_ / p_104368_ - f1);
            p_104362_[3] = p_104362_[3].remap(p_104365_ / p_104367_ - f, p_104366_ / p_104368_ - f1);

            if (p_104369_)
            {
                int i = p_104362_.length;

                for (int j = 0; j < i / 2; ++j)
                {
                    ModelPart.Vertex modelpart$vertex = p_104362_[j];
                    p_104362_[j] = p_104362_[i - 1 - j];
                    p_104362_[i - 1 - j] = modelpart$vertex;
                }
            }

            this.normal = p_104370_.step();

            if (p_104369_)
            {
                this.normal.mul(-1.0F, 1.0F, 1.0F);
            }
        }
    }

    static class Vertex
    {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public Vertex(float p_104375_, float p_104376_, float p_104377_, float p_104378_, float p_104379_)
        {
            this(new Vector3f(p_104375_, p_104376_, p_104377_), p_104378_, p_104379_);
        }

        public ModelPart.Vertex remap(float pTexU, float pTexV)
        {
            return new ModelPart.Vertex(this.pos, pTexU, pTexV);
        }

        public Vertex(Vector3f p_104381_, float p_104382_, float p_104383_)
        {
            this.pos = p_104381_;
            this.u = p_104382_;
            this.v = p_104383_;
        }
    }

    @FunctionalInterface
    public interface Visitor
    {
        void visit(PoseStack.Pose p_171342_, String p_171343_, int p_171344_, ModelPart.Cube p_171345_);
    }
}

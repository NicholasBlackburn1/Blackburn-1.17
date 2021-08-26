package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

public class PostChain implements AutoCloseable
{
    private static final String MAIN_RENDER_TARGET = "minecraft:main";
    private final RenderTarget screenTarget;
    private final ResourceManager resourceManager;
    private final String name;
    private final List<PostPass> passes = Lists.newArrayList();
    private final Map<String, RenderTarget> customRenderTargets = Maps.newHashMap();
    private final List<RenderTarget> fullSizedTargets = Lists.newArrayList();
    private Matrix4f shaderOrthoMatrix;
    private int screenWidth;
    private int screenHeight;
    private float time;
    private float lastStamp;

    public PostChain(TextureManager p_110018_, ResourceManager p_110019_, RenderTarget p_110020_, ResourceLocation p_110021_) throws IOException, JsonSyntaxException
    {
        this.resourceManager = p_110019_;
        this.screenTarget = p_110020_;
        this.time = 0.0F;
        this.lastStamp = 0.0F;
        this.screenWidth = p_110020_.viewWidth;
        this.screenHeight = p_110020_.viewHeight;
        this.name = p_110021_.toString();
        this.updateOrthoMatrix();
        this.load(p_110018_, p_110021_);
    }

    private void load(TextureManager p_110034_, ResourceLocation p_110035_) throws IOException, JsonSyntaxException
    {
        Resource resource = null;

        try
        {
            resource = this.resourceManager.getResource(p_110035_);
            JsonObject jsonobject = GsonHelper.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            if (GsonHelper.isArrayNode(jsonobject, "targets"))
            {
                JsonArray jsonarray = jsonobject.getAsJsonArray("targets");
                int i = 0;

                for (JsonElement jsonelement : jsonarray)
                {
                    try
                    {
                        this.parseTargetNode(jsonelement);
                    }
                    catch (Exception exception1)
                    {
                        ChainedJsonException chainedjsonexception1 = ChainedJsonException.forException(exception1);
                        chainedjsonexception1.prependJsonKey("targets[" + i + "]");
                        throw chainedjsonexception1;
                    }

                    ++i;
                }
            }

            if (GsonHelper.isArrayNode(jsonobject, "passes"))
            {
                JsonArray jsonarray1 = jsonobject.getAsJsonArray("passes");
                int j = 0;

                for (JsonElement jsonelement1 : jsonarray1)
                {
                    try
                    {
                        this.parsePassNode(p_110034_, jsonelement1);
                    }
                    catch (Exception exception)
                    {
                        ChainedJsonException chainedjsonexception2 = ChainedJsonException.forException(exception);
                        chainedjsonexception2.prependJsonKey("passes[" + j + "]");
                        throw chainedjsonexception2;
                    }

                    ++j;
                }
            }
        }
        catch (Exception exception2)
        {
            String s;

            if (resource != null)
            {
                s = " (" + resource.getSourceName() + ")";
            }
            else
            {
                s = "";
            }

            ChainedJsonException chainedjsonexception = ChainedJsonException.forException(exception2);
            chainedjsonexception.setFilenameAndFlush(p_110035_.getPath() + s);
            throw chainedjsonexception;
        }
        finally
        {
            IOUtils.closeQuietly((Closeable)resource);
        }
    }

    private void parseTargetNode(JsonElement p_110029_) throws ChainedJsonException
    {
        if (GsonHelper.isStringValue(p_110029_))
        {
            this.addTempTarget(p_110029_.getAsString(), this.screenWidth, this.screenHeight);
        }
        else
        {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(p_110029_, "target");
            String s = GsonHelper.getAsString(jsonobject, "name");
            int i = GsonHelper.getAsInt(jsonobject, "width", this.screenWidth);
            int j = GsonHelper.getAsInt(jsonobject, "height", this.screenHeight);

            if (this.customRenderTargets.containsKey(s))
            {
                throw new ChainedJsonException(s + " is already defined");
            }

            this.addTempTarget(s, i, j);
        }
    }

    private void parsePassNode(TextureManager p_110031_, JsonElement pJson) throws IOException
    {
        JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "pass");
        String s = GsonHelper.getAsString(jsonobject, "name");
        String s1 = GsonHelper.getAsString(jsonobject, "intarget");
        String s2 = GsonHelper.getAsString(jsonobject, "outtarget");
        RenderTarget rendertarget = this.getRenderTarget(s1);
        RenderTarget rendertarget1 = this.getRenderTarget(s2);

        if (rendertarget == null)
        {
            throw new ChainedJsonException("Input target '" + s1 + "' does not exist");
        }
        else if (rendertarget1 == null)
        {
            throw new ChainedJsonException("Output target '" + s2 + "' does not exist");
        }
        else
        {
            PostPass postpass = this.addPass(s, rendertarget, rendertarget1);
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "auxtargets", (JsonArray)null);

            if (jsonarray != null)
            {
                int i = 0;

                for (JsonElement jsonelement : jsonarray)
                {
                    try
                    {
                        JsonObject jsonobject1 = GsonHelper.convertToJsonObject(jsonelement, "auxtarget");
                        String s5 = GsonHelper.getAsString(jsonobject1, "name");
                        String s3 = GsonHelper.getAsString(jsonobject1, "id");
                        boolean flag;
                        String s4;

                        if (s3.endsWith(":depth"))
                        {
                            flag = true;
                            s4 = s3.substring(0, s3.lastIndexOf(58));
                        }
                        else
                        {
                            flag = false;
                            s4 = s3;
                        }

                        RenderTarget rendertarget2 = this.getRenderTarget(s4);

                        if (rendertarget2 == null)
                        {
                            if (flag)
                            {
                                throw new ChainedJsonException("Render target '" + s4 + "' can't be used as depth buffer");
                            }

                            ResourceLocation resourcelocation = new ResourceLocation("textures/effect/" + s4 + ".png");
                            Resource resource = null;

                            try
                            {
                                resource = this.resourceManager.getResource(resourcelocation);
                            }
                            catch (FileNotFoundException filenotfoundexception)
                            {
                                throw new ChainedJsonException("Render target or texture '" + s4 + "' does not exist");
                            }
                            finally
                            {
                                IOUtils.closeQuietly((Closeable)resource);
                            }

                            RenderSystem.setShaderTexture(0, resourcelocation);
                            p_110031_.bindForSetup(resourcelocation);
                            AbstractTexture abstracttexture = p_110031_.getTexture(resourcelocation);
                            int j = GsonHelper.getAsInt(jsonobject1, "width");
                            int k = GsonHelper.getAsInt(jsonobject1, "height");
                            boolean flag1 = GsonHelper.getAsBoolean(jsonobject1, "bilinear");

                            if (flag1)
                            {
                                RenderSystem.texParameter(3553, 10241, 9729);
                                RenderSystem.texParameter(3553, 10240, 9729);
                            }
                            else
                            {
                                RenderSystem.texParameter(3553, 10241, 9728);
                                RenderSystem.texParameter(3553, 10240, 9728);
                            }

                            postpass.addAuxAsset(s5, abstracttexture::getId, j, k);
                        }
                        else if (flag)
                        {
                            postpass.addAuxAsset(s5, rendertarget2::getDepthTextureId, rendertarget2.width, rendertarget2.height);
                        }
                        else
                        {
                            postpass.addAuxAsset(s5, rendertarget2::getColorTextureId, rendertarget2.width, rendertarget2.height);
                        }
                    }
                    catch (Exception exception1)
                    {
                        ChainedJsonException chainedjsonexception = ChainedJsonException.forException(exception1);
                        chainedjsonexception.prependJsonKey("auxtargets[" + i + "]");
                        throw chainedjsonexception;
                    }

                    ++i;
                }
            }

            JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "uniforms", (JsonArray)null);

            if (jsonarray1 != null)
            {
                int l = 0;

                for (JsonElement jsonelement1 : jsonarray1)
                {
                    try
                    {
                        this.parseUniformNode(jsonelement1);
                    }
                    catch (Exception exception)
                    {
                        ChainedJsonException chainedjsonexception1 = ChainedJsonException.forException(exception);
                        chainedjsonexception1.prependJsonKey("uniforms[" + l + "]");
                        throw chainedjsonexception1;
                    }

                    ++l;
                }
            }
        }
    }

    private void parseUniformNode(JsonElement pJson) throws ChainedJsonException
    {
        JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "uniform");
        String s = GsonHelper.getAsString(jsonobject, "name");
        Uniform uniform = this.passes.get(this.passes.size() - 1).getEffect().getUniform(s);

        if (uniform == null)
        {
            throw new ChainedJsonException("Uniform '" + s + "' does not exist");
        }
        else
        {
            float[] afloat = new float[4];
            int i = 0;

            for (JsonElement jsonelement : GsonHelper.getAsJsonArray(jsonobject, "values"))
            {
                try
                {
                    afloat[i] = GsonHelper.convertToFloat(jsonelement, "value");
                }
                catch (Exception exception)
                {
                    ChainedJsonException chainedjsonexception = ChainedJsonException.forException(exception);
                    chainedjsonexception.prependJsonKey("values[" + i + "]");
                    throw chainedjsonexception;
                }

                ++i;
            }

            switch (i)
            {
                case 0:
                default:
                    break;

                case 1:
                    uniform.set(afloat[0]);
                    break;

                case 2:
                    uniform.set(afloat[0], afloat[1]);
                    break;

                case 3:
                    uniform.set(afloat[0], afloat[1], afloat[2]);
                    break;

                case 4:
                    uniform.set(afloat[0], afloat[1], afloat[2], afloat[3]);
            }
        }
    }

    public RenderTarget getTempTarget(String pAttributeName)
    {
        return this.customRenderTargets.get(pAttributeName);
    }

    public void addTempTarget(String pName, int pWidth, int pHeight)
    {
        RenderTarget rendertarget = new TextureTarget(pWidth, pHeight, true, Minecraft.ON_OSX);
        rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.customRenderTargets.put(pName, rendertarget);

        if (pWidth == this.screenWidth && pHeight == this.screenHeight)
        {
            this.fullSizedTargets.add(rendertarget);
        }
    }

    public void close()
    {
        for (RenderTarget rendertarget : this.customRenderTargets.values())
        {
            rendertarget.destroyBuffers();
        }

        for (PostPass postpass : this.passes)
        {
            postpass.close();
        }

        this.passes.clear();
    }

    public PostPass addPass(String pProgramName, RenderTarget pFramebuffer, RenderTarget pFramebufferOut) throws IOException
    {
        PostPass postpass = new PostPass(this.resourceManager, pProgramName, pFramebuffer, pFramebufferOut);
        this.passes.add(this.passes.size(), postpass);
        return postpass;
    }

    private void updateOrthoMatrix()
    {
        this.shaderOrthoMatrix = Matrix4f.orthographic(0.0F, (float)this.screenTarget.width, (float)this.screenTarget.height, 0.0F, 0.1F, 1000.0F);
    }

    public void resize(int pWidth, int pHeight)
    {
        this.screenWidth = this.screenTarget.width;
        this.screenHeight = this.screenTarget.height;
        this.updateOrthoMatrix();

        for (PostPass postpass : this.passes)
        {
            postpass.setOrthoMatrix(this.shaderOrthoMatrix);
        }

        for (RenderTarget rendertarget : this.fullSizedTargets)
        {
            rendertarget.resize(pWidth, pHeight, Minecraft.ON_OSX);
        }
    }

    public void process(float pPartialTicks)
    {
        if (pPartialTicks < this.lastStamp)
        {
            this.time += 1.0F - this.lastStamp;
            this.time += pPartialTicks;
        }
        else
        {
            this.time += pPartialTicks - this.lastStamp;
        }

        for (this.lastStamp = pPartialTicks; this.time > 20.0F; this.time -= 20.0F)
        {
        }

        for (PostPass postpass : this.passes)
        {
            postpass.process(this.time / 20.0F);
        }
    }

    public final String getName()
    {
        return this.name;
    }

    private RenderTarget getRenderTarget(String p_110050_)
    {
        if (p_110050_ == null)
        {
            return null;
        }
        else
        {
            return p_110050_.equals("minecraft:main") ? this.screenTarget : this.customRenderTargets.get(p_110050_);
        }
    }
}

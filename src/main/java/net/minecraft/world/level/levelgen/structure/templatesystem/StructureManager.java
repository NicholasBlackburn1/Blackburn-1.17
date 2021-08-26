package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String STRUCTURE_DIRECTORY_NAME = "structures";
    private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
    private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
    private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.newConcurrentMap();
    private final DataFixer fixerUpper;
    private ResourceManager resourceManager;
    private final Path generatedDir;

    public StructureManager(ResourceManager p_74332_, LevelStorageSource.LevelStorageAccess p_74333_, DataFixer p_74334_)
    {
        this.resourceManager = p_74332_;
        this.fixerUpper = p_74334_;
        this.generatedDir = p_74333_.getLevelPath(LevelResource.GENERATED_DIR).normalize();
    }

    public StructureTemplate getOrCreate(ResourceLocation p_74342_)
    {
        Optional<StructureTemplate> optional = this.get(p_74342_);

        if (optional.isPresent())
        {
            return optional.get();
        }
        else
        {
            StructureTemplate structuretemplate = new StructureTemplate();
            this.structureRepository.put(p_74342_, Optional.of(structuretemplate));
            return structuretemplate;
        }
    }

    public Optional<StructureTemplate> get(ResourceLocation p_163775_)
    {
        return this.structureRepository.computeIfAbsent(p_163775_, (p_163781_) ->
        {
            Optional<StructureTemplate> optional = this.loadFromGenerated(p_163781_);
            return optional.isPresent() ? optional : this.loadFromResource(p_163781_);
        });
    }

    public void onResourceManagerReload(ResourceManager pResourceManager)
    {
        this.resourceManager = pResourceManager;
        this.structureRepository.clear();
    }

    private Optional<StructureTemplate> loadFromResource(ResourceLocation p_163777_)
    {
        ResourceLocation resourcelocation = new ResourceLocation(p_163777_.getNamespace(), "structures/" + p_163777_.getPath() + ".nbt");

        try
        {
            Resource resource = this.resourceManager.getResource(resourcelocation);
            Optional optional;

            try
            {
                optional = Optional.of(this.readStructure(resource.getInputStream()));
            }
            catch (Throwable throwable1)
            {
                if (resource != null)
                {
                    try
                    {
                        resource.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (resource != null)
            {
                resource.close();
            }

            return optional;
        }
        catch (FileNotFoundException filenotfoundexception)
        {
            return Optional.empty();
        }
        catch (Throwable throwable2)
        {
            LOGGER.error("Couldn't load structure {}: {}", p_163777_, throwable2.toString());
            return Optional.empty();
        }
    }

    private Optional<StructureTemplate> loadFromGenerated(ResourceLocation p_163779_)
    {
        if (!this.generatedDir.toFile().isDirectory())
        {
            return Optional.empty();
        }
        else
        {
            Path path = this.createAndValidatePathToStructure(p_163779_, ".nbt");

            try
            {
                InputStream inputstream = new FileInputStream(path.toFile());
                Optional optional;

                try
                {
                    optional = Optional.of(this.readStructure(inputstream));
                }
                catch (Throwable throwable1)
                {
                    try
                    {
                        inputstream.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }

                    throw throwable1;
                }

                inputstream.close();
                return optional;
            }
            catch (FileNotFoundException filenotfoundexception)
            {
                return Optional.empty();
            }
            catch (IOException ioexception)
            {
                LOGGER.error("Couldn't load structure from {}", path, ioexception);
                return Optional.empty();
            }
        }
    }

    private StructureTemplate readStructure(InputStream pInputStream) throws IOException
    {
        CompoundTag compoundtag = NbtIo.readCompressed(pInputStream);
        return this.readStructure(compoundtag);
    }

    public StructureTemplate readStructure(CompoundTag pInputStream)
    {
        if (!pInputStream.contains("DataVersion", 99))
        {
            pInputStream.putInt("DataVersion", 500);
        }

        StructureTemplate structuretemplate = new StructureTemplate();
        structuretemplate.load(NbtUtils.update(this.fixerUpper, DataFixTypes.STRUCTURE, pInputStream, pInputStream.getInt("DataVersion")));
        return structuretemplate;
    }

    public boolean save(ResourceLocation pTemplateName)
    {
        Optional<StructureTemplate> optional = this.structureRepository.get(pTemplateName);

        if (!optional.isPresent())
        {
            return false;
        }
        else
        {
            StructureTemplate structuretemplate = optional.get();
            Path path = this.createAndValidatePathToStructure(pTemplateName, ".nbt");
            Path path1 = path.getParent();

            if (path1 == null)
            {
                return false;
            }
            else
            {
                try
                {
                    Files.createDirectories(Files.exists(path1) ? path1.toRealPath() : path1);
                }
                catch (IOException ioexception)
                {
                    LOGGER.error("Failed to create parent directory: {}", (Object)path1);
                    return false;
                }

                CompoundTag compoundtag = structuretemplate.save(new CompoundTag());

                try
                {
                    OutputStream outputstream = new FileOutputStream(path.toFile());

                    try
                    {
                        NbtIo.writeCompressed(compoundtag, outputstream);
                    }
                    catch (Throwable throwable1)
                    {
                        try
                        {
                            outputstream.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable1.addSuppressed(throwable);
                        }

                        throw throwable1;
                    }

                    outputstream.close();
                    return true;
                }
                catch (Throwable throwable2)
                {
                    return false;
                }
            }
        }
    }

    public Path createPathToStructure(ResourceLocation pLocation, String pExt)
    {
        try
        {
            Path path = this.generatedDir.resolve(pLocation.getNamespace());
            Path path1 = path.resolve("structures");
            return FileUtil.createPathToResource(path1, pLocation.getPath(), pExt);
        }
        catch (InvalidPathException invalidpathexception)
        {
            throw new ResourceLocationException("Invalid resource path: " + pLocation, invalidpathexception);
        }
    }

    private Path createAndValidatePathToStructure(ResourceLocation pLocation, String pExt)
    {
        if (pLocation.getPath().contains("//"))
        {
            throw new ResourceLocationException("Invalid resource path: " + pLocation);
        }
        else
        {
            Path path = this.createPathToStructure(pLocation, pExt);

            if (path.startsWith(this.generatedDir) && FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path))
            {
                return path;
            }
            else
            {
                throw new ResourceLocationException("Invalid resource path: " + path);
            }
        }
    }

    public void remove(ResourceLocation pTemplatePath)
    {
        this.structureRepository.remove(pTemplatePath);
    }
}

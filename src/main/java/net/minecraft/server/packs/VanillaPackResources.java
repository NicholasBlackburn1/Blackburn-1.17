package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPackResources implements PackResources, ResourceProvider
{
    public static Path generatedDir;
    private static final Logger LOGGER = LogManager.getLogger();
    public static Class<?> clientObject;
    private static final Map<PackType, Path> f_182296_ = Util.make(() ->
    {
        synchronized (VanillaPackResources.class)
        {
            Builder<PackType, Path> builder = ImmutableMap.builder();

            for (PackType packtype : PackType.values())
            {
                String s = "/" + packtype.getDirectory() + "/.mcassetsroot";
                URL url = VanillaPackResources.class.getResource(s);

                if (url == null)
                {
                    LOGGER.error("File {} does not exist in classpath", (Object)s);
                }
                else
                {
                    try
                    {
                        URI uri = url.toURI();
                        String s1 = uri.getScheme();

                        if (!"jar".equals(s1) && !"file".equals(s1))
                        {
                            LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)uri);
                        }

                        Path path = m_182297_(uri);
                        builder.put(packtype, path.getParent());
                    }
                    catch (Exception exception1)
                    {
                        LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)exception1);
                    }
                }
            }

            return builder.build();
        }
    });
    public final PackMetadataSection packMetadata;
    public final Set<String> namespaces;
    private static final boolean ON_WINDOWS = Util.getPlatform() == Util.OS.WINDOWS;
    private static final boolean FORGE = Reflector.ForgeHooksClient.exists();

    private static Path m_182297_(URI p_182298_) throws IOException
    {
        try
        {
            return Paths.get(p_182298_);
        }
        catch (FileSystemNotFoundException filesystemnotfoundexception)
        {
        }
        catch (Throwable throwable)
        {
            LOGGER.warn("Unable to get path for: {}", p_182298_, throwable);
        }

        try
        {
            FileSystems.newFileSystem(p_182298_, Collections.emptyMap());
        }
        catch (FileSystemAlreadyExistsException filesystemalreadyexistsexception)
        {
        }

        return Paths.get(p_182298_);
    }

    public VanillaPackResources(PackMetadataSection p_143761_, String... p_143762_)
    {
        this.packMetadata = p_143761_;
        this.namespaces = ImmutableSet.copyOf(p_143762_);
    }

    public InputStream getRootResource(String pFileName) throws IOException
    {
        if (!pFileName.contains("/") && !pFileName.contains("\\"))
        {
            if (generatedDir != null)
            {
                Path path = generatedDir.resolve(pFileName);

                if (Files.exists(path))
                {
                    return Files.newInputStream(path);
                }
            }

            return this.getResourceAsStream(pFileName);
        }
        else
        {
            throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
        }
    }

    public InputStream getResource(PackType pType, ResourceLocation pLocation) throws IOException
    {
        InputStream inputstream = this.getResourceAsStream(pType, pLocation);

        if (inputstream != null)
        {
            return inputstream;
        }
        else
        {
            throw new FileNotFoundException(pLocation.getPath());
        }
    }

    public Collection<ResourceLocation> getResources(PackType pMaxDepth, String pNamespace, String pPath, int pPathName, Predicate<String> pFilter)
    {
        Set<ResourceLocation> set = Sets.newHashSet();

        if (generatedDir != null)
        {
            try
            {
                getResources(set, pPathName, pNamespace, generatedDir.resolve(pMaxDepth.getDirectory()), pPath, pFilter);
            }
            catch (IOException ioexception3)
            {
            }

            if (pMaxDepth == PackType.CLIENT_RESOURCES)
            {
                Enumeration<URL> enumeration = null;

                try
                {
                    enumeration = clientObject.getClassLoader().getResources(pMaxDepth.getDirectory() + "/");
                }
                catch (IOException ioexception2)
                {
                }

                while (enumeration != null && enumeration.hasMoreElements())
                {
                    try
                    {
                        URI uri = enumeration.nextElement().toURI();

                        if ("file".equals(uri.getScheme()))
                        {
                            getResources(set, pPathName, pNamespace, Paths.get(uri), pPath, pFilter);
                        }
                    }
                    catch (URISyntaxException | IOException ioexception1)
                    {
                    }
                }
            }
        }

        try
        {
            Path path = f_182296_.get(pMaxDepth);

            if (path != null)
            {
                getResources(set, pPathName, pNamespace, path, pPath, pFilter);
            }
            else
            {
                LOGGER.error("Can't access assets root for type: {}", (Object)pMaxDepth);
            }
        }
        catch (FileNotFoundException | NoSuchFileException nosuchfileexception)
        {
        }
        catch (IOException ioexception1)
        {
            LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)ioexception1);
        }

        return set;
    }

    private static void getResources(Collection<ResourceLocation> pResourceLocations, int pMaxDepth, String pNamespace, Path pPath, String pPathName, Predicate<String> pFilter) throws IOException
    {
        Path path = pPath.resolve(pNamespace);
        Stream<Path> stream = Files.walk(path.resolve(pPathName), pMaxDepth);

        try
        {
            stream.filter((p_10351_1_) ->
            {
                return !p_10351_1_.endsWith(".mcmeta") && Files.isRegularFile(p_10351_1_) && pFilter.test(p_10351_1_.getFileName().toString());
            }).map((p_10338_2_) ->
            {
                return new ResourceLocation(pNamespace, path.relativize(p_10338_2_).toString().replaceAll("\\\\", "/"));
            }).forEach(pResourceLocations::add);
        }
        catch (Throwable throwable1)
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (stream != null)
        {
            stream.close();
        }
    }

    @Nullable
    protected InputStream getResourceAsStream(PackType pPath, ResourceLocation p_10360_)
    {
        String s = createPath(pPath, p_10360_);
        InputStream inputstream = ReflectorForge.getOptiFineResourceStream(s);

        if (inputstream != null)
        {
            return inputstream;
        }
        else
        {
            if (generatedDir != null)
            {
                Path path = generatedDir.resolve(pPath.getDirectory() + "/" + p_10360_.getNamespace() + "/" + p_10360_.getPath());

                if (Files.exists(path))
                {
                    try
                    {
                        return Files.newInputStream(path);
                    }
                    catch (IOException ioexception1)
                    {
                    }
                }
            }

            try
            {
                URL url = VanillaPackResources.class.getResource(s);
                return isResourceUrlValid(s, url) ? (FORGE ? this.getExtraInputStream(pPath, s) : url.openStream()) : null;
            }
            catch (IOException ioexception1)
            {
                return VanillaPackResources.class.getResourceAsStream(s);
            }
        }
    }

    private static String createPath(PackType pPackType, ResourceLocation pLocation)
    {
        return "/" + pPackType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath();
    }

    private static boolean isResourceUrlValid(String pPath, @Nullable URL pUrl) throws IOException
    {
        return pUrl != null && (pUrl.getProtocol().equals("jar") || validatePath(new File(pUrl.getFile()), pPath));
    }

    @Nullable
    protected InputStream getResourceAsStream(String pPath)
    {
        return FORGE ? this.getExtraInputStream(PackType.SERVER_DATA, "/" + pPath) : VanillaPackResources.class.getResourceAsStream("/" + pPath);
    }

    public boolean hasResource(PackType pType, ResourceLocation pLocation)
    {
        String s = createPath(pType, pLocation);
        InputStream inputstream = ReflectorForge.getOptiFineResourceStream(s);

        if (inputstream != null)
        {
            return true;
        }
        else
        {
            if (generatedDir != null)
            {
                Path path = generatedDir.resolve(pType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath());

                if (Files.exists(path))
                {
                    return true;
                }
            }

            try
            {
                URL url = VanillaPackResources.class.getResource(s);
                return isResourceUrlValid(s, url);
            }
            catch (IOException ioexception1)
            {
                return false;
            }
        }
    }

    public Set<String> getNamespaces(PackType pType)
    {
        return this.namespaces;
    }

    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> pDeserializer) throws IOException
    {
        try
        {
            InputStream inputstream = this.getRootResource("pack.mcmeta");
            Object object;
            label61:
            {
                try
                {
                    if (inputstream != null)
                    {
                        T t = AbstractPackResources.getMetadataFromStream(pDeserializer, inputstream);

                        if (t != null)
                        {
                            object = t;
                            break label61;
                        }
                    }
                }
                catch (Throwable throwable11)
                {
                    if (inputstream != null)
                    {
                        try
                        {
                            inputstream.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable11.addSuppressed(throwable);
                        }
                    }

                    throw throwable11;
                }

                if (inputstream != null)
                {
                    inputstream.close();
                }

                return (T)(pDeserializer == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
            }

            if (inputstream != null)
            {
                inputstream.close();
            }

            return (T)object;
        }
        catch (RuntimeException | FileNotFoundException filenotfoundexception)
        {
            return (T)(pDeserializer == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
        }
    }

    public String getName()
    {
        return "Default";
    }

    public void close()
    {
    }

    public Resource getResource(final ResourceLocation pType) throws IOException
    {
        return new Resource()
        {
            @Nullable
            InputStream inputStream;
            public void close() throws IOException
            {
                if (this.inputStream != null)
                {
                    this.inputStream.close();
                }
            }
            public ResourceLocation getLocation()
            {
                return pType;
            }
            public InputStream getInputStream()
            {
                try
                {
                    this.inputStream = VanillaPackResources.this.getResource(PackType.CLIENT_RESOURCES, pType);
                }
                catch (IOException ioexception)
                {
                    throw new UncheckedIOException("Could not get client resource from vanilla pack", ioexception);
                }

                return this.inputStream;
            }
            public boolean hasMetadata()
            {
                return false;
            }
            @Nullable
            public <T> T getMetadata(MetadataSectionSerializer<T> p_143773_)
            {
                return (T)null;
            }
            public String getSourceName()
            {
                return pType.toString();
            }
        };
    }

    private static boolean validatePath(File file, String path) throws IOException
    {
        String s = file.getPath();

        if (s.startsWith("file:"))
        {
            if (ON_WINDOWS)
            {
                s = s.replace("\\", "/");
            }

            return s.endsWith(path);
        }
        else
        {
            return FolderPackResources.validatePath(file, path);
        }
    }

    private InputStream getExtraInputStream(PackType type, String resource)
    {
        try
        {
            Path path = f_182296_.get(type);
            return path != null ? Files.newInputStream(path.resolve(resource)) : VanillaPackResources.class.getResourceAsStream(resource);
        }
        catch (IOException ioexception)
        {
            return VanillaPackResources.class.getResourceAsStream(resource);
        }
    }
}

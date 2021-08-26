package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public interface PackResources extends AutoCloseable
{
    String METADATA_EXTENSION = ".mcmeta";
    String PACK_META = "pack.mcmeta";

    @Nullable
    InputStream getRootResource(String pFileName) throws IOException;

    InputStream getResource(PackType pType, ResourceLocation pLocation) throws IOException;

    Collection<ResourceLocation> getResources(PackType pType, String pNamespace, String pPath, int pMaxDepth, Predicate<String> pFilter);

    boolean hasResource(PackType pType, ResourceLocation pLocation);

    Set<String> getNamespaces(PackType pType);

    @Nullable
    <T> T getMetadataSection(MetadataSectionSerializer<T> pDeserializer) throws IOException;

    String getName();

    void close();
}

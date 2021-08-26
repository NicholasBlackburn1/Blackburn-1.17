package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class FilePackResources extends AbstractPackResources
{
    public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
    private ZipFile zipFile;

    public FilePackResources(File p_10236_)
    {
        super(p_10236_);
    }

    private ZipFile getOrCreateZipFile() throws IOException
    {
        if (this.zipFile == null)
        {
            this.zipFile = new ZipFile(this.file);
        }

        return this.zipFile;
    }

    protected InputStream getResource(String pResourcePath) throws IOException
    {
        ZipFile zipfile = this.getOrCreateZipFile();
        ZipEntry zipentry = zipfile.getEntry(pResourcePath);

        if (zipentry == null)
        {
            throw new ResourcePackFileNotFoundException(this.file, pResourcePath);
        }
        else
        {
            return zipfile.getInputStream(zipentry);
        }
    }

    public boolean hasResource(String pResourcePath)
    {
        try
        {
            return this.getOrCreateZipFile().getEntry(pResourcePath) != null;
        }
        catch (IOException ioexception)
        {
            return false;
        }
    }

    public Set<String> getNamespaces(PackType pType)
    {
        ZipFile zipfile;

        try
        {
            zipfile = this.getOrCreateZipFile();
        }
        catch (IOException ioexception)
        {
            return Collections.emptySet();
        }

        Enumeration <? extends ZipEntry > enumeration = zipfile.entries();
        Set<String> set = Sets.newHashSet();

        while (enumeration.hasMoreElements())
        {
            ZipEntry zipentry = enumeration.nextElement();
            String s = zipentry.getName();

            if (s.startsWith(pType.getDirectory() + "/"))
            {
                List<String> list = Lists.newArrayList(SPLITTER.split(s));

                if (list.size() > 1)
                {
                    String s1 = list.get(1);

                    if (s1.equals(s1.toLowerCase(Locale.ROOT)))
                    {
                        set.add(s1);
                    }
                    else
                    {
                        this.logWarning(s1);
                    }
                }
            }
        }

        return set;
    }

    protected void finalize() throws Throwable
    {
        this.close();
        super.finalize();
    }

    public void close()
    {
        if (this.zipFile != null)
        {
            IOUtils.closeQuietly((Closeable)this.zipFile);
            this.zipFile = null;
        }
    }

    public Collection<ResourceLocation> getResources(PackType pType, String pNamespace, String pPath, int pMaxDepth, Predicate<String> pFilter)
    {
        ZipFile zipfile;

        try
        {
            zipfile = this.getOrCreateZipFile();
        }
        catch (IOException ioexception)
        {
            return Collections.emptySet();
        }

        Enumeration <? extends ZipEntry > enumeration = zipfile.entries();
        List<ResourceLocation> list = Lists.newArrayList();
        String s = pType.getDirectory() + "/" + pNamespace + "/";
        String s1 = s + pPath + "/";

        while (enumeration.hasMoreElements())
        {
            ZipEntry zipentry = enumeration.nextElement();

            if (!zipentry.isDirectory())
            {
                String s2 = zipentry.getName();

                if (!s2.endsWith(".mcmeta") && s2.startsWith(s1))
                {
                    String s3 = s2.substring(s.length());
                    String[] astring = s3.split("/");

                    if (astring.length >= pMaxDepth + 1 && pFilter.test(astring[astring.length - 1]))
                    {
                        list.add(new ResourceLocation(pNamespace, s3));
                    }
                }
            }
        }

        return list;
    }
}

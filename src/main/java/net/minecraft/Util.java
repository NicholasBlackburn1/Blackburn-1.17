package net.minecraft;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import net.optifine.SmartExecutorService;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util
{
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ExecutorService BOOTSTRAP_EXECUTOR = makeExecutor("Bootstrap");
    private static final ExecutorService BACKGROUND_EXECUTOR = makeExecutor("Main");
    private static final ExecutorService IO_POOL = makeIoExecutor();
    public static LongSupplier timeSource = System::nanoTime;
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter((p_300575_0_) ->
    {
        return p_300575_0_.getScheme().equalsIgnoreCase("jar");
    }).findFirst().orElseThrow(() ->
    {
        return new IllegalStateException("No jar file system provider found");
    });
    static final Logger LOGGER = LogManager.getLogger();
    private static Exception exceptionOpenUrl;
    private static final ExecutorService CAPE_EXECUTOR = makeExecutor("Cape");
    private static LongSupplier INNER_CLASS_SHIFT = () ->
    {
        return 0L;
    };

    public static <K, V> Collector < Entry <? extends K, ? extends V > , ? , Map<K, V >> toMap()
    {
        return Collectors.toMap(Entry::getKey, Entry::getValue);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> pProperty, Object pValue)
    {
        return pProperty.getName((T)(pValue));
    }

    public static String makeDescriptionId(String pType, @Nullable ResourceLocation pId)
    {
        return pId == null ? pType + ".unregistered_sadface" : pType + "." + pId.getNamespace() + "." + pId.getPath().replace('/', '.');
    }

    public static long getMillis()
    {
        return getNanos() / 1000000L;
    }

    public static long getNanos()
    {
        return timeSource.getAsLong();
    }

    public static long getEpochMillis()
    {
        return Instant.now().toEpochMilli();
    }

    private static ExecutorService makeExecutor(String pServiceName)
    {
        int i = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, 7);
        ExecutorService executorservice;

        if (i <= 0)
        {
            executorservice = MoreExecutors.newDirectExecutorService();
        }
        else
        {
            executorservice = new ForkJoinPool(i, (p_300572_1_) ->
            {
                ForkJoinWorkerThread forkjoinworkerthread = new ForkJoinWorkerThread(p_300572_1_)
                {
                    protected void onTermination(Throwable p_137590_)
                    {
                        if (p_137590_ != null)
                        {
                            Util.LOGGER.warn("{} died", this.getName(), p_137590_);
                        }
                        else
                        {
                            Util.LOGGER.debug("{} shutdown", (Object)this.getName());
                        }

                        super.onTermination(p_137590_);
                    }
                };
                forkjoinworkerthread.setName("Worker-" + pServiceName + "-" + WORKER_COUNT.getAndIncrement());

                if (pServiceName.equals("Bootstrap"))
                {
                    forkjoinworkerthread.setPriority(1);
                }

                return forkjoinworkerthread;
            }, Util::onThreadException, true);
        }

        if (pServiceName.equals("Bootstrap"))
        {
            executorservice = createSmartExecutor(executorservice);
        }

        return executorservice;
    }

    private static ExecutorService createSmartExecutor(ExecutorService executor)
    {
        int i = Runtime.getRuntime().availableProcessors();

        if (i <= 1)
        {
            return executor;
        }
        else
        {
            ExecutorService executorservice = new SmartExecutorService(executor);
            return executorservice;
        }
    }

    public static Executor bootstrapExecutor()
    {
        return BOOTSTRAP_EXECUTOR;
    }

    public static Executor backgroundExecutor()
    {
        return BACKGROUND_EXECUTOR;
    }

    public static Executor ioPool()
    {
        return IO_POOL;
    }

    public static void shutdownExecutors()
    {
        shutdownExecutor(BACKGROUND_EXECUTOR);
        shutdownExecutor(IO_POOL);
        shutdownExecutor(CAPE_EXECUTOR);
    }

    private static void shutdownExecutor(ExecutorService pService)
    {
        pService.shutdown();
        boolean flag;

        try
        {
            flag = pService.awaitTermination(3L, TimeUnit.SECONDS);
        }
        catch (InterruptedException interruptedexception)
        {
            flag = false;
        }

        if (!flag)
        {
            pService.shutdownNow();
        }
    }

    private static ExecutorService makeIoExecutor()
    {
        return Executors.newCachedThreadPool((p_300570_0_) ->
        {
            Thread thread = new Thread(p_300570_0_);
            thread.setName("IO-Worker-" + WORKER_COUNT.getAndIncrement());
            thread.setUncaughtExceptionHandler(Util::onThreadException);
            return thread;
        });
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable pThrowable)
    {
        CompletableFuture<T> completablefuture = new CompletableFuture<>();
        completablefuture.completeExceptionally(pThrowable);
        return completablefuture;
    }

    public static void throwAsRuntime(Throwable pThrowable)
    {
        throw pThrowable instanceof RuntimeException ? (RuntimeException)pThrowable : new RuntimeException(pThrowable);
    }

    private static void onThreadException(Thread pThread, Throwable pThrowable)
    {
        pauseInIde(pThrowable);

        if (pThrowable instanceof CompletionException)
        {
            pThrowable = pThrowable.getCause();
        }

        if (pThrowable instanceof ReportedException)
        {
            Bootstrap.realStdoutPrintln(((ReportedException)pThrowable).getReport().getFriendlyReport());
            System.exit(-1);
        }

        LOGGER.error(String.format("Caught exception in thread %s", pThread), pThrowable);
    }

    @Nullable
    public static Type<?> fetchChoiceType(TypeReference pType, String pChoiceName)
    {
        return !SharedConstants.CHECK_DATA_FIXER_SCHEMA ? null : doFetchChoiceType(pType, pChoiceName);
    }

    @Nullable
    private static Type<?> doFetchChoiceType(TypeReference pType, String pChoiceName)
    {
        Type<?> type = null;

        try
        {
            type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion())).getChoiceType(pType, pChoiceName);
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            LOGGER.debug("No data fixer registered for {}", (Object)pChoiceName);

            if (SharedConstants.IS_RUNNING_IN_IDE)
            {
                throw illegalargumentexception;
            }
        }

        return type;
    }

    public static Runnable wrapThreadWithTaskName(String p_143788_, Runnable p_143789_)
    {
        return SharedConstants.IS_RUNNING_IN_IDE ? () ->
        {
            Thread thread = Thread.currentThread();
            String s = thread.getName();
            thread.setName(p_143788_);

            try {
                p_143789_.run();
            }
            finally {
                thread.setName(s);
            }
        } : p_143789_;
    }

    public static Util.OS getPlatform()
    {
        String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (s.contains("win"))
        {
            return Util.OS.WINDOWS;
        }
        else if (s.contains("mac"))
        {
            return Util.OS.OSX;
        }
        else if (s.contains("solaris"))
        {
            return Util.OS.SOLARIS;
        }
        else if (s.contains("sunos"))
        {
            return Util.OS.SOLARIS;
        }
        else if (s.contains("linux"))
        {
            return Util.OS.LINUX;
        }
        else
        {
            return s.contains("unix") ? Util.OS.LINUX : Util.OS.UNKNOWN;
        }
    }

    public static Stream<String> getVmArguments()
    {
        RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
        return runtimemxbean.getInputArguments().stream().filter((p_300614_0_) ->
        {
            return p_300614_0_.startsWith("-X");
        });
    }

    public static <T> T lastOf(List<T> pList)
    {
        return pList.get(pList.size() - 1);
    }

    public static <T> T findNextInIterable(Iterable<T> pIterable, @Nullable T pElement)
    {
        Iterator<T> iterator = pIterable.iterator();
        T t = iterator.next();

        if (pElement != null)
        {
            T t1 = t;

            while (t1 != pElement)
            {
                if (iterator.hasNext())
                {
                    t1 = iterator.next();
                }
            }

            if (iterator.hasNext())
            {
                return iterator.next();
            }
        }

        return t;
    }

    public static <T> T findPreviousInIterable(Iterable<T> pIterable, @Nullable T pCurrent)
    {
        Iterator<T> iterator = pIterable.iterator();
        T t;
        T t1;

        for (t = null; iterator.hasNext(); t = t1)
        {
            t1 = iterator.next();

            if (t1 == pCurrent)
            {
                if (t == null)
                {
                    t = (T)(iterator.hasNext() ? Iterators.getLast(iterator) : pCurrent);
                }

                break;
            }
        }

        return t;
    }

    public static <T> T make(Supplier<T> pObject)
    {
        return pObject.get();
    }

    public static <T> T make(T pObject, Consumer<T> pConsumer)
    {
        pConsumer.accept(pObject);
        return pObject;
    }

    public static <K> Strategy<K> identityStrategy()
    {
        return (Strategy<K>) Util.IdentityStrategy.INSTANCE;
    }

    public static <V> CompletableFuture<List<V>> sequence(List <? extends CompletableFuture <? extends V >> pFutures)
    {
        return pFutures.stream().reduce(CompletableFuture.completedFuture(Lists.newArrayList()), (p_221769_0_, p_221769_1_) ->
        {
            return p_221769_1_.thenCombine(p_221769_0_, (p_300567_0_, p_300567_1_) -> {
                List<V> list = Lists.newArrayListWithCapacity(p_300567_1_.size() + 1);
                list.addAll(p_300567_1_);
                list.add(p_300567_0_);
                return list;
            });
        }, (p_300597_0_, p_300597_1_) ->
        {
            return p_300597_0_.thenCombine(p_300597_1_, (p_300583_0_, p_300583_1_) -> {
                List<V> list = Lists.newArrayListWithCapacity(p_300583_0_.size() + p_300583_1_.size());
                list.addAll(p_300583_0_);
                list.addAll(p_300583_1_);
                return list;
            });
        });
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List <? extends CompletableFuture <? extends V >> p_143841_)
    {
        List<V> list = Lists.newArrayListWithCapacity(p_143841_.size());
        CompletableFuture<?>[] completablefuture = new CompletableFuture[p_143841_.size()];
        CompletableFuture<Void> completablefuture1 = new CompletableFuture<>();
        p_143841_.forEach((p_300586_3_) ->
        {
            int i = list.size();
            list.add((V)null);
            completablefuture[i] = p_300586_3_.whenComplete((p_300591_3_, p_300591_4_) -> {
                if (p_300591_4_ != null)
                {
                    completablefuture1.completeExceptionally(p_300591_4_);
                }
                else {
                    list.set(i, p_300591_3_);
                }
            });
        });
        return CompletableFuture.allOf(completablefuture).applyToEither(completablefuture1, (p_300580_1_) ->
        {
            return list;
        });
    }

    public static <T> Stream<T> toStream(Optional <? extends T > pOptional)
    {
        return pOptional.isPresent() ? Stream.of(pOptional.get()) : Stream.empty();
    }

    public static Exception getExceptionOpenUrl()
    {
        return exceptionOpenUrl;
    }

    public static void setExceptionOpenUrl(Exception exceptionOpenUrl)
    {
        Util.exceptionOpenUrl = exceptionOpenUrl;
    }

    public static ExecutorService getCapeExecutor()
    {
        return CAPE_EXECUTOR;
    }

    public static <T> Optional<T> ifElse(Optional<T> pOpt, Consumer<T> pConsumer, Runnable pOrElse)
    {
        if (pOpt.isPresent())
        {
            pConsumer.accept(pOpt.get());
        }
        else
        {
            pOrElse.run();
        }

        return pOpt;
    }

    public static Runnable name(Runnable pRunnable, Supplier<String> pSupplier)
    {
        return pRunnable;
    }

    public static void logAndPauseIfInIde(String p_143786_)
    {
        LOGGER.error(p_143786_);

        if (SharedConstants.IS_RUNNING_IN_IDE)
        {
            doPause();
        }
    }

    public static <T extends Throwable> T pauseInIde(T pThrowable)
    {
        if (SharedConstants.IS_RUNNING_IN_IDE)
        {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", pThrowable);
            doPause();
        }

        return pThrowable;
    }

    private static void doPause()
    {
        while (true)
        {
            try
            {
                Thread.sleep(1000L);
                LOGGER.error("paused");
            }
            catch (InterruptedException interruptedexception)
            {
                return;
            }
        }
    }

    public static String describeError(Throwable pThrowable)
    {
        if (pThrowable.getCause() != null)
        {
            return describeError(pThrowable.getCause());
        }
        else
        {
            return pThrowable.getMessage() != null ? pThrowable.getMessage() : pThrowable.toString();
        }
    }

    public static <T> T m_137545_(T[] p_137546_, Random p_137547_)
    {
        return p_137546_[p_137547_.nextInt(p_137546_.length)];
    }

    public static int m_137542_(int[] p_137543_, Random p_137544_)
    {
        return p_137543_[p_137544_.nextInt(p_137543_.length)];
    }

    public static <T> T getRandom(List<T> pSelections, Random pRand)
    {
        return pSelections.get(pRand.nextInt(pSelections.size()));
    }

    private static BooleanSupplier createRenamer(final Path pFilePath, final Path pNewName)
    {
        return new BooleanSupplier()
        {
            public boolean getAsBoolean()
            {
                try
                {
                    Files.move(pFilePath, pNewName);
                    return true;
                }
                catch (IOException ioexception)
                {
                    Util.LOGGER.error("Failed to rename", (Throwable)ioexception);
                    return false;
                }
            }
            public String toString()
            {
                return "rename " + pFilePath + " to " + pNewName;
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path pFilePath)
    {
        return new BooleanSupplier()
        {
            public boolean getAsBoolean()
            {
                try
                {
                    Files.deleteIfExists(pFilePath);
                    return true;
                }
                catch (IOException ioexception)
                {
                    Util.LOGGER.warn("Failed to delete", (Throwable)ioexception);
                    return false;
                }
            }
            public String toString()
            {
                return "delete old " + pFilePath;
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path pFilePath)
    {
        return new BooleanSupplier()
        {
            public boolean getAsBoolean()
            {
                return !Files.exists(pFilePath);
            }
            public String toString()
            {
                return "verify that " + pFilePath + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path pFilePath)
    {
        return new BooleanSupplier()
        {
            public boolean getAsBoolean()
            {
                return Files.isRegularFile(pFilePath);
            }
            public String toString()
            {
                return "verify that " + pFilePath + " is present";
            }
        };
    }

    private static boolean m_137548_(BooleanSupplier... p_137549_)
    {
        for (BooleanSupplier booleansupplier : p_137549_)
        {
            if (!booleansupplier.getAsBoolean())
            {
                LOGGER.warn("Failed to execute {}", (Object)booleansupplier);
                return false;
            }
        }

        return true;
    }

    private static boolean m_137449_(int p_137450_, String p_137451_, BooleanSupplier... p_137452_)
    {
        for (int i = 0; i < p_137450_; ++i)
        {
            if (m_137548_(p_137452_))
            {
                return true;
            }

            LOGGER.error("Failed to {}, retrying {}/{}", p_137451_, i, p_137450_);
        }

        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)p_137451_);
        return false;
    }

    public static void safeReplaceFile(File pCurrent, File pLatest, File pOldBackup)
    {
        safeReplaceFile(pCurrent.toPath(), pLatest.toPath(), pOldBackup.toPath());
    }

    public static void safeReplaceFile(Path pCurrent, Path pLatest, Path pOldBackup)
    {
        int i = 10;

        if ((!Files.exists(pCurrent) || m_137449_(10, "create backup " + pOldBackup, createDeleter(pOldBackup), createRenamer(pCurrent, pOldBackup), createFileCreatedCheck(pOldBackup))) && m_137449_(10, "remove old " + pCurrent, createDeleter(pCurrent), createFileDeletedCheck(pCurrent)) && !m_137449_(10, "replace " + pCurrent + " with " + pLatest, createRenamer(pLatest, pCurrent), createFileCreatedCheck(pCurrent)))
        {
            m_137449_(10, "restore " + pCurrent + " from " + pOldBackup, createRenamer(pOldBackup, pCurrent), createFileCreatedCheck(pCurrent));
        }
    }

    public static int offsetByCodepoints(String p_137480_, int p_137481_, int p_137482_)
    {
        int i = p_137480_.length();

        if (p_137482_ >= 0)
        {
            for (int j = 0; p_137481_ < i && j < p_137482_; ++j)
            {
                if (Character.isHighSurrogate(p_137480_.charAt(p_137481_++)) && p_137481_ < i && Character.isLowSurrogate(p_137480_.charAt(p_137481_)))
                {
                    ++p_137481_;
                }
            }
        }
        else
        {
            for (int k = p_137482_; p_137481_ > 0 && k < 0; ++k)
            {
                --p_137481_;

                if (Character.isLowSurrogate(p_137480_.charAt(p_137481_)) && p_137481_ > 0 && Character.isHighSurrogate(p_137480_.charAt(p_137481_ - 1)))
                {
                    --p_137481_;
                }
            }
        }

        return p_137481_;
    }

    public static Consumer<String> prefix(String pPrefix, Consumer<String> pConsumer)
    {
        return (p_300602_2_) ->
        {
            pConsumer.accept(pPrefix + p_300602_2_);
        };
    }

    public static DataResult<int[]> fixedSize(IntStream pStream, int pSize)
    {
        int[] aint = pStream.limit((long)(pSize + 1)).toArray();

        if (aint.length != pSize)
        {
            String s = "Input is not a list of " + pSize + " ints";
            return aint.length >= pSize ? DataResult.error(s, Arrays.copyOf(aint, pSize)) : DataResult.error(s);
        }
        else
        {
            return DataResult.success(aint);
        }
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> pStream, int pSize)
    {
        if (pStream.size() != pSize)
        {
            String s = "Input is not a list of " + pSize + " elements";
            return pStream.size() >= pSize ? DataResult.error(s, pStream.subList(0, pSize)) : DataResult.error(s);
        }
        else
        {
            return DataResult.success(pStream);
        }
    }

    public static void startTimerHackThread()
    {
        Thread thread = new Thread("Timer hack thread")
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(2147483647L);
                    }
                    catch (InterruptedException interruptedexception)
                    {
                        Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                        return;
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    public static void copyBetweenDirs(Path pFromDirectory, Path pToDirectory, Path pFilePath) throws IOException
    {
        Path path = pFromDirectory.relativize(pFilePath);
        Path path1 = pToDirectory.resolve(path);
        Files.copy(pFilePath, path1);
    }

    public static String sanitizeName(String pFileName, CharPredicate pCharacterValidator)
    {
        return pFileName.toLowerCase(Locale.ROOT).chars().mapToObj((p_300608_1_) ->
        {
            return pCharacterValidator.test((char)p_300608_1_) ? Character.toString((char)p_300608_1_) : "_";
        }).collect(Collectors.joining());
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> p_143828_)
    {
        return new Function<T, R>()
        {
            private final Map<T, R> cache = Maps.newHashMap();
            public R apply(T p_143849_)
            {
                return this.cache.computeIfAbsent(p_143849_, p_143828_);
            }
            public String toString()
            {
                return "memoize/1[function=" + p_143828_ + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> p_143822_)
    {
        return new BiFunction<T, U, R>()
        {
            private final Map<Pair<T, U>, R> cache = Maps.newHashMap();
            public R apply(T p_143859_, U p_143860_)
            {
                return this.cache.computeIfAbsent(Pair.of(p_143859_, p_143860_), (p_300628_1_) ->
                {
                    return p_143822_.apply(p_300628_1_.getFirst(), p_300628_1_.getSecond());
                });
            }
            public String toString()
            {
                return "memoize/2[function=" + p_143822_ + ", size=" + this.cache.size() + "]";
            }
        };
    }

    static enum IdentityStrategy implements Strategy<Object>
    {
        INSTANCE;

        public int hashCode(Object p_137626_)
        {
            return System.identityHashCode(p_137626_);
        }

        public boolean equals(Object p_137623_, Object p_137624_)
        {
            return p_137623_ == p_137624_;
        }
    }

    public static enum OS
    {
        LINUX,
        SOLARIS,
        WINDOWS {
            protected String[] getOpenUrlArguments(URL pUrl)
            {
                return new String[] {"rundll32", "url.dll,FileProtocolHandler", pUrl.toString()};
            }
        },
        OSX {
            protected String[] getOpenUrlArguments(URL pUrl)
            {
                return new String[] {"open", pUrl.toString()};
            }
        },
        UNKNOWN;

        private OS()
        {
        }

        public void openUrl(URL pUrl)
        {
            try
            {
                Process process = AccessController.doPrivileged((PrivilegedExceptionAction<Process>)(() ->
                {
                    return Runtime.getRuntime().exec(this.getOpenUrlArguments(pUrl));
                }));

                for (String s : IOUtils.readLines(process.getErrorStream()))
                {
                    Util.LOGGER.error(s);
                }

                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            }
            catch (PrivilegedActionException | IOException ioexception)
            {
                Util.LOGGER.error("Couldn't open url '{}'", pUrl, ioexception);
                Util.exceptionOpenUrl = ioexception;
            }
        }

        public void openUri(URI pUri)
        {
            try
            {
                this.openUrl(pUri.toURL());
            }
            catch (MalformedURLException malformedurlexception)
            {
                Util.LOGGER.error("Couldn't open uri '{}'", pUri, malformedurlexception);
            }
        }

        public void openFile(File pFile)
        {
            try
            {
                this.openUrl(pFile.toURI().toURL());
            }
            catch (MalformedURLException malformedurlexception)
            {
                Util.LOGGER.error("Couldn't open file '{}'", pFile, malformedurlexception);
            }
        }

        protected String[] getOpenUrlArguments(URL pUrl)
        {
            String s = pUrl.toString();

            if ("file".equals(pUrl.getProtocol()))
            {
                s = s.replace("file:", "file://");
            }

            return new String[] {"xdg-open", s};
        }

        public void openUri(String pUri)
        {
            try
            {
                this.openUrl((new URI(pUri)).toURL());
            }
            catch (IllegalArgumentException | URISyntaxException | MalformedURLException malformedurlexception)
            {
                Util.LOGGER.error("Couldn't open uri '{}'", pUri, malformedurlexception);
            }
        }
    }
}

package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.InactiveProfiler;

public class SimpleReloadInstance<S> implements ReloadInstance
{
    private static final int PREPARATION_PROGRESS_WEIGHT = 2;
    private static final int EXTRA_RELOAD_PROGRESS_WEIGHT = 2;
    private static final int LISTENER_PROGRESS_WEIGHT = 1;
    protected final ResourceManager resourceManager;
    protected final CompletableFuture<Unit> allPreparations = new CompletableFuture<>();
    protected final CompletableFuture<List<S>> allDone;
    final Set<PreparableReloadListener> preparingListeners;
    private final int listenerCount;
    private int startedReloads;
    private int finishedReloads;
    private final AtomicInteger startedTaskCounter = new AtomicInteger();
    private final AtomicInteger doneTaskCounter = new AtomicInteger();

    public static SimpleReloadInstance<Void> of(ResourceManager pResourceManager, List<PreparableReloadListener> pListeners, Executor pBackgroundExecutor, Executor pGameExecutor, CompletableFuture<Unit> pAlsoWaitedFor)
    {
        return new SimpleReloadInstance<>(pBackgroundExecutor, pGameExecutor, pResourceManager, pListeners, (p_10829_, p_10830_, p_10831_, p_10832_, p_10833_) ->
        {
            return p_10831_.reload(p_10829_, p_10830_, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, pBackgroundExecutor, p_10833_);
        }, pAlsoWaitedFor);
    }

    protected SimpleReloadInstance(Executor p_10808_, final Executor p_10809_, ResourceManager p_10810_, List<PreparableReloadListener> p_10811_, SimpleReloadInstance.StateFactory<S> p_10812_, CompletableFuture<Unit> p_10813_)
    {
        this.resourceManager = p_10810_;
        this.listenerCount = p_10811_.size();
        this.startedTaskCounter.incrementAndGet();
        p_10813_.thenRun(this.doneTaskCounter::incrementAndGet);
        List<CompletableFuture<S>> list = Lists.newArrayList();
        CompletableFuture<?> completablefuture = p_10813_;
        this.preparingListeners = Sets.newHashSet(p_10811_);

        for (final PreparableReloadListener preparablereloadlistener : p_10811_)
        {
            final CompletableFuture<?> completablefuture1 = completablefuture;
            CompletableFuture<S> completablefuture2 = p_10812_.create(new PreparableReloadListener.PreparationBarrier()
            {
                public <T> CompletableFuture<T> wait(T pBackgroundResult)
                {
                    p_10809_.execute(() ->
                    {
                        SimpleReloadInstance.this.preparingListeners.remove(preparablereloadlistener);

                        if (SimpleReloadInstance.this.preparingListeners.isEmpty())
                        {
                            SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
                        }
                    });
                    return SimpleReloadInstance.this.allPreparations.thenCombine(completablefuture1, (p_10861_, p_10862_) ->
                    {
                        return pBackgroundResult;
                    });
                }
            }, p_10810_, preparablereloadlistener, (p_10842_) ->
            {
                this.startedTaskCounter.incrementAndGet();
                p_10808_.execute(() -> {
                    p_10842_.run();
                    this.doneTaskCounter.incrementAndGet();
                });
            }, (p_10836_) ->
            {
                ++this.startedReloads;
                p_10809_.execute(() -> {
                    p_10836_.run();
                    ++this.finishedReloads;
                });
            });
            list.add(completablefuture2);
            completablefuture = completablefuture2;
        }

        this.allDone = Util.sequenceFailFast(list);
    }

    public CompletableFuture<Unit> done()
    {
        return this.allDone.thenApply((p_10826_) ->
        {
            return Unit.INSTANCE;
        });
    }

    public float getActualProgress()
    {
        int i = this.listenerCount - this.preparingListeners.size();
        float f = (float)(this.doneTaskCounter.get() * 2 + this.finishedReloads * 2 + i * 1);
        float f1 = (float)(this.startedTaskCounter.get() * 2 + this.startedReloads * 2 + this.listenerCount * 1);
        return f / f1;
    }

    public boolean isDone()
    {
        return this.allDone.isDone();
    }

    public void checkExceptions()
    {
        if (this.allDone.isCompletedExceptionally())
        {
            this.allDone.join();
        }
    }

    protected interface StateFactory<S>
    {
        CompletableFuture<S> create(PreparableReloadListener.PreparationBarrier p_10864_, ResourceManager p_10865_, PreparableReloadListener p_10866_, Executor p_10867_, Executor p_10868_);
    }
}

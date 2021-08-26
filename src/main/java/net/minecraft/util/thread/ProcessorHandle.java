package net.minecraft.util.thread;

import com.mojang.datafixers.util.Either;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ProcessorHandle<Msg> extends AutoCloseable
{
    String name();

    void tell(Msg pTask);

default void close()
    {
    }

default <Source> CompletableFuture<Source> ask(Function<? super ProcessorHandle<Source>, ? extends Msg> p_18721_)
    {
        CompletableFuture<Source> completablefuture = new CompletableFuture<>();
        Msg msg = p_18721_.apply(of("ask future procesor handle", completablefuture::complete));
        this.tell(msg);
        return completablefuture;
    }

default <Source> CompletableFuture<Source> askEither(Function<? super ProcessorHandle<Either<Source, Exception>>, ? extends Msg> p_18723_)
    {
        CompletableFuture<Source> completablefuture = new CompletableFuture<>();
        Msg msg = p_18723_.apply(of("ask future procesor handle", (p_18719_) ->
        {
            p_18719_.ifLeft(completablefuture::complete);
            p_18719_.ifRight(completablefuture::completeExceptionally);
        }));
        this.tell(msg);
        return completablefuture;
    }

    static <Msg> ProcessorHandle<Msg> of(final String pName, final Consumer<Msg> p_18716_)
    {
        return new ProcessorHandle<Msg>()
        {
            public String name()
            {
                return pName;
            }
            public void tell(Msg pTask)
            {
                p_18716_.accept(pTask);
            }
            public String toString()
            {
                return pName;
            }
        };
    }
}

package net.minecraft.world;

public class InteractionResultHolder<T>
{
    private final InteractionResult result;
    private final T object;

    public InteractionResultHolder(InteractionResult p_19087_, T p_19088_)
    {
        this.result = p_19087_;
        this.object = p_19088_;
    }

    public InteractionResult getResult()
    {
        return this.result;
    }

    public T getObject()
    {
        return this.object;
    }

    public static <T> InteractionResultHolder<T> success(T pType)
    {
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, pType);
    }

    public static <T> InteractionResultHolder<T> consume(T pType)
    {
        return new InteractionResultHolder<>(InteractionResult.CONSUME, pType);
    }

    public static <T> InteractionResultHolder<T> pass(T pType)
    {
        return new InteractionResultHolder<>(InteractionResult.PASS, pType);
    }

    public static <T> InteractionResultHolder<T> fail(T pType)
    {
        return new InteractionResultHolder<>(InteractionResult.FAIL, pType);
    }

    public static <T> InteractionResultHolder<T> sidedSuccess(T p_19093_, boolean p_19094_)
    {
        return p_19094_ ? success(p_19093_) : consume(p_19093_);
    }
}

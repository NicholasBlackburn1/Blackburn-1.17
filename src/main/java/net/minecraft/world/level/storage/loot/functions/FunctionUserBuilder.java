package net.minecraft.world.level.storage.loot.functions;

public interface FunctionUserBuilder<T>
{
    T apply(LootItemFunction.Builder pFunctionBuilder);

    T unwrap();
}

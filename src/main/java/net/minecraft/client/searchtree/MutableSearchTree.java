package net.minecraft.client.searchtree;

public interface MutableSearchTree<T> extends SearchTree<T>
{
    void add(T p_119869_);

    void clear();

    void refresh();
}

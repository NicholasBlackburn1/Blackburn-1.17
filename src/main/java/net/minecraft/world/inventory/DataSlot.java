package net.minecraft.world.inventory;

public abstract class DataSlot
{
    private int prevValue;

    public static DataSlot forContainer(final ContainerData pData, final int pIdx)
    {
        return new DataSlot()
        {
            public int get()
            {
                return pData.get(pIdx);
            }
            public void set(int pValue)
            {
                pData.set(pIdx, pValue);
            }
        };
    }

    public static DataSlot m_39406_(final int[] p_39407_, final int p_39408_)
    {
        return new DataSlot()
        {
            public int get()
            {
                return p_39407_[p_39408_];
            }
            public void set(int pValue)
            {
                p_39407_[p_39408_] = pValue;
            }
        };
    }

    public static DataSlot standalone()
    {
        return new DataSlot()
        {
            private int value;
            public int get()
            {
                return this.value;
            }
            public void set(int pValue)
            {
                this.value = pValue;
            }
        };
    }

    public abstract int get();

    public abstract void set(int pValue);

    public boolean checkAndClearUpdateFlag()
    {
        int i = this.get();
        boolean flag = i != this.prevValue;
        this.prevValue = i;
        return flag;
    }
}

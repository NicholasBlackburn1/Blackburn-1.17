package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;

public class VertexFormatElement
{
    private final VertexFormatElement.Type type;
    private final VertexFormatElement.Usage usage;
    private final int index;
    private final int count;
    private final int byteSize;
    private String name;
    private int attributeIndex = -1;

    public VertexFormatElement(int p_86037_, VertexFormatElement.Type p_86038_, VertexFormatElement.Usage p_86039_, int p_86040_)
    {
        if (this.supportsUsage(p_86037_, p_86039_))
        {
            this.usage = p_86039_;
            this.type = p_86038_;
            this.index = p_86037_;
            this.count = p_86040_;
            this.byteSize = p_86038_.getSize() * this.count;
        }
        else
        {
            throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
        }
    }

    private boolean supportsUsage(int pIndex, VertexFormatElement.Usage pUsage)
    {
        return pIndex == 0 || pUsage == VertexFormatElement.Usage.UV;
    }

    public final VertexFormatElement.Type getType()
    {
        return this.type;
    }

    public final VertexFormatElement.Usage getUsage()
    {
        return this.usage;
    }

    public final int getCount()
    {
        return this.count;
    }

    public final int getIndex()
    {
        return this.index;
    }

    public String toString()
    {
        return this.name != null ? this.name : this.count + "," + this.usage.getName() + "," + this.type.getName();
    }

    public final int getByteSize()
    {
        return this.byteSize;
    }

    public final boolean isPosition()
    {
        return this.usage == VertexFormatElement.Usage.POSITION;
    }

    public boolean equals(Object p_86053_)
    {
        if (this == p_86053_)
        {
            return true;
        }
        else if (p_86053_ != null && this.getClass() == p_86053_.getClass())
        {
            VertexFormatElement vertexformatelement = (VertexFormatElement)p_86053_;

            if (this.count != vertexformatelement.count)
            {
                return false;
            }
            else if (this.index != vertexformatelement.index)
            {
                return false;
            }
            else if (this.type != vertexformatelement.type)
            {
                return false;
            }
            else
            {
                return this.usage == vertexformatelement.usage;
            }
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int i = this.type.hashCode();
        i = 31 * i + this.usage.hashCode();
        i = 31 * i + this.index;
        return 31 * i + this.count;
    }

    public void setupBufferState(int p_166966_, long p_166967_, int p_166968_)
    {
        this.usage.setupBufferState(this.count, this.type.getGlType(), p_166968_, p_166967_, this.index, p_166966_);
    }

    public void clearBufferState(int p_166964_)
    {
        this.usage.clearBufferState(this.index, p_166964_);
    }

    public final int getElementCount()
    {
        return this.count;
    }

    public String getName()
    {
        return this.name;
    }

    public VertexFormatElement setName(String name)
    {
        this.name = name;
        return this;
    }

    public int getAttributeIndex()
    {
        return this.attributeIndex;
    }

    public void setAttributeIndex(int attributeIndex)
    {
        this.attributeIndex = attributeIndex;
    }

    public int getAttributeIndex(int elementIndex)
    {
        return this.attributeIndex;
    }

    public static enum Type
    {
        FLOAT(4, "Float", 5126),
        UBYTE(1, "Unsigned Byte", 5121),
        BYTE(1, "Byte", 5120),
        USHORT(2, "Unsigned Short", 5123),
        SHORT(2, "Short", 5122),
        UINT(4, "Unsigned Int", 5125),
        INT(4, "Int", 5124);

        private final int size;
        private final String name;
        private final int glType;

        private Type(int p_86071_, String p_86072_, int p_86073_)
        {
            this.size = p_86071_;
            this.name = p_86072_;
            this.glType = p_86073_;
        }

        public int getSize()
        {
            return this.size;
        }

        public String getName()
        {
            return this.name;
        }

        public int getGlType()
        {
            return this.glType;
        }
    }

    public static enum Usage
    {
        POSITION("Position", (p_268777_0_, p_268777_1_, p_268777_2_, p_268777_3_, p_268777_5_, p_268777_6_) -> {
            GlStateManager._enableVertexAttribArray(p_268777_6_);
            GlStateManager._vertexAttribPointer(p_268777_6_, p_268777_0_, p_268777_1_, false, p_268777_2_, p_268777_3_);
        }, (p_268774_0_, p_268774_1_) -> {
            GlStateManager._disableVertexAttribArray(p_268774_1_);
        }),
        NORMAL("Normal", (p_268767_0_, p_268767_1_, p_268767_2_, p_268767_3_, p_268767_5_, p_268767_6_) -> {
            GlStateManager._enableVertexAttribArray(p_268767_6_);
            GlStateManager._vertexAttribPointer(p_268767_6_, p_268767_0_, p_268767_1_, true, p_268767_2_, p_268767_3_);
        }, (p_268764_0_, p_268764_1_) -> {
            GlStateManager._disableVertexAttribArray(p_268764_1_);
        }),
        COLOR("Vertex Color", (p_268757_0_, p_268757_1_, p_268757_2_, p_268757_3_, p_268757_5_, p_268757_6_) -> {
            GlStateManager._enableVertexAttribArray(p_268757_6_);
            GlStateManager._vertexAttribPointer(p_268757_6_, p_268757_0_, p_268757_1_, true, p_268757_2_, p_268757_3_);
        }, (p_268754_0_, p_268754_1_) -> {
            GlStateManager._disableVertexAttribArray(p_268754_1_);
        }),
        UV("UV", (p_268747_0_, p_268747_1_, p_268747_2_, p_268747_3_, p_268747_5_, p_268747_6_) -> {
            GlStateManager._enableVertexAttribArray(p_268747_6_);

            if (p_268747_1_ == 5126)
            {
                GlStateManager._vertexAttribPointer(p_268747_6_, p_268747_0_, p_268747_1_, false, p_268747_2_, p_268747_3_);
            }
            else {
                GlStateManager._vertexAttribIPointer(p_268747_6_, p_268747_0_, p_268747_1_, p_268747_2_, p_268747_3_);
            }
        }, (p_268744_0_, p_268744_1_) -> {
            GlStateManager._disableVertexAttribArray(p_268744_1_);
        }),
        PADDING("Padding", (p_268737_0_, p_268737_1_, p_268737_2_, p_268737_3_, p_268737_5_, p_268737_6_) -> {
        }, (p_268734_0_, p_268734_1_) -> {
        }),
        GENERIC("Generic", (p_268727_0_, p_268727_1_, p_268727_2_, p_268727_3_, p_268727_5_, p_268727_6_) -> {
            GlStateManager._enableVertexAttribArray(p_268727_6_);
            GlStateManager._vertexAttribPointer(p_268727_6_, p_268727_0_, p_268727_1_, false, p_268727_2_, p_268727_3_);
        }, (p_268724_0_, p_268724_1_) -> {
            GlStateManager._disableVertexAttribArray(p_268724_1_);
        });

        private final String name;
        private final VertexFormatElement.Usage.SetupState setupState;
        private final VertexFormatElement.Usage.ClearState clearState;

        private Usage(String p_166975_, VertexFormatElement.Usage.SetupState p_166976_, VertexFormatElement.Usage.ClearState p_166977_)
        {
            this.name = p_166975_;
            this.setupState = p_166976_;
            this.clearState = p_166977_;
        }

        void setupBufferState(int p_166982_, int p_166983_, int p_166984_, long p_166985_, int p_166986_, int p_166987_)
        {
            this.setupState.setupBufferState(p_166982_, p_166983_, p_166984_, p_166985_, p_166986_, p_166987_);
        }

        public void clearBufferState(int p_166979_, int p_166980_)
        {
            this.clearState.clearBufferState(p_166979_, p_166980_);
        }

        public String getName()
        {
            return this.name;
        }

        @FunctionalInterface
        interface ClearState {
            void clearBufferState(int p_167050_, int p_167051_);
        }

        @FunctionalInterface
        interface SetupState {
            void setupBufferState(int p_167053_, int p_167054_, int p_167055_, long p_167056_, int p_167057_, int p_167058_);
        }
    }
}

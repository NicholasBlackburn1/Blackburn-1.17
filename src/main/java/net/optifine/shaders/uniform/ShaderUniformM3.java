package net.optifine.shaders.uniform;

import com.mojang.math.Matrix3f;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import net.optifine.util.BufferUtil;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

public class ShaderUniformM3 extends ShaderUniformBase
{
    private boolean transpose;
    private FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(9);
    private FloatBuffer tempBuffer = MemoryUtil.memAllocFloat(9);

    public ShaderUniformM3(String name)
    {
        super(name);
    }

    public void setValue(Matrix3f matrixIn)
    {
        this.transpose = false;
        ((Buffer)this.tempBuffer).clear();
        matrixIn.store(this.tempBuffer);
        this.setValue(false, this.tempBuffer);
    }

    public void setValue(boolean transpose, FloatBuffer matrix)
    {
        this.transpose = transpose;
        ((Buffer)this.matrixBuffer).clear();
        this.matrixBuffer.put(matrix);
        ((Buffer)this.matrixBuffer).rewind();
        int i = this.getLocation();

        if (i >= 0)
        {
            flushRenderBuffers();
            GL20.glUniformMatrix3fv(i, transpose, this.matrixBuffer);
            this.checkGLError();
        }
    }

    public float getValue(int row, int col)
    {
        int i = this.transpose ? col * 3 + row : row * 3 + col;
        return this.matrixBuffer.get(i);
    }

    protected void onProgramSet(int program)
    {
    }

    protected void resetValue()
    {
        BufferUtil.fill(this.matrixBuffer, 0.0F);
    }
}

package org.jeromerodrigo.lucidengine.graphics.glutils;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;

public class VertexArray implements VertexData {

    protected transient VertexAttribute[] attributes;

    private transient int totalComponents;
    private transient final FloatBuffer buffer;
    private transient final int vertCount;

    /**
     * Constructs a vertex array
     * @param vertCount the number of VERTICES; e.g. 3 verts to make a triangle,
     *  regardless of number of attributes
     * @param attributes a list of attributes per vertex
     */
    public VertexArray(final int vertCount, final VertexAttribute ... attributes) {
        this.attributes = attributes;
        for (final VertexAttribute attribute : attributes) {
            totalComponents += attribute.getNumComponents();
        }
        this.vertCount = vertCount;

        //our buffer which holds our data
        this.buffer = BufferUtils.createFloatBuffer(vertCount * totalComponents);
    }

    public VertexArray(final int vertCount, final List<VertexAttribute> attributes) {
        this(vertCount, attributes.toArray(new VertexAttribute[attributes.size()]));
    }

    @Override
    public VertexArray flip() {
        buffer.flip();
        return this;
    }

    @Override
    public VertexArray clear() {
        buffer.clear();
        return this;
    }

    @Override
    public VertexArray put(final float[] verts, final int offset, final int length) {
        buffer.put(verts, offset, length);
        return this;
    }

    /**
     * Puts a value into the vertex array
     */

    @Override
    public VertexArray put(final float vertex) {
        buffer.put(vertex);
        return this;
    }

    /**
     * Returns the vertex array buffer
     * @return vertex array buffer
     */

    @Override
    public FloatBuffer getBuffer() {
        return buffer;
    }

    /**
     * Gets the total number of components of the vertex array
     */

    @Override
    public int getTotalNumComponents() {
        return totalComponents;
    }

    /**
     * Gets the number vertices held in the vertex array
     */

    @Override
    public int getVertexCount() {
        return vertCount;
    }

    /**
     * Binds the vertex array
     */

    @Override
    public void bind() {
        int offset = 0;
        //4 bytes per float
        final int stride = totalComponents * 4;

        for (final VertexAttribute attribute : attributes) {
            buffer.position(offset);
            glEnableVertexAttribArray(attribute.getLocation());
            glVertexAttribPointer(attribute.getLocation(), attribute.getNumComponents(),
                    false, stride, buffer);
            offset += attribute.getNumComponents();
        }
    }

    @Override
    public void draw(final int geom, final int first, final int count) {
        glDrawArrays(geom, first, count);
    }

    /**
     * Unbinds the vertex array
     */

    @Override
    public void unbind() {
        for (final VertexAttribute attribute : attributes) {
            glDisableVertexAttribArray(attribute.getLocation());
        }
    }
}
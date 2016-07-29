package org.jeromerodrigo.lucidengine.graphics.glutils;

import java.nio.FloatBuffer;

public interface VertexData {

    void bind();
    void draw(int geom, int first, int count);
    void unbind();

    VertexData clear();
    VertexData flip();
    VertexData put(float[] verts, int offset, int length);
    VertexData put(float vertex);

    FloatBuffer getBuffer();

    int getTotalNumComponents();
    int getVertexCount();

}

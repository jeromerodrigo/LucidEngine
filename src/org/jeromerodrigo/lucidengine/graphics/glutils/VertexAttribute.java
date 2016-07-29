package org.jeromerodrigo.lucidengine.graphics.glutils;

public class VertexAttribute {

    private transient final String name;
    private transient final int numComponents;
    private transient final int location;

    public VertexAttribute(final int location, final String name,
            final int numComponents) {
        this.location = location;
        this.name = name;
        this.numComponents = numComponents;
    }

    public final String getName() {
        return name;
    }

    public final int getNumComponents() {
        return numComponents;
    }

    public final int getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return name +" (" + numComponents+")";
    }
}
package org.jeromerodrigo.lucidengine.graphics.texture;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GLContext;

import de.matthiasmann.twl.utils.PNGDecoder;

/**
 * This is a minimal implementation of an OpenGL texture loader. A more complete
 * implementation would support multiple filetypes (JPEG, BMP, TGA, etc), allow
 * for parameters such as width/height to be changed (by uploading new texture
 * data), allow for different internal formats, async loading, different targets
 * (i.e. for GL_TEXTURE_3D or array textures), mipmaps, compression, etc.
 * 
 * @author davedes
 */
public final class Texture implements ITexture {

    protected int id;
    protected int width;
    protected int height;

    public static int toPowerOfTwo(final int n) {
        return 1 << (32 - Integer.numberOfLeadingZeros(n - 1));
    }

    public static boolean isPowerOfTwo(final int n) {
        return (n & -n) == n;
    }

    public static boolean isNPOTSupported() {
        return GLContext.getCapabilities().GL_ARB_texture_non_power_of_two;
    }

    // Some filters, included here for convenience
    public static final int LINEAR = GL_LINEAR;
    public static final int NEAREST = GL_NEAREST;
    public static final int LINEAR_MIPMAP_LINEAR = GL_LINEAR_MIPMAP_LINEAR;
    public static final int LINEAR_MIPMAP_NEAREST = GL_LINEAR_MIPMAP_NEAREST;
    public static final int NEAREST_MIPMAP_NEAREST = GL_NEAREST_MIPMAP_NEAREST;
    public static final int NEAREST_MIPMAP_LINEAR = GL_NEAREST_MIPMAP_LINEAR;

    // Some wrap modes, included here for convenience
    public static final int CLAMP = GL_CLAMP;
    public static final int CLAMP_TO_EDGE = GL_CLAMP_TO_EDGE;
    public static final int REPEAT = GL_REPEAT;

    public static final int DEFAULT_FILTER = NEAREST;
    public static final int DEFAULT_WRAP = REPEAT;

    protected Texture() {
        // does nothing... for subclasses
    }

    /**
     * Creates an empty OpenGL texture with the given width and height, where
     * each pixel is transparent black (0, 0, 0, 0) and the wrap mode is
     * CLAMP_TO_EDGE and the filter is NEAREST.
     * 
     * @param width
     *            the width of the texture
     * @param height
     *            the height of the texture
     */
    public Texture(final int width, final int height) {
        this(width, height, DEFAULT_FILTER);
    }

    /**
     * Creates an empty OpenGL texture with the given width and height, where
     * each pixel is transparent black (0, 0, 0, 0) and the wrap mode is
     * CLAMP_TO_EDGE.
     * 
     * @param width
     *            the width of the texture
     * @param height
     *            the height of the texture
     * @param filter
     *            the filter to use
     */
    public Texture(final int width, final int height, final int filter) {
        this(width, height, filter, DEFAULT_WRAP);
    }

    /**
     * Creates an empty OpenGL texture with the given width and height, where
     * each pixel is transparent black (0, 0, 0, 0).
     * 
     * @param width
     *            the width of the texture
     * @param height
     *            the height of the texture
     * @param minFilter
     *            the minification filter to use
     * @param magFilter
     *            the magnification filter to use
     * @param wrap
     *            the wrap mode to use
     * @param genMipmaps
     *            - whether to generate mipmaps, which requires
     *            GL_EXT_framebuffer_object (or GL3+)
     */
    public Texture(final int width, final int height, final int filter,
            final int wrap) {
        glEnable(getTarget());
        id = glGenTextures();
        this.width = width;
        this.height = height;
        bind();

        setFilter(filter);
        setWrap(wrap);

        final ByteBuffer buf = BufferUtils.createByteBuffer(width * height * 4);
        upload(GL_RGBA, buf);
    }

    public Texture(final URL pngRef) throws IOException {
        this(pngRef, DEFAULT_FILTER);
    }

    public Texture(final URL pngRef, final int filter) throws IOException {
        this(pngRef, filter, DEFAULT_WRAP);
    }

    public Texture(final URL pngRef, final int filter, final int wrap)
            throws IOException {
        this(pngRef, filter, filter, wrap, false);
    }

    public Texture(final URL pngRef, final int filter, final boolean genMipmap)
            throws IOException {
        this(pngRef, filter, filter, DEFAULT_WRAP, genMipmap);
    }

    public Texture(final URL pngRef, final int minFilter, final int magFilter,
            final int wrap, final boolean genMipmap) throws IOException {
        // TODO: npot check
        InputStream input = null;
        try {
            input = pngRef.openStream();
            final PNGDecoder dec = new PNGDecoder(input);

            width = dec.getWidth();
            height = dec.getHeight();
            final ByteBuffer buf = BufferUtils.createByteBuffer(4 * width
                    * height);
            dec.decode(buf, width * 4, PNGDecoder.Format.RGBA);
            buf.flip();

            glEnable(getTarget());
            id = glGenTextures();

            bind();
            setFilter(minFilter, magFilter);
            setWrap(wrap);
            upload(GL_RGBA, buf);

            // use EXT since we are targeting 2.0+
            if (genMipmap) {
                EXTFramebufferObject.glGenerateMipmapEXT(getTarget());
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    private int getTarget() {
        return GL_TEXTURE_2D;
    }

    public int getID() {
        return id;
    }

    protected void setUnpackAlignment() {
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
    }

    /**
     * Uploads image data with the dimensions of this Texture.
     * 
     * @param dataFormat
     *            the format, e.g. GL_RGBA
     * @param data
     *            the byte data
     */
    public void upload(final int dataFormat, final ByteBuffer data) {
        bind();
        setUnpackAlignment();
        glTexImage2D(getTarget(), 0, GL_RGBA, width, height, 0, dataFormat,
                GL_UNSIGNED_BYTE, data);
    }

    /**
     * Uploads a sub-image within this texture.
     * 
     * @param x
     *            the destination x offset
     * @param y
     *            the destination y offset, with lower-left origin
     * @param width
     *            the width of the sub image data
     * @param height
     *            the height of the sub image data
     * @param dataFormat
     *            the format of the sub image data, e.g. GL_RGBA
     * @param data
     *            the sub image data
     */
    public void upload(final int x, final int y, final int width,
            final int height, final int dataFormat, final ByteBuffer data) {
        bind();
        setUnpackAlignment();
        glTexSubImage2D(getTarget(), 0, x, y, width, height, dataFormat,
                GL_UNSIGNED_BYTE, data);
    }

    public void setFilter(final int filter) {
        setFilter(filter, filter);
    }

    public void setFilter(final int minFilter, final int magFilter) {
        bind();
        glTexParameteri(getTarget(), GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(getTarget(), GL_TEXTURE_MAG_FILTER, magFilter);
    }

    public void setWrap(final int wrap) {
        bind();
        glTexParameteri(getTarget(), GL_TEXTURE_WRAP_S, wrap);
        glTexParameteri(getTarget(), GL_TEXTURE_WRAP_T, wrap);
    }

    public final void bind() {
        if (!valid()) {
            throw new IllegalStateException(
                    "trying to bind a texture that was disposed");
        }
        glBindTexture(getTarget(), id);
    }

    public void dispose() {
        if (valid()) {
            glDeleteTextures(id);
            id = 0;
        }
    }

    /**
     * Returns true if this texture is valid, aka it has not been disposed.
     * 
     * @return true if id!=0
     */
    public boolean valid() {
        return id != 0;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Returns this object; used for abstraction with SpriteBatch.
     * 
     * @return this texture object
     */
    @Override
    public Texture getTexture() {
        return this;
    }

    @Override
    public float getU() {
        return 0f;
    }

    @Override
    public float getV() {
        return 0f;
    }

    @Override
    public float getU2() {
        return 1f;
    }

    @Override
    public float getV2() {
        return 1f;
    }
}

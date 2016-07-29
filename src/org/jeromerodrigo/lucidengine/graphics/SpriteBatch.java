package org.jeromerodrigo.lucidengine.graphics;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import org.jeromerodrigo.lucidengine.graphics.glutils.VertexData;
import org.jeromerodrigo.lucidengine.graphics.glutils.ShaderProgram;
import org.jeromerodrigo.lucidengine.graphics.glutils.VertexArray;
import org.jeromerodrigo.lucidengine.graphics.glutils.VertexAttribute;
import org.jeromerodrigo.lucidengine.graphics.texture.ITexture;
import org.jeromerodrigo.lucidengine.graphics.texture.Texture;
import org.jeromerodrigo.lucidengine.graphics.texture.TextureRegion;
import org.jeromerodrigo.lucidengine.util.MatrixUtil;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;

/**
 * @author Matt (mdesl) DesLauriers
 * @author matheusdev
 */
public class SpriteBatch {
    public static final String U_TEXTURE = "u_texture";
    public static final String U_PROJ_VIEW = "u_projView";

    public static final String ATTR_COLOR = "Color";
    public static final String ATTR_POSITION = "Position";
    public static final String ATTR_TEXCOORD = "TexCoord";

    public static final String DEFAULT_VERT_SHADER = "uniform mat4 "
            + U_PROJ_VIEW + ";\n" + "attribute vec4 " + ATTR_COLOR + ";\n"
            + "attribute vec2 " + ATTR_TEXCOORD + ";\n" + "attribute vec2 "
            + ATTR_POSITION + ";\n" + "varying vec4 vColor;\n"
            + "varying vec2 vTexCoord; \n" + "void main() {\n" + "	vColor = "
            + ATTR_COLOR + ";\n" + "	vTexCoord = " + ATTR_TEXCOORD + ";\n"
            + "	gl_Position = " + U_PROJ_VIEW + " * vec4(" + ATTR_POSITION
            + ".xy, 0.0, 1.0);\n" + "}";

    public static final String DEFAULT_FRAG_SHADER = "uniform sampler2D "
            + U_TEXTURE + ";\n" + "varying vec4 vColor;\n"
            + "varying vec2 vTexCoord;\n" + "void main() {\n"
            + "	vec4 texColor = texture2D(" + U_TEXTURE + ", vTexCoord);\n"
            + "	gl_FragColor = vColor * texColor;\n" + "}";

    public static final List<VertexAttribute> ATTRIBUTES = Arrays.asList(
            new VertexAttribute(0, ATTR_POSITION, 2), new VertexAttribute(1,
                    ATTR_COLOR, 4), new VertexAttribute(2, ATTR_TEXCOORD, 2));

    static ShaderProgram defaultShader;

    protected FloatBuffer buf16;
    protected Matrix4f projMatrix = new Matrix4f();
    protected Matrix4f viewMatrix = new Matrix4f();
    protected Matrix4f transpositionPool = new Matrix4f();
    private Matrix4f projViewMatrix = new Matrix4f(); // only for re-using
    // Matrix4f objects

    protected Texture texture;
    protected ShaderProgram program;

    protected VertexData data;

    private int idx;
    private final int maxIndex;

    private final Color color = new Color();
    private boolean drawing = false;

    public static ShaderProgram getDefaultShader() throws LWJGLException {
        return defaultShader == null ? (defaultShader = new ShaderProgram(
                DEFAULT_VERT_SHADER, DEFAULT_FRAG_SHADER, ATTRIBUTES))
                : defaultShader;
    }

    public SpriteBatch(final ShaderProgram program) {
        this(program, 1000);
    }

    public SpriteBatch(final ShaderProgram program, final int size) {
        this(program, 1000, true);
    }

    public SpriteBatch(final ShaderProgram program, final int size,
            final boolean updateUniforms) {
        this.program = program;

        // later we can do some abstraction to replace this with VBOs...
        this.data = new VertexArray(size * 6, ATTRIBUTES);

        // max indices before we need to flush the renderer
        maxIndex = size * 6;

        // default size
        resize(Display.getWidth(), Display.getHeight());
    }

    /**
     * Creates a sprite batch with a default shader, shared across all sprite
     * batches.
     *
     * @param size
     * @throws LWJGLException
     */
    public SpriteBatch(final int size) throws LWJGLException {
        this(getDefaultShader(), size);
    }

    public SpriteBatch() throws LWJGLException {
        this(1000);
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projMatrix;
    }

    public Matrix4f getCombinedMatrix() {
        Matrix4f.mul(Matrix4f.transpose(projMatrix, transpositionPool),
                viewMatrix, projViewMatrix);
        return projViewMatrix;
    }

    /**
     * A convenience method to resize the projection matrix to the given
     * dimensions, using y-down ortho 2D. This will invoke a call to
     * updateMatrices.
     *
     * @param width
     * @param height
     */
    public void resize(final int width, final int height) {
        projMatrix = MatrixUtil.toOrtho2D(projMatrix, 0, 0, width, height);
        updateUniforms();
    }

    /**
     * Sets this SpriteBatch's color to the RGBA values of the given color
     * object.
     *
     * @param color
     *            the RGBA values to use
     */
    public void setColor(final Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    /**
     * Sets this SpriteBatch's color to the given RGBA values.
     *
     * @param r
     *            the red value
     * @param g
     *            the green value
     * @param b
     *            the blue value
     * @param a
     *            the alpha value
     */
    public void setColor(final float r, final float g, final float b,
            final float a) {
        color.set(r, g, b, a);
    }

    /**
     * Call to multiply the the projection with the view matrix and save the
     * result in the uniform mat4 {@value #U_PROJ_VIEW}, as well as update the
     * {@value #U_TEXTURE} uniform.
     */
    public void updateUniforms() {
        updateUniforms(program);
    }

    /**
     * Call to multiply the the projection with the view matrix and save the
     * result in the uniform mat4 {@value #U_PROJ_VIEW}, as well as update the
     * {@value #U_TEXTURE} uniform.
     */
    public void updateUniforms(final ShaderProgram program) {
        projViewMatrix = getCombinedMatrix();

        // bind the program before sending uniforms
        program.use();

        final boolean oldStrict = ShaderProgram.isStrictMode();

        // disable strict mode so we don't run into any problems
        ShaderProgram.setStrictMode(false);

        // we can now utilize ShaderProgram's hash map which may be better than
        // glGetUniformLocation

        // Store the the multiplied matrix in the "projViewMatrix"-uniform:
        program.setUniformMatrix(U_PROJ_VIEW, false, projViewMatrix);

        // upload texcoord 0
        program.setUniformi(U_TEXTURE, 0);

        // reset strict mode
        ShaderProgram.setStrictMode(oldStrict);
    }

    /**
     * An advanced call that allows you to change the shader without uploading
     * shader uniforms. This will flush the batch if we are within begin().
     *
     * @param program
     * @param updateUniforms
     *            whether to call updateUniforms after changing the programs
     */
    public void setShader(final ShaderProgram program,
            final boolean updateUniforms) {
        if (program == null) {
            throw new IllegalArgumentException(
                    "shader cannot be null; use getDefaultShader instead");
        }
        if (drawing) {
            flush();
        }
        this.program = program; // now switch the shader
        if (updateUniforms) {
            updateUniforms();
        } else if (drawing) {
            program.use();
        }
    }

    /**
     * Changes the shader and updates it with the current texture and projView
     * uniforms. This will flush the batch if we are within begin().
     *
     * @param program
     *            the new program to use
     */
    public void setShader(final ShaderProgram program) {
        setShader(program, true);
    }

    public ShaderProgram getShader() {
        return program;
    }

    public void begin() {
        if (drawing) {
            throw new IllegalStateException(
                    "must not be drawing before calling begin()");
        }
        drawing = true;
        program.use();
        idx = 0;
        texture = null;
    }

    public void end() {
        if (!drawing) {
            throw new IllegalStateException(
                    "must be drawing before calling end()");
        }
        drawing = false;
        flush();
    }

    public void flush() {
        if (idx > 0) {
            data.flip();
            render();
            idx = 0;
            data.clear();
        }
    }

    public void drawRegion(final Texture tex, final float srcX,
            final float srcY, final float srcWidth, final float srcHeight,
            final float dstX, final float dstY) {
        drawRegion(tex, srcX, srcY, srcWidth, srcHeight, dstX, dstY, srcWidth,
                srcHeight);
    }

    public void drawRegion(final Texture tex, final float srcX,
            final float srcY, final float srcWidth, final float srcHeight,
            final float dstX, final float dstY, final float dstWidth,
            final float dstHeight) {
        final float u = srcX / tex.getWidth();
        final float v = srcY / tex.getHeight();
        final float u2 = (srcX + srcWidth) / tex.getWidth();
        final float v2 = (srcY + srcHeight) / tex.getHeight();
        draw(tex, dstX, dstY, dstWidth, dstHeight, u, v, u2, v2);
    }

    public void drawRegion(final TextureRegion region, final float srcX,
            final float srcY, final float srcWidth, final float srcHeight,
            final float dstX, final float dstY) {
        drawRegion(region, srcX, srcY, srcWidth, srcHeight, dstX, dstY,
                srcWidth, srcHeight);
    }

    public void drawRegion(final TextureRegion region, final float srcX,
            final float srcY, final float srcWidth, final float srcHeight,
            final float dstX, final float dstY, final float dstWidth,
            final float dstHeight) {
        drawRegion(region.getTexture(), region.getRegionX() + srcX,
                region.getRegionY() + srcY, srcWidth, srcHeight, dstX, dstY,
                dstWidth, dstHeight);
    }

    public void draw(final ITexture tex, final float x, final float y) {
        draw(tex, x, y, tex.getWidth(), tex.getHeight());
    }

    public void draw(final ITexture tex, final float x, final float y,
            final float width, final float height) {
        draw(tex, x, y, width, height, tex.getU(), tex.getV(), tex.getU2(),
                tex.getV2());
    }

    public void draw(final ITexture tex, final float x, final float y,
            final float originX, final float originY,
            final float rotationRadians) {
        draw(tex, x, y, tex.getWidth(), tex.getHeight(), originX, originY,
                rotationRadians);
    }

    public void draw(final ITexture tex, final float x, final float y,
            final float width, final float height, final float originX,
            final float originY, final float rotationRadians) {
        draw(tex, x, y, width, height, originX, originY, rotationRadians,
                tex.getU(), tex.getV(), tex.getU2(), tex.getV2());
    }

    public void draw(final ITexture tex, final float x, final float y,
            final float width, final float height, final float originX,
            final float originY, final float rotationRadians, final float u,
            final float v, final float u2, final float v2) {
        checkFlush(tex);
        final float r = color.r;
        final float g = color.g;
        final float b = color.b;
        final float a = color.a;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        if (rotationRadians != 0) {
            final float scaleX = 1f; // width/tex.getWidth();
            final float scaleY = 1f; // height/tex.getHeight();

            final float cx = originX * scaleX;
            final float cy = originY * scaleY;

            final float p1x = -cx;
            final float p1y = -cy;
            final float p2x = width - cx;
            final float p2y = -cy;
            final float p3x = width - cx;
            final float p3y = height - cy;
            final float p4x = -cx;
            final float p4y = height - cy;

            final float cos = (float) Math.cos(rotationRadians);
            final float sin = (float) Math.sin(rotationRadians);

            x1 = x + cos * p1x - sin * p1y + cx; // TOP LEFT
            y1 = y + sin * p1x + cos * p1y + cy;
            x2 = x + cos * p2x - sin * p2y + cx; // TOP RIGHT
            y2 = y + sin * p2x + cos * p2y + cy;
            x3 = x + cos * p3x - sin * p3y + cx; // BOTTOM RIGHT
            y3 = y + sin * p3x + cos * p3y + cy;
            x4 = x + cos * p4x - sin * p4y + cx; // BOTTOM LEFT
            y4 = y + sin * p4x + cos * p4y + cy;
        } else {
            x1 = x;
            y1 = y;

            x2 = x + width;
            y2 = y;

            x3 = x + width;
            y3 = y + height;

            x4 = x;
            y4 = y + height;
        }

        // top left, top right, bottom left
        vertex(x1, y1, r, g, b, a, u, v);
        vertex(x2, y2, r, g, b, a, u2, v);
        vertex(x4, y4, r, g, b, a, u, v2);

        // top right, bottom right, bottom left
        vertex(x2, y2, r, g, b, a, u2, v);
        vertex(x3, y3, r, g, b, a, u2, v2);
        vertex(x4, y4, r, g, b, a, u, v2);
    }

    public void draw(final ITexture tex, final float x, final float y,
            final float width, final float height, final float u,
            final float v, final float u2, final float v2) {
        draw(tex, x, y, width, height, x, y, 0f, u, v, u2, v2);
    }

    /**
     * Renders a texture using custom vertex attributes; e.g. for different
     * vertex colours. This will ignore the current batch color and
     * "x/y translation", as well as the U/V coordinates of the given ITexture.
     *
     * @param tex
     *            the texture to use
     * @param vertices
     *            an array of 6 vertices, each holding 8 attributes (total = 48
     *            elements)
     * @param offset
     *            the offset from the vertices array to start from
     */
    public void draw(final ITexture tex, final float[] vertices,
            final int offset) {
        checkFlush(tex);
        data.put(vertices, offset, data.getTotalNumComponents() * 6);
        idx += 6;
    }

    VertexData vertex(final float x, final float y, final float r,
            final float g, final float b, final float a, final float u,
            final float v) {
        data.put(x).put(y).put(r).put(g).put(b).put(a).put(u).put(v);
        idx++;
        return data;
    }

    protected void checkFlush(final ITexture sprite) {
        if (sprite == null || sprite.getTexture() == null) {
            throw new IllegalArgumentException("null texture");
        }

        // we need to bind a different texture/type. this is
        // for convenience; ideally the user should order
        // their rendering wisely to minimize texture binds
        if (sprite.getTexture() != this.texture || idx >= maxIndex) {
            // apply the last texture
            flush();
            this.texture = sprite.getTexture();
        }
    }

    private void render() {
        if (texture != null) {
            texture.bind();
        }
        data.bind();
        data.draw(GL_TRIANGLES, 0, idx);
        data.unbind();
    }
}

package org.jeromerodrigo.lucidengine.graphics.glutils;

import static org.lwjgl.opengl.GL20.GL_ACTIVE_ATTRIBUTES;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORMS;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetActiveAttrib;
import static org.lwjgl.opengl.GL20.glGetActiveAttribSize;
import static org.lwjgl.opengl.GL20.glGetActiveAttribType;
import static org.lwjgl.opengl.GL20.glGetActiveUniform;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniform;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform2i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform3i;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniform4i;
import static org.lwjgl.opengl.GL20.glUniformMatrix3;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * A complete ShaderProgram utility wrapper.
 * 
 * @author davedes
 */
public class ShaderProgram {

    private static FloatBuffer fbuf16;
    private static IntBuffer ibuf4;

    // a simple struct for attrib data; ideally we should find the
    // component count to utilize our VertexAttrib class
    protected static class Attrib {
        String name = null;
        int type = -1;
        int size = 0; // for arrays
        int location = -1;
    }

    /** The vertex shader type (GL_VERTEX_SHADER). */
    public static final int VERTEX_SHADER = GL_VERTEX_SHADER;
    /** The fragment shader type (GL_FRAGMENT_SHADER). */
    public static final int FRAGMENT_SHADER = GL_FRAGMENT_SHADER;
    private static boolean strict = false;

    /**
     * Returns true if the extensions GL_ARB_shader_objects,
     * GL_ARB_vertex_shader, and GL_ARB_fragment shader are present.
     * 
     * @return true if shaders are supported
     */
    public static boolean isSupported() {
        final ContextCapabilities c = GLContext.getCapabilities();
        return c.GL_ARB_shader_objects && c.GL_ARB_vertex_shader
                && c.GL_ARB_fragment_shader;
        // return c.OpenGL20;
    }

    /**
     * Whether shader programs are to use "strict" uniform/attribute name
     * checking (default: disabled). That is, when strict mode is enabled,
     * trying to modify or retrieve uniform/attribute data by name will fail and
     * throw an IllegalArgumentException if there exists no 'active'
     * uniforms/attributes by the given name. (In GLSL, declared uniforms might
     * still be "inactive" if they are not used.) If strict mode is disabled,
     * getting/setting uniform/attribute data will fail silently if the name is
     * not found.
     * 
     * @param enabled
     *            true to enable strict mode
     */
    public static void setStrictMode(final boolean enabled) {
        strict = enabled;
    }

    /**
     * Returns <tt>true</tt> if shader programs are to use "strict"
     * uniform/attribute name checking (default: disabled). That is, when strict
     * mode is enabled, trying to modify or retrieve uniform/attribute data by
     * name will fail and throw an IllegalArgumentException if there exists no
     * 'active' uniforms/attributes by the given name. (In GLSL, declared
     * uniforms might still be "inactive" if they are not used.) If strict mode
     * is disabled, getting/setting uniform/attribute data will fail silently if
     * the name is not found.
     * 
     * @return true if strict mode is enabled
     */
    public static boolean isStrictMode() {
        return strict;
    }

    /** The OpenGL handle for this shader program object. */
    protected int program;
    /** The log for this program. */
    protected String log = "";
    /** A map of uniforms by <name, int>. */
    protected Map<String, Integer> uniforms = new HashMap<String, Integer>();
    /** A list of attribute data. */
    protected Attrib[] attributes;

    /** The vertex shader source. */
    protected String vertShaderSource;
    /** The fragment shader source. */
    protected String fragShaderSource;
    /** The OpenGL handle for this program's vertex shader object. */
    protected int vert;
    /** The OpenGL handle for this program's fragment shader object. */
    protected int frag;

    /**
     * Creates a new shader program with the given vertex and fragment shader
     * source code. The given source code is compiled, then the shaders attached
     * and linked.
     * 
     * If shaders are not supported on this system (isSupported returns false),
     * a LWJGLException will be thrown.
     * 
     * If one of the shaders does not compile successfully, a LWJGLException
     * will be thrown.
     * 
     * If there was a problem in linking the shaders to the program, a
     * LWJGLException will be thrown and the program will be deleted.
     * 
     * Before linking, the specified attribLocations will be bound using
     * glBindAttribLocation.
     * 
     * @param vertexShaderSource
     *            the shader code to compile, attach and link
     * @param fragShaderSource
     *            the frag code to compile, attach and link
     * @param attribLocations
     *            the attribute locations to bind
     * @throws LWJGLException
     *             if there was an issue
     * @throws IllegalArgumentException
     *             if there was an issue
     */
    public ShaderProgram(final String vertexShaderSource,
            final String fragShaderSource,
            final List<VertexAttribute> attribLocations) throws LWJGLException {
        if (vertexShaderSource == null || fragShaderSource == null) {
            throw new IllegalArgumentException("shader source must be non-null");
        }
        if (!isSupported()) {
            throw new LWJGLException(
                    "no shader support found; shaders require OpenGL 2.0");
        }
        this.vertShaderSource = vertexShaderSource;
        this.fragShaderSource = fragShaderSource;
        vert = compileShader(VERTEX_SHADER, vertexShaderSource);
        frag = compileShader(FRAGMENT_SHADER, fragShaderSource);
        program = createProgram();
        try {
            linkProgram(attribLocations);
        } catch (final LWJGLException e) {
            dispose();
            throw e;
        }
        // TODO: for convenience it might be nice to warn non-critical errors in
        // a log
        // but ideally the user should do that himself
        // if (log != null && log.length() != 0)
        // Util.warn(log);
    }

    /**
     * Creates a new shader program with the given vertex and fragment shader
     * source code. The given source code is compiled, then the shaders attached
     * and linked.
     * 
     * If shaders are not supported on this system (isSupported returns false),
     * a LWJGLException will be thrown.
     * 
     * If one of the shaders does not compile successfully, a LWJGLException
     * will be thrown.
     * 
     * If there was a problem in linking the shaders to the program, a
     * LWJGLException will be thrown and the program will be deleted.
     * 
     * @param vertexShaderSource
     *            the shader code to compile, attach and link
     * @param fragShaderSource
     *            the frag code to compile, attach and link
     * @throws LWJGLException
     *             if there was an issue
     * @throws IllegalArgumentException
     *             if there was an issue
     */
    public ShaderProgram(final String vertexShaderSource,
            final String fragShaderSource) throws LWJGLException {
        this(vertexShaderSource, fragShaderSource, null);
    }

    /**
     * Subclasses may wish to implement this to manually handle program/shader
     * creation, compiling, and linking. This constructor does nothing; users
     * will need to call compileShader, createProgram and linkProgram manually.
     */
    protected ShaderProgram() {
        // Not used
    }

    /**
     * Creates a shader program and returns its OpenGL handle. If the result is
     * zero, an exception will be thrown.
     * 
     * @return the OpenGL handle for the newly created shader program
     * @throws SlimException
     *             if the result is zero
     */
    private int createProgram() throws LWJGLException {
        final int program = glCreateProgram();
        if (program == 0) {
            throw new LWJGLException(
                    "could not create program; check ShaderProgram.isSupported()");
        }
        return program;
    }

    /** Used only for clearer debug messages. */
    private String shaderTypeString(final int type) {
        if (type == FRAGMENT_SHADER) {
            return "FRAGMENT_SHADER";
        } else if (type == VERTEX_SHADER) {
            return "VERTEX_SHADER";
        } else if (type == GL_GEOMETRY_SHADER) {
            return "GEOMETRY_SHADER";
        } else {
            return "shader";
        }
    }

    /**
     * Compiles a shader from source and returns its handle. If the compilation
     * failed, a SlimException will be thrown. If the compilation had error,
     * info or warnings messages, they will be appended to this program's log.
     * 
     * @param type
     *            the type to use in compilation
     * @param source
     *            the source code to compile
     * @return the resulting ID
     * @throws SlimException
     *             if compilation was unsuccessful
     */
    protected int compileShader(final int type, final String source)
            throws LWJGLException {
        final int shader = glCreateShader(type);
        if (shader == 0) {
            throw new LWJGLException(
                    "could not create shader object; check ShaderProgram.isSupported()");
        }
        glShaderSource(shader, source);
        glCompileShader(shader);

        final int comp = glGetShaderi(shader, GL_COMPILE_STATUS);
        final int len = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
        final String t = shaderTypeString(type);
        final String err = glGetShaderInfoLog(shader, len);
        if (err != null && err.length() != 0) {
            log += t + " compile log:\n" + err + "\n";
        }
        if (comp == GL11.GL_FALSE) {
            throw new LWJGLException(log.length() != 0 ? log
                    : "Could not compile " + shaderTypeString(type));
        }
        return shader;
    }

    /**
     * Called to attach vertex and fragment; users may override this for more
     * specific purposes.
     */
    protected void attachShaders() {
        glAttachShader(getID(), vert);
        glAttachShader(getID(), frag);
    }

    /**
     * Tries to bind the given attributes by location, then calls
     * attachShaders() and links the program.
     * 
     * @param attribLocations
     *            tries to bind the given attributes in their order of
     *            appearance
     * @throws LWJGLException
     *             if this program is invalid (released) or if the link was
     *             unsuccessful
     */
    protected final void linkProgram(final List<VertexAttribute> attribLocations)
            throws LWJGLException {
        if (!valid()) {
            throw new LWJGLException(
                    "trying to link an invalid (i.e. released) program");
        }

        uniforms.clear();

        // bind user-defined attribute locations
        if (attribLocations != null) {
            for (final VertexAttribute a : attribLocations) {
                if (a != null) {
                    glBindAttribLocation(program, a.getLocation(), a.getName());
                }
            }
        }

        attachShaders();
        glLinkProgram(program);
        final int comp = glGetProgrami(program, GL_LINK_STATUS);
        final int len = glGetProgrami(program, GL_INFO_LOG_LENGTH);
        final String err = glGetProgramInfoLog(program, len);
        if (err != null && err.length() != 0) {
            log = err + "\n" + log;
        }
        if (log != null) {
            log = log.trim();
        }
        if (comp == GL11.GL_FALSE) {
            throw new LWJGLException(log.length() != 0 ? log
                    : "Could not link program");
        }

        fetchUniforms();
        fetchAttributes();
    }

    /**
     * Returns the full log of compiling/linking errors, info, warnings, etc.
     * 
     * @return the full log of this ShaderProgram
     */
    public final String getLog() {
        return log;
    }

    /**
     * Enables this shader for use -- only one shader can be bound at a time.
     * Calling bind() when another program is bound will simply make this object
     * the active program.
     * 
     * @throw IllegalStateException if this program is invalid
     */
    public final void use() {
        if (!valid()) {
            throw new IllegalStateException(
                    "trying to enable a program that is not valid");
        }
        glUseProgram(program);
    }

    /**
     * Detaches and releases the shaders associated with this program. This can
     * be called after linking a program in order to free up memory (as the
     * shaders are no longer needed), however, since it is not a commonly used
     * feature and thus not well tested on all drivers, it should be used with
     * caution. Shaders shouldn't be used after being released.
     */
    public final void disposeShaders() {
        if (vert != 0) {
            glDetachShader(getID(), vert);
            glDeleteShader(vert);
            vert = 0;
        }
        if (frag != 0) {
            glDetachShader(getID(), frag);
            glDeleteShader(frag);
            frag = 0;
        }
    }

    /**
     * If this program has not yet been released, this will releases this
     * program and its shaders. To only release the shaders (not the program
     * itself), call disposeShaders(). Programs will be considered "invalid"
     * after being released, and should no longer be used.
     */
    public final void dispose() {
        if (program != 0) {
            disposeShaders();
            glDeleteProgram(program);
            program = 0;
        }
    }

    /**
     * Returns the OpenGL handle for this program's vertex shader.
     * 
     * @return the vertex ID
     */
    public final int getVertexShaderID() {
        return vert;
    }

    /**
     * Returns the OpenGL handle for this program's fragment shader.
     * 
     * @return the fragment ID
     */
    public final int getFragmentShaderID() {
        return frag;
    }

    /**
     * Returns the source code for the vertex shader.
     * 
     * @return the source code
     */
    public final String getVertexShaderSource() {
        return vertShaderSource;
    }

    /**
     * Returns the source code for the fragment shader.
     * 
     * @return the source code
     */
    public final String getFragmentShaderSource() {
        return fragShaderSource;
    }

    /**
     * Returns the OpenGL handle for this shader program
     * 
     * @return the program ID
     */
    public final int getID() {
        return program;
    }

    /**
     * A shader program is "valid" if it's ID is not zero. Upon releasing a
     * program, the ID will be set to zero.
     * 
     * @return whether this program is valid
     */
    public final boolean valid() {
        return program != 0;
    }

    /**
     * Retrieves the uniforms from the shader program
     */

    private void fetchUniforms() {
        final int len = glGetProgrami(program, GL_ACTIVE_UNIFORMS);
        // max length of all uniforms stored in program
        final int strLen = glGetProgrami(program, GL_ACTIVE_UNIFORM_MAX_LENGTH);

        for (int i = 0; i < len; i++) {
            final String name = glGetActiveUniform(program, i, strLen);
            final int id = glGetUniformLocation(program, name);
            uniforms.put(name, id);
        }
    }

    /**
     * Retrieves the attributes from the shader program
     */

    private void fetchAttributes() {
        final int len = glGetProgrami(program, GL_ACTIVE_ATTRIBUTES);
        // max length of all uniforms stored in program
        final int strLen = glGetProgrami(program,
                GL_ACTIVE_ATTRIBUTE_MAX_LENGTH);

        attributes = new Attrib[len];
        for (int i = 0; i < len; i++) {
            final Attrib a = new Attrib();
            // TODO: use proper FloatBuffer method instead of these convenience
            // methods
            a.name = glGetActiveAttrib(program, i, strLen);
            a.size = glGetActiveAttribSize(program, i);
            a.type = glGetActiveAttribType(program, i);
            a.location = glGetAttribLocation(program, a.name);
            attributes[i] = a;
        }
    }

    /**
     * Returns the location of the uniform by name. If the uniform is not found
     * and we are in strict mode, an IllegalArgumentException will be thrown,
     * otherwise -1 will be returned if no active uniform by that name exists.
     * 
     * @param name
     *            the uniform name
     * @return the ID (location) in the shader program
     */
    public final int getUniformLocation(final String name) {
        int location = -1;
        final Integer locI = uniforms.get(name);
        if (locI == null) { // maybe it's not yet cached?
            location = glGetUniformLocation(program, name);
            uniforms.put(name, location);
        } else {
            location = locI.intValue();
        }
        // throw an exception if not found...
        if (location == -1 && strict) {
            throw new IllegalArgumentException("no active uniform by name '"
                    + name + "' "
                    + "(disable strict compiling to suppress warnings)");
        }
        return location;
    }

    /**
     * Gets the attribute
     * 
     * @param name
     *            the name of the attribute
     * @return the attribute
     */

    final Attrib attrib(final String name) {
        for (final Attrib attribute : attributes) {
            if (name.equals(attribute.name)) {
                return attribute;
            }
        }
        // throw an exception if not found...
        if (strict) {
            throw new IllegalArgumentException("no active attribute by name '"
                    + name + "' "
                    + "(disable strict compiling to suppress warnings)");
        }
        return null;
    }

    /**
     * Returns the location of the attribute by name. If the attribute is not
     * found and we are in strict mode, an IllegalArgumentException will be
     * thrown, otherwise -1 will be returned if no active attribute by that name
     * exists.
     * 
     * @param name
     *            the attribute name
     * @return the ID (location) in the shader program
     */
    public final int getAttributeLocation(final String name) {
        final Attrib a = attrib(name);
        return a != null ? a.location : -1;
    }

    /**
     * Returns the type of the attribute by name. If the attribute is not found
     * and we are in strict mode, an IllegalArgumentException will be thrown,
     * otherwise -1 will be returned if no active attribute by that name exists.
     * 
     * @param name
     *            the attribute name
     * @return the ID (location) in the shader program
     */
    public final int getAttributeType(final String name) {
        final Attrib a = attrib(name);
        return a != null ? a.type : -1;
    }

    /**
     * Returns the size of the attribute by name (i.e. for arrays). If the
     * attribute is not found and we are in strict mode, an
     * IllegalArgumentException will be thrown, otherwise -1 will be returned if
     * no active attribute by that name exists.
     * 
     * @param name
     *            the attribute name
     * @return the ID (location) in the shader program
     */
    public final int getAttributeSize(final String name) {
        final Attrib a = attrib(name);
        return a != null ? a.size : -1;
    }

    /**
     * Creates and returns an array for all active attributes that were found
     * when linking the program.
     * 
     * @return an array list of active uniform names
     */
    public final String[] getAttributeNames() {
        final String[] s = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            s[i] = attributes[i].name;
        }
        return s;
    }

    /**
     * Creates and returns an array for all active uniforms that were found when
     * linking the program.
     * 
     * @return an array list of active uniform names
     */
    public final String[] getUniformNames() {
        return uniforms.keySet().toArray(new String[uniforms.size()]);
    }

    /**
     * Returns true if an active uniform by the given name was found when
     * linking.
     * 
     * @param name
     *            the active uniform name
     * @return true if the uniform was found
     */
    public final boolean hasUniform(final String name) {
        return uniforms.containsKey(name);
    }

    /**
     * Returns true if an active attribute by the given name was found when
     * linking.
     * 
     * @param name
     *            the active attribute name
     * @return true if the attribute was found
     */
    public final boolean hasAttribute(final String name) {
        for (final Attrib attribute : attributes) {
            if (name.equals(attribute.name)) {
                return true;
            }
        }
        return false;
    }

    /** ----- UNIFORM GETTERS ----- */

    private FloatBuffer uniformf(final int loc) {
        if (fbuf16 == null) {
            fbuf16 = BufferUtils.createFloatBuffer(16);
        }
        fbuf16.clear();
        if (loc == -1) {
            return fbuf16;
        }
        getUniform(loc, fbuf16);
        return fbuf16;
    }

    private IntBuffer uniformi(final int loc) {
        if (ibuf4 == null) {
            ibuf4 = BufferUtils.createIntBuffer(4);
        }
        ibuf4.clear();
        if (loc == -1) {
            return ibuf4;
        }
        getUniform(loc, ibuf4);
        return ibuf4;
    }

    /**
     * Retrieves data from a uniform and places it in the given buffer.
     * 
     * @param loc
     *            the location of the uniform
     * @param buf
     *            the buffer to place the data
     */
    public final void getUniform(final int loc, final FloatBuffer buf) {
        glGetUniform(program, loc, buf);
    }

    /**
     * Retrieves data from a uniform and places it in the given buffer.
     * 
     * @param loc
     *            the location of the uniform
     * @param buf
     *            the buffer to place the data
     */
    public final void getUniform(final int loc, final IntBuffer buf) {
        glGetUniform(program, loc, buf);
    }

    /**
     * Retrieves data from a uniform and places it in the given buffer. If
     * strict mode is enabled, this will throw an IllegalArgumentException if
     * the given uniform is not 'active' -- i.e. if GLSL determined that the
     * shader isn't using it. If strict mode is disabled, this method will
     * return <tt>true</tt> if the uniform was found, and <tt>false</tt>
     * otherwise.
     * 
     * @param name
     *            the name of the uniform
     * @param buf
     *            the buffer to place the data
     * @return true if the uniform was found, false if there is no active
     *         uniform by that name
     */
    public final boolean getUniform(final String name, final FloatBuffer buf) {
        final int id = getUniformLocation(name);
        if (id == -1) {
            return false;
        }
        getUniform(id, buf);
        return true;
    }

    /**
     * Retrieves data from a uniform and places it in the given buffer. If
     * strict mode is enabled, this will throw an IllegalArgumentException if
     * the given uniform is not 'active' -- i.e. if GLSL determined that the
     * shader isn't using it. If strict mode is disabled, this method will
     * return <tt>true</tt> if the uniform was found, and <tt>false</tt>
     * otherwise.
     * 
     * @param name
     *            the name of the uniform
     * @param buf
     *            the buffer to place the data
     * @return true if the uniform was found, false if there is no active
     *         uniform by that name
     */
    public final boolean getUniform(final String name, final IntBuffer buf) {
        final int id = getUniformLocation(name);
        if (id == -1) {
            return false;
        }
        getUniform(id, buf);
        return true;
    }

    /**
     * A convenience method to retrieve an integer/sampler2D uniform. The return
     * values are undefined if the uniform is not found.
     * 
     * @param loc
     *            the uniform location
     * @return the value
     */
    public final int getUniform1i(final int loc) {
        return uniformi(loc).get(0);
    }

    /**
     * A convenience method to retrieve an integer/sampler2D uniform. The return
     * values are undefined if the uniform is not found.
     * 
     * @param name
     *            the uniform location
     * @return the value
     */
    public final int getUniform1i(final String name) {
        return getUniform1i(getUniformLocation(name));
    }

    /**
     * A convenience method to retrieve an ivec2 uniform; for maximum
     * performance and memory efficiency you should use getUniform(int,
     * IntBuffer) with a shared buffer.
     * 
     * @param loc
     *            the uniform location
     * @return a newly created int[] array with 2 elements; e.g. (x, y)
     */
    public final int[] getUniform2i(final int loc) {
        final IntBuffer buf = uniformi(loc);
        return new int[] { buf.get(0), buf.get(1) };
    }

    /**
     * A convenience method to retrieve an ivec2 uniform; for maximum
     * performance and memory efficiency you should use getUniform(int,
     * IntBuffer) with a shared buffer. The return values are undefined if the
     * uniform is not found.
     * 
     * @param name
     *            the uniform name
     * @return a newly created int[] array with 2 elements; e.g. (x, y)
     */
    public final int[] getUniform2i(final String name) {
        return getUniform2i(getUniformLocation(name));
    }

    /**
     * A convenience method to retrieve an ivec3 uniform; for maximum
     * performance and memory efficiency you should use getUniform(String,
     * IntBuffer) with a shared buffer.
     * 
     * @param loc
     *            the name of the uniform
     * @return a newly created int[] array with 3 elements; e.g. (x, y, z)
     */
    public final int[] getUniform3i(final int loc) {
        final IntBuffer buf = uniformi(loc);
        return new int[] { buf.get(0), buf.get(1), buf.get(2) };
    }

    /**
     * A convenience method to retrieve an ivec3 uniform; for maximum
     * performance and memory efficiency you should use getUniform(String,
     * IntBuffer) with a shared buffer. The return values are undefined if the
     * uniform is not found.
     * 
     * @param name
     *            the name of the uniform
     * @return a newly created int[] array with 3 elements; e.g. (x, y, z)
     */
    public final int[] getUniform3i(final String name) {
        return getUniform3i(getUniformLocation(name));
    }

    /**
     * A convenience method to retrieve an ivec4 uniform; for maximum
     * performance and memory efficiency you should use getUniform(String,
     * IntBuffer) with a shared buffer.
     * 
     * @param loc
     *            the location of the uniform
     * @return a newly created int[] array with 2 elements; e.g. (r, g, b, a)
     */
    public final int[] getUniform4i(final int loc) {
        final IntBuffer buf = uniformi(loc);
        return new int[] { buf.get(0), buf.get(1), buf.get(2), buf.get(3) };
    }

    /**
     * A convenience method to retrieve an ivec4 uniform; for maximum
     * performance and memory efficiency you should use getUniform(String,
     * IntBuffer) with a shared buffer. The return values are undefined if the
     * uniform is not found.
     * 
     * @param name
     *            the name of the uniform
     * @return a newly created int[] array with 2 elements; e.g. (r, g, b, a)
     */
    public final int[] getUniform4i(final String name) {
        return getUniform4i(getUniformLocation(name));
    }

    /**
     * A convenience method to retrieve a float uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @return the value
     */
    public final float getUniform1f(final int loc) {
        return uniformf(loc).get(0);
    }

    /**
     * A convenience method to retrieve a float uniform. The return values are
     * undefined if the uniform is not found.
     * 
     * @param name
     *            the uniform name
     * @return the value
     */
    public final float getUniform1f(final String name) {
        return getUniform1f(getUniformLocation(name));
    }

    /**
     * A convenience method to retrieve a vec2 uniform; for maximum performance
     * and memory efficiency you should use getUniform(String, FloatBuffer) with
     * a shared buffer.
     * 
     * @param location
     *            the location of the uniform
     * @return a newly created float[] array with 2 elements; e.g. (x, y)
     */
    public final float[] getUniform2f(final int loc) {
        final FloatBuffer buf = uniformf(loc);
        return new float[] { buf.get(0), buf.get(1) };
    }

    /**
     * A convenience method to retrieve a vec2 uniform; for maximum performance
     * and memory efficiency you should use getUniform(String, FloatBuffer) with
     * a shared buffer. The return values are undefined if the uniform is not
     * found.
     * 
     * @param name
     *            the name of the uniform
     * @return a newly created float[] array with 2 elements; e.g. (x, y)
     */
    public final float[] getUniform2f(final String name) {
        return getUniform2f(getUniformLocation(name));
    }

    /**
     * A convenience method to retrieve a vec3 uniform; for maximum performance
     * and memory efficiency you should use getUniform(String, FloatBuffer) with
     * a shared buffer.
     * 
     * @param loc
     *            the location of the uniform
     * @return a newly created float[] array with 3 elements; e.g. (x, y, z)
     */
    public final float[] getUniform3f(final int loc) {
        final FloatBuffer buf = uniformf(loc);
        return new float[] { buf.get(0), buf.get(1), buf.get(2) };
    }

    /**
     * A convenience method to retrieve a vec3 uniform; for maximum performance
     * and memory efficiency you should use getUniform(String, FloatBuffer) with
     * a shared buffer. The return values are undefined if the uniform is not
     * found.
     * 
     * @param name
     *            the name of the uniform
     * @return a newly created float[] array with 3 elements; e.g. (x, y, z)
     */
    public final float[] getUniform3f(final String name) {
        return getUniform3f(getUniformLocation(name));
    }

    /**
     * A convenience method to retrieve a vec4 uniform; for maximum performance
     * and memory efficiency you should use getUniform(String, FloatBuffer) with
     * a shared buffer.
     * 
     * @param loc
     *            the location of the uniform
     * @return a newly created float[] array with 4 elements; e.g. (r, g, b, a)
     */
    public final float[] getUniform4f(final int loc) {
        final FloatBuffer buf = uniformf(loc);
        return new float[] { buf.get(0), buf.get(1), buf.get(2), buf.get(3) };
    }

    /**
     * A convenience method to retrieve a vec4 uniform; for maximum performance
     * and memory efficiency you should use getUniform(String, FloatBuffer) with
     * a shared buffer. The return values are undefined if the uniform is not
     * found.
     * 
     * @param name
     *            the name of the uniform
     * @return a newly created float[] array with 4 elements; e.g. (r, g, b, a)
     */
    public final float[] getUniform4f(final String name) {
        return getUniform4f(getUniformLocation(name));
    }

    /** ----- UNIFORM LOCATION SETTERS ----- */

    /**
     * Sets the value of a float uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param f
     *            the float value
     */
    public final void setUniformf(final int loc, final float f) {
        if (loc == -1) {
            return;
        }
        glUniform1f(loc, f);
    }

    /**
     * Sets the value of a vec2 uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param a
     *            vec.x / tex.s
     * @param b
     *            vec.y / tex.t
     */
    public final void setUniformf(final int loc, final float a, final float b) {
        if (loc == -1) {
            return;
        }
        glUniform2f(loc, a, b);
    }

    /**
     * Sets the value of a vec3 uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param a
     *            vec.x / color.r / tex.s
     * @param b
     *            vec.y / color.g / tex.t
     * @param c
     *            vec.z / color.b / tex.p
     */
    public final void setUniformf(final int loc, final float a, final float b,
            final float c) {
        if (loc == -1) {
            return;
        }
        glUniform3f(loc, a, b, c);
    }

    /**
     * Sets the value of a vec4 uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param a
     *            vec.x / color.r
     * @param b
     *            vec.y / color.g
     * @param c
     *            vec.z / color.b
     * @param d
     *            vec.w / color.a
     */
    public final void setUniformf(final int loc, final float a, final float b,
            final float c, final float d) {
        if (loc == -1) {
            return;
        }
        glUniform4f(loc, a, b, c, d);
    }

    /**
     * Sets the value of an int or sampler2D uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param i
     *            the integer / active texture (e.g. 0 for TEXTURE0)
     */
    public final void setUniformi(final int loc, final int i) {
        if (loc == -1) {
            return;
        }
        glUniform1i(loc, i);
    }

    /**
     * Sets the value of a ivec2 uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param a
     *            vec.x / tex.s
     * @param b
     *            vec.y / tex.t
     */
    public final void setUniformi(final int loc, final int a, final int b) {
        if (loc == -1) {
            return;
        }
        glUniform2i(loc, a, b);
    }

    /**
     * Sets the value of a ivec3 uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param a
     *            vec.x / color.r
     * @param b
     *            vec.y / color.g
     * @param c
     *            vec.z / color.b
     */
    public final void setUniformi(final int loc, final int a, final int b,
            final int c) {
        if (loc == -1) {
            return;
        }
        glUniform3i(loc, a, b, c);
    }

    /**
     * Sets the value of a ivec4 uniform.
     * 
     * @param loc
     *            the location of the uniform
     * @param a
     *            vec.x / color.r
     * @param b
     *            vec.y / color.g
     * @param c
     *            vec.z / color.b
     * @param d
     *            vec.w / color.a
     */
    public final void setUniformi(final int loc, final int a, final int b,
            final int c, final int d) {
        if (loc == -1) {
            return;
        }
        glUniform4i(loc, a, b, c, d);
    }

    /** ----- UNIFORM STRING SETTERS ----- */

    /**
     * Sets the value of a float uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param f
     *            the float value
     */
    public final void setUniformf(final String name, final float f) {
        setUniformf(getUniformLocation(name), f);
    }

    /**
     * Sets the value of a vec2 uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param a
     *            vec.x / tex.s
     * @param b
     *            vec.y / tex.t
     */
    public final void setUniformf(final String name, final float a,
            final float b) {
        setUniformf(getUniformLocation(name), a, b);
    }

    /**
     * Sets the value of a vec3 uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param a
     *            vec.x / color.r / tex.s
     * @param b
     *            vec.y / color.g / tex.t
     * @param c
     *            vec.z / color.b / tex.p
     */
    public final void setUniformf(final String name, final float a,
            final float b, final float c) {
        setUniformf(getUniformLocation(name), a, b, c);
    }

    /**
     * Sets the value of a vec4 uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param a
     *            vec.x / color.r
     * @param b
     *            vec.y / color.g
     * @param c
     *            vec.z / color.b
     * @param d
     *            vec.w / color.a
     */
    public final void setUniformf(final String name, final float a,
            final float b, final float c, final float d) {
        setUniformf(getUniformLocation(name), a, b, c, d);
    }

    /**
     * Sets the value of an int or sampler2D uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param i
     *            the integer / active texture (e.g. 0 for TEXTURE0)
     */
    public final void setUniformi(final String name, final int i) {
        setUniformi(getUniformLocation(name), i);
    }

    /**
     * Sets the value of a ivec2 uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param a
     *            vec.x / tex.s
     * @param b
     *            vec.y / tex.t
     */
    public final void setUniformi(final String name, final int a, final int b) {
        setUniformi(getUniformLocation(name), a, b);
    }

    /**
     * Sets the value of a ivec3 uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param a
     *            vec.x / color.r
     * @param b
     *            vec.y / color.g
     * @param c
     *            vec.z / color.b
     */
    public final void setUniformi(final String name, final int a, final int b,
            final int c) {
        setUniformi(getUniformLocation(name), a, b, c);
    }

    /**
     * Sets the value of a ivec4 uniform.
     * 
     * @param name
     *            the name of the uniform
     * @param a
     *            vec.x / color.r
     * @param b
     *            vec.y / color.g
     * @param c
     *            vec.z / color.b
     * @param d
     *            vec.w / color.a
     */
    public final void setUniformi(final String name, final int a, final int b,
            final int c, final int d) {
        setUniformi(getUniformLocation(name), a, b, c, d);
    }

    /** ----- MATRIX SETTERS ----- */

    public final void setUniformMatrix(final String name,
            final boolean transpose, final Matrix3f m) {
        setUniformMatrix(getUniformLocation(name), transpose, m);
    }

    public void setUniformMatrix(final String name, final boolean transpose,
            final Matrix4f m) {
        setUniformMatrix(getUniformLocation(name), transpose, m);
    }

    public void setUniformMatrix(final int loc, final boolean transpose,
            final Matrix3f m) {
        if (loc == -1) {
            return;
        }
        if (fbuf16 == null) {
            fbuf16 = BufferUtils.createFloatBuffer(16);
        }
        fbuf16.clear();
        m.store(fbuf16);
        fbuf16.flip();
        glUniformMatrix3(loc, transpose, fbuf16);
    }

    public void setUniformMatrix(final int loc, final boolean transpose,
            final Matrix4f m) {
        if (loc == -1) {
            return;
        }
        if (fbuf16 == null) {
            fbuf16 = BufferUtils.createFloatBuffer(16);
        }
        fbuf16.clear();
        m.store(fbuf16);
        fbuf16.flip();
        glUniformMatrix4(loc, transpose, fbuf16);
    }

    /** ----- VECTOR SETTERS ----- */

    /**
     * Sets the uniform using 2 float vector
     * 
     * @param name
     *            the name of the uniform
     * @param v
     *            the vector
     */

    public final void setUniformf(final String name, final Vector2f v) {
        setUniformf(getUniformLocation(name), v);
    }

    /**
     * Sets the uniform using 3 float vector
     * 
     * @param name
     *            the name of the uniform
     * @param v
     *            the vector
     */

    public final void setUniformf(final String name, final Vector3f v) {
        setUniformf(getUniformLocation(name), v);
    }

    /**
     * Sets the uniform using 4 float vector
     * 
     * @param name
     *            the name of the uniform
     * @param v
     *            the vector
     */

    public final void setUniformf(final String name, final Vector4f v) {
        setUniformf(getUniformLocation(name), v);
    }

    /**
     * Sets the uniform using 2 float vector by location
     * 
     * @param loc
     *            the location of the uniform
     * @param v
     *            the vector
     */

    public final void setUniformf(final int loc, final Vector2f v) {
        if (loc == -1) {
            return;
        }
        setUniformf(loc, v.x, v.y);
    }

    /**
     * Sets the uniform using 3 float vector by location
     * 
     * @param loc
     *            the location of the uniform
     * @param v
     *            the vector
     */

    public final void setUniformf(final int loc, final Vector3f v) {
        if (loc == -1) {
            return;
        }
        setUniformf(loc, v.x, v.y, v.z);
    }

    /**
     * Sets the uniform using 4 float vector by location
     * 
     * @param loc
     *            the location of the uniform
     * @param v
     *            the vector
     */

    public final void setUniformf(final int loc, final Vector4f v) {
        if (loc == -1) {
            return;
        }
        setUniformf(loc, v.x, v.y, v.z, v.w);
    }
}

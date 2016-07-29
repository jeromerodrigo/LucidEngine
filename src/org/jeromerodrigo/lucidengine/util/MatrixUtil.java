package org.jeromerodrigo.lucidengine.util;

import org.lwjgl.util.vector.Matrix4f;

public final class MatrixUtil {

    private MatrixUtil() {

    }

    /**
     * Sets the given matrix to an orthographic 2D projection matrix, and
     * returns it. If the given matrix is null, a new one will be created and
     * returned.
     *
     * @param m
     *            the matrix to re-use, or null to create a new matrix
     * @param x
     * @param y
     * @param width
     * @param height
     * @return the given matrix, or a newly created matrix if none was specified
     */
    public static Matrix4f toOrtho2D(final Matrix4f m, final float x,
            final float y, final float width, final float height) {
        return toOrtho(m, x, x + width, y + height, y, 1, -1);
    }

    /**
     * Sets the given matrix to an orthographic 2D projection matrix, and
     * returns it. If the given matrix is null, a new one will be created and
     * returned.
     *
     * @param m
     *            the matrix to re-use, or null to create a new matrix
     * @param x
     * @param y
     * @param width
     * @param height
     * @param near
     *            near clipping plane
     * @param far
     *            far clipping plane
     * @return the given matrix, or a newly created matrix if none was specified
     */
    public static Matrix4f toOrtho2D(final Matrix4f m, final float x,
            final float y, final float width, final float height,
            final float near, final float far) {
        return toOrtho(m, x, x + width, y, y + height, near, far);
    }

    /**
     * Sets the given matrix to an orthographic projection matrix, and returns
     * it. If the given matrix is null, a new one will be created and returned.
     *
     * @param m
     *            the matrix to re-use, or null to create a new matrix
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     *            near clipping plane
     * @param far
     *            far clipping plane
     * @return the given matrix, or a newly created matrix if none was specified
     */
    public static Matrix4f toOrtho(final Matrix4f m, final float left,
            final float right, final float bottom, final float top,
            final float near, final float far) {

        Matrix4f matrix;

        if (m == null) {
            matrix = new Matrix4f();
        } else {
            matrix = m;
        }

        final float xOrth = 2 / (right - left);
        final float yOrth = 2 / (top - bottom);
        final float zOrth = -2 / (far - near);

        final float tx = -(right + left) / (right - left);
        final float ty = -(top + bottom) / (top - bottom);
        final float tz = -(far + near) / (far - near);

        matrix.m00 = xOrth;
        matrix.m10 = 0;
        matrix.m20 = 0;
        matrix.m30 = 0;
        matrix.m01 = 0;
        matrix.m11 = yOrth;
        matrix.m21 = 0;
        matrix.m31 = 0;
        matrix.m02 = 0;
        matrix.m12 = 0;
        matrix.m22 = zOrth;
        matrix.m32 = 0;
        matrix.m03 = tx;
        matrix.m13 = ty;
        matrix.m23 = tz;
        matrix.m33 = 1;
        return matrix;
    }

    /**
     * Sets the matrix to a projection matrix with a near- and far plane, a
     * field of view in degrees and an aspect ratio.
     *
     * @param near
     *            The near plane
     * @param far
     *            The far plane
     * @param fov
     *            The field of view in degrees
     * @param aspectRatio
     *            The aspect ratio
     * @return This matrix for the purpose of chaining methods together.
     */
    public static Matrix4f setToProjection(final Matrix4f m, final float near,
            final float far, final float fov, final float aspectRatio) {

        Matrix4f matrix;

        if (m == null) {
            matrix = new Matrix4f();
        } else {
            matrix = m;
        }

        matrix.setIdentity();

        final float lengthFd = (float) (1.0 / Math.tan(fov * (Math.PI / 180)
                / 2.0));
        final float lengthA1 = (far + near) / (near - far);
        final float lengthA2 = 2 * far * near / (near - far);

        matrix.m00 = lengthFd / aspectRatio;
        matrix.m10 = 0;
        matrix.m20 = 0;
        matrix.m30 = 0;
        matrix.m01 = 0;
        matrix.m11 = lengthFd;
        matrix.m21 = 0;
        matrix.m31 = 0;
        matrix.m02 = 0;
        matrix.m12 = 0;
        matrix.m22 = lengthA1;
        matrix.m32 = -1;
        matrix.m03 = 0;
        matrix.m13 = 0;
        matrix.m23 = lengthA2;
        matrix.m33 = 0;
        return matrix;
    }

}

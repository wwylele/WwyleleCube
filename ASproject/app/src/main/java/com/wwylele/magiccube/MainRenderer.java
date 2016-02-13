package com.wwylele.magiccube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.content.res.Resources;

public class MainRenderer implements Renderer {

    public final float[] mtxProj = new float[16];
    public final float[] mtxView = new float[16];

    public static final float viewDistanceConst = 2.3f;
    public float viewDistance;

    private Resources resource;
    private Cube cube;

    MainRenderer(Resources res, Cube cube) {
        resource = res;
        this.cube = cube;

    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw();

    }

    // Matrix.perspectiveM for low API
    public static void perspectiveM(float[] m, int offset,
                                    float fovy, float aspect, float zNear, float zFar) {
        float f = 1.0f / (float) Math.tan(fovy * (Math.PI / 360.0));
        float rangeReciprocal = 1.0f / (zNear - zFar);

        m[offset + 0] = f / aspect;
        m[offset + 1] = 0.0f;
        m[offset + 2] = 0.0f;
        m[offset + 3] = 0.0f;

        m[offset + 4] = 0.0f;
        m[offset + 5] = f;
        m[offset + 6] = 0.0f;
        m[offset + 7] = 0.0f;

        m[offset + 8] = 0.0f;
        m[offset + 9] = 0.0f;
        m[offset + 10] = (zFar + zNear) * rangeReciprocal;
        m[offset + 11] = -1.0f;

        m[offset + 12] = 0.0f;
        m[offset + 13] = 0.0f;
        m[offset + 14] = 2.0f * zFar * zNear * rangeReciprocal;
        m[offset + 15] = 0.0f;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        // Matrix.perspectiveM(mtxProj, 0, 45, ratio, 1, 100);
        perspectiveM(mtxProj, 0, 45, ratio, 1, 100);
        viewDistance = viewDistanceConst;
        if (ratio < 1) viewDistance /= ratio;
        Matrix.setLookAtM(mtxView, 0, viewDistance, 0, 0, 0, 0, 0, 0, 0, 1);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        Sticker.init(resource, this);
    }

}

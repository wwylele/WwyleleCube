package com.wwylele.magiccube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.content.res.Resources;

class MainRenderer implements Renderer {

    public final float[] mtxProj = new float[16];
    public final float[] mtxView = new float[16];

    public static final float viewDistanceConst = 2.3f;
    private float viewDistance;

    private final Resources resource;
    private final Cube cube;

    MainRenderer(Resources res, Cube cube) {
        resource = res;
        this.cube = cube;

    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw();

    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float xMax, yMax, zNear = 1, zFar = 100;
        yMax = zNear * 0.414f;
        xMax = yMax * ratio;
        Matrix.frustumM(mtxProj, 0, -xMax, xMax, -yMax, yMax, zNear, zFar);
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

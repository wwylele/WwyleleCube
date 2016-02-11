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

    public static final float viewDistance = 3.5f;

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

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.perspectiveM(mtxProj, 0, 45, ratio, 1, 100);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        Sticker.init(resource, this);
        Matrix.setLookAtM(mtxView, 0, viewDistance, 0, 0, 0, 0, 0, 0, 0, 1);
    }

}

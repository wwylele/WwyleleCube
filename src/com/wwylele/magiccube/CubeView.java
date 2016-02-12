package com.wwylele.magiccube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.MotionEvent;

public class CubeView extends GLSurfaceView {
    private final MainRenderer renderer;
    private Cube cube;

    public CubeView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        cube = ((MainActivity) context).cube;
        renderer = new MainRenderer(context.getResources(), cube);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    // transform a point from screen to a ray in the model space
    private void pointToModelSpace(float x, float y, float[] from, float[] direction) {
        float[] mtxMV = new float[16];
        Matrix.multiplyMM(mtxMV, 0, renderer.mtxView, 0, cube.mtxModel, 0);
        int[] view = { 0, 0, getWidth(), getHeight() };
        float[] modelSpaceFrom = new float[4], modelSpaceTo = new float[4];
        GLU.gluUnProject(x, getHeight() - y, 1.0f, mtxMV, 0,
                renderer.mtxProj, 0, view, 0, modelSpaceFrom, 0);
        GLU.gluUnProject(x, getHeight() - y, 0.0f, mtxMV, 0,
                renderer.mtxProj, 0, view, 0, modelSpaceTo, 0);
        for (int i = 0; i < 3; ++i) {
            from[i] = modelSpaceFrom[i] / modelSpaceFrom[3];
            direction[i] = modelSpaceTo[i] / modelSpaceTo[3] - from[i];
        }

    }

    // find the intersection of the ray with one face of six
    // and return if the intersection is within the cube
    private boolean testRayInModel(float[] from, float[] direction, int face,
            /* out */float[/* 3 */] tuv
    /*
     * the @param tuv is used for receiving three value: t - the 'distance' to
     * reach the intersection. u, v - the u and v coordinates of the
     * intersection, both range in [0, size].
     */
    ) {
        boolean in;
        int ui, vi;
        float coord;
        if (face < 3) {// +x,+y,+z
            ui = face + 1;
            vi = face + 2;
            if (ui >= 3) ui -= 3;
            if (vi >= 3) vi -= 3;
            coord = cube.getSize() * 0.5f;
        } else {// -x,-y,-z
            face -= 3;
            ui = face + 2;
            vi = face + 1;
            if (ui >= 3) ui -= 3;
            if (vi >= 3) vi -= 3;
            coord = -cube.getSize() * 0.5f;
        }

        tuv[0] = (coord - from[face]) / direction[face];
        tuv[1] = from[ui] + direction[ui] * tuv[0];
        tuv[2] = from[vi] + direction[vi] * tuv[0];
        tuv[1] += cube.getSize() * 0.5f;
        tuv[2] += cube.getSize() * 0.5f;
        in = tuv[1] < cube.getSize() && tuv[1] > 0 &&
                tuv[2] < cube.getSize() && tuv[2] > 0;
        return in;
    }

    static private int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    // for rotating
    private float previousX, previousY;

    // for turning
    private int operatingFace = -1;
    private float operatingU, operatingV;

    private static final int MOTION_NONE = 0,
            MOTION_ROTATING = 1,
            MOTION_TURN = 2,
            MOTION_TURNING = 3;
    private int motionState = MOTION_NONE;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
        case MotionEvent.ACTION_MOVE:
            if (motionState == MOTION_ROTATING) {
                // TODO need a better rotation method
                float dx = x - previousX;
                float dy = y - previousY;
                if (Math.abs(cube.rotatePhi) > 90) dx = -dx;
                // TODO: what is the correct proportion?
                cube.rotateTheta += dx / MainRenderer.viewDistance;
                if (cube.rotateTheta > 360)
                    cube.rotateTheta -= 360;
                if (cube.rotateTheta < 0)
                    cube.rotateTheta += 360;
                cube.rotatePhi += dy / MainRenderer.viewDistance;
                if (cube.rotatePhi > 180)
                    cube.rotatePhi -= 360;
                if (cube.rotatePhi < -180)
                    cube.rotatePhi += 360;
                cube.updateModelMatrix();

            } else if (motionState == MOTION_TURN) {

                float[] tuv = new float[3];
                float[] from = new float[3], direction = new float[3];
                pointToModelSpace(x, y, from, direction);
                testRayInModel(from, direction, operatingFace, tuv);
                float threshold = cube.getSize() * 0.2f;
                if (tuv[1] - operatingU > threshold) {
                    motionState = MOTION_TURNING;
                    if (operatingFace < 3) {
                        cube.turn((operatingFace + 2) % 3, cube.selectV);
                    } else {
                        cube.turn((operatingFace + 1) % 3, cube.selectV);
                    }
                } else if (operatingU - tuv[1] > threshold) {
                    motionState = MOTION_TURNING;
                    if (operatingFace < 3) {
                        cube.turn(3 + (operatingFace + 2) % 3, cube.selectV);
                    } else {
                        cube.turn(3 + (operatingFace + 1) % 3, cube.selectV);
                    }
                } else if (tuv[2] - operatingV > threshold) {
                    motionState = MOTION_TURNING;
                    if (operatingFace < 3) {
                        cube.turn(3 + (operatingFace + 1) % 3, cube.selectU);
                    } else {
                        cube.turn(3 + (operatingFace + 2) % 3, cube.selectU);
                    }
                } else if (operatingV - tuv[2] > threshold) {
                    motionState = MOTION_TURNING;
                    if (operatingFace < 3) {
                        cube.turn((operatingFace + 1) % 3, cube.selectU);
                    } else {
                        cube.turn((operatingFace + 2) % 3, cube.selectU);
                    }
                }

                if (motionState == MOTION_TURNING) {
                    cube.selectFace = -1;
                }
            }

            break;
        case MotionEvent.ACTION_UP:
            cube.selectFace = -1;
            motionState = MOTION_NONE;
            break;
        case MotionEvent.ACTION_DOWN:
            if (cube.isTurning()) break;// workaround :don't do anything
                                        // if the cube is turning

            // test if the user hits the cubic (to turn) or not (to
            // rotate)
            float[] from = new float[3], direction = new float[3];
            pointToModelSpace(x, y, from, direction);
            operatingFace = -1;
            float t = -1000.0f;
            for (int face = 0; face < 6; ++face) {
                float[] tuv = new float[3];
                boolean in = testRayInModel(from, direction, face, tuv);
                if (in) {
                    if (t < tuv[0]) {
                        t = tuv[0];
                        operatingFace = face;
                        operatingU = tuv[1];
                        operatingV = tuv[2];
                    }
                }

            }

            if (operatingFace == -1) {
                motionState = MOTION_ROTATING;
            } else {
                cube.selectFace = operatingFace;
                cube.selectU = clamp((int) operatingU, 0, cube.getSize() - 1);
                cube.selectV = clamp((int) operatingV, 0, cube.getSize() - 1);
                motionState = MOTION_TURN;
            }

            break;
        }

        previousX = x;
        previousY = y;
        return true;
    }
}

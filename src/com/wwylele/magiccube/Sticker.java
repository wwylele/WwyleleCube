package com.wwylele.magiccube;

import java.io.*;
import java.nio.*;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class Sticker {
    private static int program;
    private static int att_vPosition, att_vNormal;
    private static int uni_stickerColor;
    private static int uni_mtxProj, uni_mtxView;
    private static int uni_mtxModel;
    private static int uni_highlight;

    static final float stickerSize = 0.45f;
    private static final float stickerCoords[] = { // in counterclockwise order:
            stickerSize, stickerSize, 0.5f, 0.0f, 0.0f, 1.0f, -stickerSize, -stickerSize, 0.5f, 0.0f, 0.0f, 1.0f,
            stickerSize, -stickerSize, 0.5f, 0.0f, 0.0f, 1.0f, stickerSize, stickerSize, 0.5f, 0.0f, 0.0f, 1.0f,
            -stickerSize, stickerSize, 0.5f, 0.0f, 0.0f, 1.0f, -stickerSize, -stickerSize, 0.5f, 0.0f, 0.0f, 1.0f, };

    private static int vertexBuffer;
    private static MainRenderer renderer;

    public static void init(Resources res, MainRenderer mainRenderer) {
        renderer = mainRenderer;
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        InputStream vsInput = res.openRawResource(R.raw.sticker_vs_glsl);
        byte[] vsB;
        try {
            vsB = new byte[vsInput.available()];
            vsInput.read(vsB);
        } catch (IOException e) {
            vsB = new byte[0];
        }

        GLES20.glShaderSource(vShader, new String(vsB));
        GLES20.glCompileShader(vShader);
        Log.d("wwy", "vShader:" + GLES20.glGetShaderInfoLog(vShader));

        InputStream fsInput = res.openRawResource(R.raw.sticker_fs_glsl);
        byte[] fsB;
        try {
            fsB = new byte[fsInput.available()];
            fsInput.read(fsB);
        } catch (IOException e) {
            fsB = new byte[0];
        }
        GLES20.glShaderSource(fShader, new String(fsB));
        GLES20.glCompileShader(fShader);
        Log.d("wwy", "fShader:" + GLES20.glGetShaderInfoLog(fShader));

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);

        att_vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        att_vNormal = GLES20.glGetAttribLocation(program, "vNormal");

        uni_stickerColor = GLES20.glGetUniformLocation(program, "stickerColor");
        uni_mtxProj = GLES20.glGetUniformLocation(program, "mtxProj");
        uni_mtxView = GLES20.glGetUniformLocation(program, "mtxView");
        uni_mtxModel = GLES20.glGetUniformLocation(program, "mtxModel");
        uni_highlight = GLES20.glGetUniformLocation(program, "highlight");

        FloatBuffer fb = FloatBuffer.allocate(stickerCoords.length);
        fb.put(stickerCoords);
        fb.position(0);
        IntBuffer ib = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1, ib);
        ib.position(0);
        vertexBuffer = ib.get();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, stickerCoords.length * 4, fb, GLES20.GL_STATIC_DRAW);
    }

    private Cube cube;
    private int face, u, v;
    public int color;
    

    public Sticker(Cube cube, int face, int u, int v) {
        this.cube = cube;
        this.face = face;
        this.u = u;
        this.v = v;
    }

    private static final float[][] paints = { { 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f },
            { 0.0f, 1.0f, 1.0f }, { 1.0f, 0.0f, 1.0f }, { 1.0f, 1.0f, 0.0f } };

    public boolean turning=false;
    void draw() {
        GLES20.glUseProgram(program);

        GLES20.glUniformMatrix4fv(Sticker.uni_mtxProj, 1, false, renderer.mtxProj, 0);
        GLES20.glUniformMatrix4fv(Sticker.uni_mtxView, 1, false, renderer.mtxView, 0);

        float[] mtxModel = new float[16], mtxModelFinal = new float[16];
        Matrix.setIdentityM(mtxModel, 0);
        if(turning){
            float axis=cube.turningAxis<3?-1.0f:1.0f;
            switch(cube.turningAxis){
            case 0:
            case 3:
                Matrix.rotateM(mtxModel, 0, cube.turningAngle, axis, 0, 0);
                break;
            case 1:
            case 4:
                Matrix.rotateM(mtxModel, 0, cube.turningAngle, 0, axis, 0);
                break;
            case 2:
            case 5:
                Matrix.rotateM(mtxModel, 0, cube.turningAngle, 0, 0, axis);
                break;
            }
        }
        float posOffset = -(cube.getSize() - 1) * 0.5f;
        Matrix.translateM(mtxModel, 0, posOffset, posOffset, posOffset);
        switch (face) {
        case 0:
            Matrix.translateM(mtxModel, 0, cube.getSize() - 1, u, v);
            Matrix.rotateM(mtxModel, 0, 90, 0, 1, 0);
            break;
        case 1:
            Matrix.translateM(mtxModel, 0, v, cube.getSize() - 1, u);
            Matrix.rotateM(mtxModel, 0, 90, -1, 0, 0);
            break;
        case 2:
            Matrix.translateM(mtxModel, 0, u, v, cube.getSize() - 1);
            break;
        case 3:
            Matrix.translateM(mtxModel, 0, 0, v, u);
            Matrix.rotateM(mtxModel, 0, 90, 0, -1, 0);
            break;
        case 4:
            Matrix.translateM(mtxModel, 0, u, 0, v);
            Matrix.rotateM(mtxModel, 0, 90, 1, 0, 0);
            break;
        case 5:
            Matrix.translateM(mtxModel, 0, v, u, 0);
            Matrix.rotateM(mtxModel, 0, 180, 1, 0, 0);
            break;
        }
        Matrix.multiplyMM(mtxModelFinal, 0, cube.mtxModel, 0, mtxModel, 0);
        GLES20.glUniformMatrix4fv(uni_mtxModel, 1, false, mtxModelFinal, 0);
        
        GLES20.glUniform1i(uni_highlight, 
                (cube.selectFace==face && 
                cube.selectU==u &&
                cube.selectV==v)?1:0);

        GLES20.glEnableVertexAttribArray(att_vPosition);
        GLES20.glEnableVertexAttribArray(att_vNormal);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer);
        GLES20.glVertexAttribPointer(att_vPosition, 3, GLES20.GL_FLOAT, false, 24, 0);
        GLES20.glVertexAttribPointer(att_vNormal, 3, GLES20.GL_FLOAT, false, 24, 12);

        GLES20.glUniform3fv(uni_stickerColor, 1, paints[color], 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(att_vPosition);
        GLES20.glDisableVertexAttribArray(att_vNormal);
    }
}

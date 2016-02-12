package com.wwylele.magiccube;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.*;
/*
 * stickers coordinates:
 * | faceId | faceDirection | u  | v  |
 * |--------|---------------|----|----|
 * | 0      | +x            | y  | z  |
 * | 1      | +y            | z  | x  |
 * | 2      | +z            | x  | y  |
 * | 3      | -x            | z  | y  |
 * | 4      | -y            | x  | z  |
 * | 5      | -z            | y  | z  |
 */

public class Cube {
    private int size;
    private Sticker[] stickers;

    public final float[] mtxModel = new float[16];
    public float rotateTheta = 30, rotatePhi = 45;

    public int selectFace = -1;
    public int selectU, selectV;

    // updateModelMatrix from rotateTheta and rotatePhi
    synchronized public void updateModelMatrix() {
        Matrix.setIdentityM(mtxModel, 0);
        Matrix.scaleM(mtxModel, 0, 1.0f / size, 1.0f / size, 1.0f / size);
        Matrix.rotateM(mtxModel, 0, rotatePhi, 0, 1, 0);
        Matrix.rotateM(mtxModel, 0, rotateTheta, 0, 0, 1);
    }

    public Sticker getSticker(int face, int u, int v) {
        return stickers[(face * size + u) * size + v];
    }

    public Cube(int size) {
        Log.d("wwy", "cons");
        this.size = size;
        stickers = new Sticker[6 * size * size];

        for (int face = 0; face < 6; ++face) {
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    stickers[(face * size + u) * size + v] = new Sticker(this, face, u, v);
                    stickers[(face * size + u) * size + v].color = face;// TODO
                }
            }
        }

        updateModelMatrix();
        lastFrameTime = SystemClock.elapsedRealtime();
    }
    
    public byte[] serialize(){
        Log.d("wwy", "ser");
        ByteArrayOutputStream ostream=new ByteArrayOutputStream();
        DataOutputStream dostream=new DataOutputStream(ostream);
        try{
            dostream.writeInt(size);
            for (int face = 0; face < 6; ++face) {
                for (int u = 0; u < size; ++u) {
                    for (int v = 0; v < size; ++v) {
                        dostream.writeInt(getSticker(face,u,v).color);
                    }
                }
            }
            dostream.writeFloat(rotateTheta);
            dostream.writeFloat(rotatePhi);
            dostream.close();
        }catch(IOException e){
            return null;
        }
        return ostream.toByteArray();
    }
    
    public void deserialize(byte[] data){
        if(data==null)return;
        Log.d("wwy", "des");
        ByteArrayInputStream istream=new ByteArrayInputStream(data);
        DataInputStream distream=new DataInputStream(istream);
        try{
            size=distream.readInt();
            stickers = new Sticker[6 * size * size];
            for (int face = 0; face < 6; ++face) {
                for (int u = 0; u < size; ++u) {
                    for (int v = 0; v < size; ++v) {
                        stickers[(face * size + u) * size + v] = new Sticker(this, face, u, v);
                        stickers[(face * size + u) * size + v].color = distream.readInt();
                    }
                }
            }
            rotateTheta=distream.readFloat();
            rotatePhi=distream.readFloat();
            distream.close();
        }catch(IOException e){
            // TODO
        }
        updateModelMatrix();
        selectFace=-1;
        turning=false;
        turningAngle=0;
    }

    public int getSize() {
        return size;
    }

    private long lastFrameTime;

    synchronized public void draw() {
        long currentTime = SystemClock.elapsedRealtime();
        long deltaTime = currentTime - lastFrameTime;
        lastFrameTime = currentTime;

        // update turning angle
        if (turning) {
            turningAngle -= deltaTime * 0.35f;
            if (turningAngle < 0) {
                // turning finished, remove the mark of this and all stickers
                turningAngle = 0.0f;
                turning = false;
                for (int face = 0; face < 6; ++face) {
                    for (int u = 0; u < size; ++u) {
                        for (int v = 0; v < size; ++v) {
                            getSticker(face, u, v).turning = false;
                        }
                    }
                }
            }
        }

        // draw all stickers
        for (int face = 0; face < 6; ++face) {
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    getSticker(face, u, v).draw();
                }
            }
        }
    }

    private float turningAngle;

    public float getTurningAngle() {
        return turningAngle;
    }

    private boolean turning = false;

    public boolean isTurning() {
        return turning;
    }

    private int turningAxis, turningLevel;

    public int getTurningAxis() {
        return turningAxis;
    }

    public int getTurningLevel() {
        return turningLevel;
    }

    // begin a turning
    public void turn(int orientation, int level) {
        if (turning) return;
        turning = true;
        turningAxis = orientation;
        turningLevel = level;
        turningAngle = 90.0f;
        int absAxis = turningAxis % 3;

        // if we do the turning on the bottom or top level
        // we should also turn the stickers on that face
        if (turningLevel == 0) {
            int[] t = new int[size * size];
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a = getSticker(absAxis + 3, u, v);
                    a.turning = true;// mark the sticker to turn
                    t[u * size + v] = a.color;// store the sticker color for
                                              // data turning;
                }
            }
            // do data turning
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a = getSticker(absAxis + 3, u, v);
                    if (turningAxis < 3) {
                        a.color = t[(size - 1 - v) * size + u];
                    } else {
                        a.color = t[v * size + (size - 1 - u)];
                    }
                }
            }
        } else if (turningLevel == size - 1) {
            int[] t = new int[size * size];
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a = getSticker(absAxis, u, v);
                    a.turning = true;// mark the sticker to turn
                    t[u * size + v] = a.color;// store the sticker color for
                                              // data turning;
                }
            }
            // do data turning
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a = getSticker(absAxis, u, v);
                    if (turningAxis >= 3) {
                        a.color = t[(size - 1 - v) * size + u];
                    } else {
                        a.color = t[v * size + (size - 1 - u)];
                    }
                }
            }
        }

        // do turning for the "ring"
        for (int j = 0; j < size; ++j) {
            Sticker a, b, c, d;
            int t;
            a = getSticker((absAxis + 1) % 3, j, turningLevel);
            b = getSticker((absAxis + 2) % 3, turningLevel, size - 1 - j);
            c = getSticker((absAxis + 1) % 3 + 3, turningLevel, size - 1 - j);
            d = getSticker((absAxis + 2) % 3 + 3, j, turningLevel);
            // do data turning
            if (turningAxis < 3) {
                t = d.color;
                d.color = c.color;
                c.color = b.color;
                b.color = a.color;
                a.color = t;
            } else {
                t = a.color;
                a.color = b.color;
                b.color = c.color;
                c.color = d.color;
                d.color = t;
            }
            // mark these stickers to turn
            a.turning = true;
            b.turning = true;
            c.turning = true;
            d.turning = true;
        }
    }
}

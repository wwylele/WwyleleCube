package com.wwylele.magiccube;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
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
    //public final float[] invModel = new float[16];
    public float rotateTheta = 30, rotatePhi = 45;
    
    public int selectFace=-1;
    public int selectU,selectV;
    
    
    synchronized public void updateModelMatrix() {
        Matrix.setIdentityM(mtxModel, 0);
        Matrix.scaleM(mtxModel, 0, 1.0f / size, 1.0f / size, 1.0f / size);
        Matrix.rotateM(mtxModel, 0, rotatePhi, 0, 1, 0);
        Matrix.rotateM(mtxModel, 0, rotateTheta, 0, 0, -1);
        //Matrix.invertM(invModel, 0, mtxModel, 0);
    }

    public Sticker getSticker(int face, int u, int v) {
        return stickers[(face * size + u) * size + v];
    }

    public Cube(int size) {
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
        lastFrameTime=SystemClock.elapsedRealtime();
    }

    public int getSize() {
        return size;
    }

    private long lastFrameTime;
    synchronized public void draw() {
        long currentTime = SystemClock.elapsedRealtime();
        long deltaTime = currentTime - lastFrameTime;
        lastFrameTime = currentTime;
        if(turning){
            turningAngle-=deltaTime*0.35f;
            if(turningAngle<0){
                turningAngle=0.0f;
                turning=false;
                for (int face = 0; face < 6; ++face) {
                    for (int u = 0; u < size; ++u) {
                        for (int v = 0; v < size; ++v) {
                            getSticker(face, u, v).turning=false;
                        }
                    }
                }
                Log.d("wwy","turing finishied");
            }
        }
        for (int face = 0; face < 6; ++face) {
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    getSticker(face, u, v).draw();
                }
            }
        }
    }
    
    public float turningAngle;
    private boolean turning=false;
    public int turningAxis, turningLevel;
    public boolean isTurning(){
        return turning;
    }
    
    public void turn(int orientation, int level){
        if(turning)return;
        turning=true;
        turningAxis=orientation;
        turningLevel=level;
        turningAngle=90.0f;
        int absAxis=turningAxis%3;
        if(turningLevel==0){
            int[] t=new int[size*size];
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a=getSticker(absAxis+3, u, v);
                    a.turning=true;
                    t[u*size+v]=a.color;
                }
            }
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a=getSticker(absAxis+3, u, v);
                    if(turningAxis<3){
                        a.color=t[(size-1-v)*size+u];
                    }else{
                        a.color=t[v*size+(size-1-u)];
                    }
                }
            }
        }else if(turningLevel==size-1){
            int[] t=new int[size*size];
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a=getSticker(absAxis, u, v);
                    a.turning=true;
                    t[u*size+v]=a.color;
                }
            }
            for (int u = 0; u < size; ++u) {
                for (int v = 0; v < size; ++v) {
                    Sticker a=getSticker(absAxis, u, v);
                    if(turningAxis>=3){
                        a.color=t[(size-1-v)*size+u];
                    }else{
                        a.color=t[v*size+(size-1-u)];
                    }
                }
            }
        }
        
        for(int j=0;j<size;++j){
            Sticker a,b,c,d;
            int t;
            a=getSticker((absAxis+1)%3, j, turningLevel);
            b=getSticker((absAxis+2)%3, turningLevel, size-1-j);
            c=getSticker((absAxis+1)%3+3, turningLevel, size-1-j);
            d=getSticker((absAxis+2)%3+3, j, turningLevel);
            if(turningAxis<3){
                t=d.color;
                d.color=c.color;
                c.color=b.color;
                b.color=a.color;
                a.color=t;
            }else{
                t=a.color;
                a.color=b.color;
                b.color=c.color;
                c.color=d.color;
                d.color=t;
            }
            a.turning=true;
            b.turning=true;
            c.turning=true;
            d.turning=true;
        }
    }
}

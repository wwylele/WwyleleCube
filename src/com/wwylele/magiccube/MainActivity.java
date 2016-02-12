package com.wwylele.magiccube;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    public final Cube cube = new Cube(3);// TODO;
    public CubeView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view=new CubeView(this);
        getActionBar().hide();

        setContentView(view);
    }
}

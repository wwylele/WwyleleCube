package com.wwylele.magiccube;



import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;


public class MainActivity extends AppCompatActivity {

    public final Cube cube = new Cube(3);// TODO;
    public CubeView cubeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Toolbar toolbar=new Toolbar(this);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(0xFF000000);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.menu));
        CubeView cubeView=new CubeView(this);
        
        LinearLayout mainView=new LinearLayout(this);
        mainView.setOrientation(LinearLayout.VERTICAL);
        
        mainView.addView(toolbar);
        mainView.addView(cubeView);
        
        setContentView(mainView);
        if (savedInstanceState != null) {
            cube.deserialize(savedInstanceState.getByteArray("cube"));
        } else {
            //cube.shuffle();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        
        savedInstanceState.putByteArray("cube",cube.serialize());

        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scramble:
                cube.scramble();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}

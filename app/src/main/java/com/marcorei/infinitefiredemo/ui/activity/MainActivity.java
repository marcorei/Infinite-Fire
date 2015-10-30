package com.marcorei.infinitefiredemo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.marcorei.infinitefiredemo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonLinearDemo = (Button) findViewById(R.id.button_linear);
        Button buttonGridDemo = (Button) findViewById(R.id.button_grid);

        buttonLinearDemo.setOnClickListener(this);
        buttonGridDemo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch(v.getId()) {
            case R.id.button_linear:
                i = new Intent(this, InfiniteRecyclerViewLinearActivity.class);
                break;
            case R.id.button_grid:
                i = new Intent(this, InfiniteRecyclerViewGridActivity.class);
                break;
            default:
                return;
        }
        startActivity(i);
    }
}

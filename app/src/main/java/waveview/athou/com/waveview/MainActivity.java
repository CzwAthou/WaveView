package waveview.athou.com.waveview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Administrator on 2017/6/7.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OnClickWaveWithBoat(View view) {
        startActivity(new Intent(this, WaveActivity.class));
    }

    public void OnClickBoatMove(View view) {
        startActivity(new Intent(this, BoatMoveActivity.class));
    }
}

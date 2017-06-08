package waveview.athou.com.waveview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

public class WaveActivity extends AppCompatActivity {

    WaveView waveView;

    SeekBar rizeSb;
    SeekBar lengthSb;
    SeekBar heightSb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave);

        waveView = (WaveView) findViewById(R.id.waveView);

        rizeSb = (SeekBar) findViewById(R.id.rize_sb);
        lengthSb = (SeekBar) findViewById(R.id.length_sb);
        heightSb = (SeekBar) findViewById(R.id.height_sb);

        rizeSb.setOnSeekBarChangeListener(listener);
        lengthSb.setOnSeekBarChangeListener(listener);
        heightSb.setOnSeekBarChangeListener(listener);

        rizeSb.setMax(10);
        rizeSb.setProgress(0);

        lengthSb.setMax(1000);
        lengthSb.setProgress(400);

        heightSb.setMax(300);
        heightSb.setProgress(80);

        waveView.setRise(rizeSb.getProgress());
        waveView.setWaveLength(lengthSb.getProgress());
        waveView.setWaveHeight(heightSb.getProgress());
    }

    private SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (seekBar == rizeSb) {
                    waveView.setRise(progress);
                } else if (seekBar == lengthSb) {
                    waveView.setWaveLength(progress);
                } else if (seekBar == heightSb) {
                    waveView.setWaveHeight(progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}

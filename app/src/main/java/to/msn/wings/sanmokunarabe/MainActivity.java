package to.msn.wings.sanmokunarabe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        soundIcon = findViewById(R.id.MainBtnBGM);
        isMute = false;
        soundIcon.setImageResource(R.drawable.volumeoffimage);
        bgm.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            isMute = data.getBooleanExtra("isMute", true);
            soundIcon = findViewById(R.id.MainBtnBGM);
            bgmManager_changePage(isMute);
        }
    }

    public void btnBGM_onClick(View view) {
        bgmManager_onClick(0);
    }


    public void btn_click(View view) {
        int _mode = 0;
        switch (view.getId()) {
            case R.id.btnManVsMan:
                _mode = 0;
                break;
            case R.id.btnManVsNomalComp:
                _mode = 1;
                break;
            case R.id.btnManVsStrongComp:
                _mode = 2;
                break;
        }
        Intent intent = new Intent(this,
                to.msn.wings.sanmokunarabe.GameActivity.class);
        intent.putExtra("_mode", _mode);
        intent.putExtra("isMute", isMute);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bgm.release();
    }

}
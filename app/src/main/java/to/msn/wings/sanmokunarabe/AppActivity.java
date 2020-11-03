package to.msn.wings.sanmokunarabe;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class AppActivity extends Activity {
    static protected AudioManager am;
    static protected MediaPlayer bgm;       // BGM用フィールド宣言
    static protected MediaPlayer[] mpResult;// 勝敗音声用フィールド宣言
    static protected boolean isMute;        // 音声ON,OFF判定フィールド宣言
    static protected ImageButton soundIcon; // 音声アイコンフィールド宣言

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (bgm == null) {
            bgm = MediaPlayer.create(this,
                    R.raw.bgm);
        }
        if (mpResult == null) {
            mpResult = new MediaPlayer[] {MediaPlayer.create(this,R.raw.draw),
                    MediaPlayer.create(this,R.raw.win),
                    MediaPlayer.create(this,R.raw.lose)};
        }
        bgm.setLooping(true);
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
    }

    // ボタンクリック時のBGM設定
    public void bgmManager_onClick(int i) {
        // 0ならメインページのボタンビュー取得、1ならゲームページのボタンビュー取得
        switch (i) {
            case 0:
                soundIcon = findViewById(R.id.MainBtnBGM);
                break;
            case 1:
                soundIcon = findViewById(R.id.GameBtnBGM);
                break;
        }
        // 音声ON
        if ( isMute ) {
            isMute = false;
            soundOn();
            soundIcon.setImageResource(R.drawable.volumeoffimage);
        // 音声OFF
        }else {
            isMute = true;
            soundOff();
            soundIcon.setImageResource(R.drawable.volumeonimage);

        }
    }

    // 画面遷移時の音声設定
    public void bgmManager_changePage(Boolean isMute) {
        // 音声OFF
        if ( isMute ) {
            soundOff();
            soundIcon.setImageResource(R.drawable.volumeonimage);
        // 音声ON
        }else {
            soundOn();
            soundIcon.setImageResource(R.drawable.volumeoffimage);
        }
    }

    // 音声ON
    public void soundOn() {
        if ( Build.VERSION.SDK_INT >= 23 ) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        }else {
            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }

    }

    // 音声OFF
    public void soundOff() {
        if ( Build.VERSION.SDK_INT >= 23 ) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        }else {
            am.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

}

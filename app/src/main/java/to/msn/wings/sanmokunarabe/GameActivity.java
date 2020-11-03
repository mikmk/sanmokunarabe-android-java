package to.msn.wings.sanmokunarabe;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class GameActivity extends AppActivity {
    Intent intent;         // インテントフィールド宣言

    private static final int FIRST_MOVER = 1;       // 先手
    private static final int PASSIVE_MOVER = -1;    // 後手
    private static final int BLANK = 0;             // 空きマス
    private static final int MAN_VS_MAN = 0;        // 人間対人間
    private static final int MAN_VS_NORMALCOMPUTER = 1;   // 人間対通常コンピュータ
    private static final int MAN_VS_STRONGCOMPUTER = 2;   // 人間対最強コンピュータ
    private static final String MARK_FIRST_MOVER = "〇";     // 先手の石マーク
    private static final String MARK_PASSIVE_MOVER = "×";   // 後手の石マーク
    private static final String MARK_BLANK = "";            // 空き（マークが置かれていない）
    private int _mode = MAN_VS_MAN;     // 対戦モード（0：人間対人間、1：人間対コンピュータ）
    private int _turn = FIRST_MOVER;    // 手番（1：先手、-1：後手）
    private int _moves = 0;             // 手数９になったら終了

    private int[] _bd = {       // 盤面の状態（0：空き、1：先手、-1：後手）
            0,0,0,
            0,0,0,
            0,0,0
    };

    private int[] _vId = {      // ボタンのidの値を配列で管理する
            R.id.bt0, R.id.bt1, R.id.bt2,
            R.id.bt3, R.id.bt4, R.id.bt5,
            R.id.bt6, R.id.bt7, R.id.bt8,
    };

    private int[][] _winPtn = { // 盤面の勝ちパターン
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6}
    };

    private String[] txtModeAry = { "２人対戦モード", "コンピュータ対戦(ふつう)", "コンピュータ対戦(つよい)" };

    // コンピュータが優先して打つ手を戦略的に考えるための配列
    private int[][] _compStrategy = {
            {4},                        // 真ん中が空の場合
            {0, 2, 6, 8}                // 四隅が空の場合
    };

    private TextView _tv;   // 案内欄のビュー部品

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        soundIcon = findViewById(R.id.GameBtnBGM);

        // インテントを取得、データを挿入
        intent = getIntent();
        _mode = intent.getIntExtra("_mode", 0);
        isMute = intent.getBooleanExtra("isMute", true);

        bgmManager_changePage(isMute);      // BGM設定
        _tv = findViewById(R.id.tvGuide);   // 案内欄のビュー取得
        initBoard(0);   // 盤面一括設定（ゲーム開始）
    }



    /** メニュー処理 **/
    // メニュー定義ファイルをもとにオプションメニューを生成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // メニュー選択時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // [やりなおし]ボタンが押された時
            case R.id.gameReset:
                initBoard(0);
                break;
            // [トップ画面に戻る]ボタンが押された時
            case R.id.backToTop:
                intent = new Intent();
                intent.putExtra("isMute", isMute);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    /** 端末で戻るボタンが押された時 **/
    @Override
    public void onBackPressed(){
        intent = new Intent();
        intent.putExtra("isMute", isMute);
        setResult(RESULT_OK, intent);
        finish();
    }

    /** BGM処理 **/
    public void btnBGM_onClick(View view) {
        bgmManager_onClick(1);
    }

    /** ゲーム処理 **/
    public void btClick(View view) {
        int id = view.getId();      // 押されたボタンのビュー取得
        int no = findBoadNumber(id);// ボタンのマス目を取得
        if(_bd[no] != BLANK) {
            Toast.makeText(this, "そこは置けません", Toast.LENGTH_LONG).show();
        } else {
            switch (_mode) {
                case MAN_VS_MAN:
                    manVsMan(id, no);
                    break;
                case MAN_VS_NORMALCOMPUTER:
                case MAN_VS_STRONGCOMPUTER:
                    manVsComputer(id, no);
                    judge();
                    break;
            }
        }
    }

    private void manVsMan(int id, int no) {
        // 先手処理（ボタンクリック）(〇をマス目に表示＆後手に打たせる準備)
        if(_turn == FIRST_MOVER) {
            _bd[no] = FIRST_MOVER;
            Button touchBtn =findViewById(id);
            touchBtn.setText(MARK_FIRST_MOVER);
            _tv.setText(MARK_PASSIVE_MOVER + "の手番です。");   // 開始時の案内欄表示
            // 後手処理（ボタンクリック）(×をマス目に表示＆後手に打たせる準備）
        } else if(_turn == PASSIVE_MOVER) {
            _bd[no] = PASSIVE_MOVER;
            Button touchBtn =findViewById(id);
            touchBtn.setText(MARK_PASSIVE_MOVER);
            _tv.setText(MARK_FIRST_MOVER + "の手番です。");   // 開始時の案内欄表示
        }
        // 共通処理（手数カウント＆勝敗判定）
        _moves++;
        judge();
    }

    private void manVsComputer(int id, int no) {
        // 先手処理（ボタンクリック）(〇をマス目に表示＆後手に打たせる準備＆手数カウント＆勝敗判定)
        _bd[no] = FIRST_MOVER;
        Button touchBtn =findViewById(id);
        touchBtn.setText(MARK_FIRST_MOVER);
        _moves++;
        judge();
        // 勝負がまだついていなかったら後手処理（コンピュータ）
        // (打つ手を決める＆×をマス目に表示＆先手に打たせる準備＆手数カウント＆勝敗判定)
        if(_moves < 9 && !judge()) {
            int tmp = 0;
            Random random = new Random();
            int compIndex;
            if(findToWin()) {                       // あと一手でコンピュータが勝つ手を打つ
                for (int[] p : _winPtn) {
                    if ((_bd[p[0]] + _bd[p[1]] + _bd[p[2]]) == -2) {
                        if(_bd[p[0]] == BLANK) {
                            tmp = p[0];
                        } else if (_bd[p[1]] == BLANK) {
                            tmp = p[1];
                        } else if (_bd[p[2]] == BLANK) {
                            tmp = p[2];
                        }
                        _bd[tmp] = PASSIVE_MOVER;
                        touchBtn = findViewById(_vId[tmp]);
                        break;
                    }
                }
            } else if(findToBlock()) {              // あと一手で人間が勝つ手を阻止する
                for (int[] p : _winPtn) {
                    if ((_bd[p[0]] + _bd[p[1]] + _bd[p[2]]) == 2) {
                        if(_bd[p[0]] == BLANK) {
                            tmp = p[0];
                        } else if (_bd[p[1]] == BLANK) {
                            tmp = p[1];
                        } else if (_bd[p[2]] == BLANK) {
                            tmp = p[2];
                        }
                        _bd[tmp] = PASSIVE_MOVER;
                        touchBtn = findViewById(_vId[tmp]);
                        break;
                    }
                }
            } else if(checkStrong()) {              // 最強モードの場合、特殊パターンを優先して打つ
                _bd[3] = PASSIVE_MOVER;
                touchBtn = findViewById(_vId[3]);
            } else if(_bd[_compStrategy[0][0]] == BLANK) {      // 真ん中が空いていたら優先して打つ
                _bd[_compStrategy[0][0]] = PASSIVE_MOVER;
                touchBtn = findViewById(_vId[_compStrategy[0][0]]);
            } else if(checkCorners()) {                         // 四隅が空いていたら優先して打つ
                for(int i = 0; i < _compStrategy[1].length; i++) {
                    if(_bd[_compStrategy[1][i]] == BLANK) {
                        _bd[_compStrategy[1][i]] = PASSIVE_MOVER;
                        tmp = i;
                        break;
                    }
                }
                touchBtn = findViewById(_vId[_compStrategy[1][tmp]]);
            } else {                                            // 有効な手がない場合
                do {
                    compIndex = random.nextInt(9);
                } while(_bd[compIndex] != BLANK);
                _bd[compIndex] = PASSIVE_MOVER;
                touchBtn = findViewById(_vId[compIndex]);
            }
            touchBtn.setText(MARK_PASSIVE_MOVER);
            _moves++;
            judge();
        }
    }

    private boolean findToWin() {
        for (int[] p : _winPtn) {
            if ((_bd[p[0]] + _bd[p[1]] + _bd[p[2]]) == -2) { return true; }
        }
        return false;
    }

    private boolean findToBlock() {
        for (int[] p : _winPtn) {
            if ((_bd[p[0]] + _bd[p[1]] + _bd[p[2]]) == 2) { return true; }
        }
        return false;
    }

    private boolean checkStrong() {
        if(_moves == 3 && _bd[4] == PASSIVE_MOVER && _mode == MAN_VS_STRONGCOMPUTER) {
            return true;
        }
        return false;
    }

    private boolean checkCorners() {
        for(int i = 0; i < _compStrategy[1].length; i++) {
            if(_bd[_compStrategy[1][i]] == BLANK) { return true; }
        }
        return false;
    }

    private void turnChange() {
        _turn = (_turn == FIRST_MOVER) ? PASSIVE_MOVER : FIRST_MOVER;
    }

    private String turnMark(int turn) {
        if(turn == PASSIVE_MOVER) {
            return MARK_PASSIVE_MOVER;
        } else {
            return MARK_FIRST_MOVER;
        }
    }

    private boolean judge() {
        // どちらかが勝った時
        for (int[] p : _winPtn) {
            if ((_bd[p[0]] + _bd[p[1]] + _bd[p[2]]) == _turn * 3) {
                String winMark = turnMark(_turn);
                _tv.setText( winMark + "の勝ちです。");
                initBoard(1);
                // モード別に勝敗音声を再生
                if(_mode != MAN_VS_MAN && _turn == PASSIVE_MOVER ) {
                    mpResult[2].start();
                } else {
                    mpResult[1].start();
                }
                return true;
            }
        }
        // 引き分けの時
        if(_moves >= 9) {
            _tv.setText("引き分けです。");
            // 引き分け音声を再生
            mpResult[0].start();
            initBoard(1);
            return true;
        }
        // 勝敗が決まらず試合続行の時
        turnChange();
        return false;
    }

    // 押したボードのインデックス番号を取得
    private int findBoadNumber(int id) {
        for(int i = 0; i < 9; i++) {
            if (id == _vId[i]) {
                return i;
            }
        }
        return 0;
    }

    private void setTxtMode() {
        TextView txtMode = findViewById(R.id.txtMode);
        switch (_mode) {
            case 0:
                txtMode.setText(txtModeAry[0]);
                break;
            case 1:
                txtMode.setText(txtModeAry[1]);
                break;
            case 2:
                txtMode.setText(txtModeAry[2]);
                break;
        }
    }

    private void initBoard(int i) {
        switch (i) {
            // リセット処理
            case 0:
                _bd = new int[9];                               // 盤面のリセット
                _moves = 0;                                     // 手数のリセット
                _turn = FIRST_MOVER;                            // 先手の石マークリセット
                for (int j = 0; j < _vId.length; j++) {         // 全部のマス目を押せるようにする
                    findViewById(_vId[j]).setEnabled(true);
                }
                for (int j = 0; j < _vId.length; j++) {         // 盤面のマークをクリア
                    Button btn = findViewById(_vId[j]);
                    btn.setText(MARK_BLANK);
                }
                _tv.setText(MARK_FIRST_MOVER + "の手番です。");   // 開始時の案内欄表示
                setTxtMode();       // txtModeに今何大戦中なのか表示
                break;
            // 勝敗確定後にマス目をEnabledで固定
            case 1:
                for (int j = 0; j < _vId.length; j++) {         // 全部のマス目を押せなくする
                    findViewById(_vId[j]).setEnabled(false);
                }
                break;
        }
    }

}
package com.example.han.bulletscreendemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.io.File;
import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean showDanmaku;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final VideoView videoView = (VideoView) findViewById(R.id.video_view);

        ///storage/emulated/0/Download/2.mp4
        if (new File("/sdcard/Download/2.mp4").exists()
                ) {
            Log.d("tag", "========aa存在");
            videoView.setVideoPath(Environment
                    .getExternalStorageDirectory() + "/Download/01.mp4");
        }//sdcard/storage//Download/


        Log.d("tag", "========" + Environment.getRootDirectory());
        Log.d("tag", "========" + Environment
                .getExternalStorageDirectory());
        Log.d("tag", "== ======" + Environment.getDownloadCacheDirectory());
        Log.d("tag", "========" + new File("/storage/emulated/0/Download/01.mp4").getTotalSpace());
        // videoView.seekTo(30000);
        videoView.start();

      /* new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                videoView.pause();
            }
        }, 0);*/
        /*
        * 弹幕：一个自定义view 上面显示类似于跑马灯的文字效果，发过的评论都会在上面显示但很快地移除屏幕
        * 既可以起到互动作用同时又不影响视频的正常观看 可以自定义一个view这里用的用的开源库
        * */
        danmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        danmakuView.enableDanmakuDrawingCache(true);//提升绘制效率
        danmakuView.setCallback(new DrawHandler.Callback() {//设置回调函数
            @Override
            public void prepared() {
                showDanmaku = true;
                danmakuView.start();//DanmukuView开始正常工作
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuContext = DanmakuContext.create();//可以用于对弹幕的各种全局配置进行设定如设置字体 最大显示行数
        danmakuView.prepare(parser, danmakuContext);
        //准备完成后会自动调用刚才设置的回调函数中的prepared方法
        //这里弹幕的解析器是全局的
        final LinearLayout operationLayout = (LinearLayout) findViewById(R.id.operation_layout);
        final Button send = (Button) findViewById(R.id.send);
        final EditText editText = (EditText) findViewById(R.id.edit_text);

        danmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (operationLayout.getVisibility() == View.GONE) {
                    operationLayout.setVisibility(View.VISIBLE);

                } else {

                    operationLayout.setVisibility(View.GONE);
                }
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editText.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    addDanmaku(content, true);
                    editText.setText("");
                }
            }
        });
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });

    }

    /*
    * 向弹幕view中添加一条弹幕
    * content
    * 弹幕的具体内容
    * withBorder
    * 弹幕是否有边框
    * */
    private void addDanmaku(String content, boolean withBoder) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku
                .TYPE_SCROLL_RL);//创建BaseDanmuku实例 参数后者表示这是一条从右向左滚动的弹幕
        //对弹幕的内容字体大小颜色显示时间进行配置
        //是否带边框用于自己和他人区分
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(danmakuView.getCurrentTime());
        if (withBoder) {
            danmaku.borderColor = Color.GREEN;
        }
        danmakuView.addDanmaku(danmaku);

    }

    /*
    * 随机生成一些弹幕内容以供测试*/
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (showDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "" + time + time;
                    addDanmaku(content, false);//添加弹幕功能更
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused())
            ;
        danmakuView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku = false;
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }
    //由于系统输入法弹出的时候会导致焦点丢失从而退出沉浸模式，这里对系统全局的UI变化进行了监听保证程序一直处于沉浸模式

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        }
    }
}

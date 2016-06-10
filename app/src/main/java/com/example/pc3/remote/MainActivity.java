package com.example.pc3.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioManager am;
    private int volume;
    private Button media_play;
    private TextView txt_artist, txt_track, txt_volume;

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";

    IntentFilter iF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");
        iF.addAction("com.soundcloud.android.metachanged");

        registerReceiver(mReceiver, iF);
        Log.d("onDestory()", "브로드캐스트리시버 탑재됨");

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        findViewById(R.id.default_back).setOnClickListener(this);
        findViewById(R.id.default_home).setOnClickListener(this);
        findViewById(R.id.default_menu).setOnClickListener(this);
        findViewById(R.id.media_volup).setOnClickListener(this);
        findViewById(R.id.media_voldown).setOnClickListener(this);
        findViewById(R.id.media_play).setOnClickListener(this);
        findViewById(R.id.media_pre).setOnClickListener(this);
        findViewById(R.id.media_next).setOnClickListener(this);

        media_play = (Button) findViewById(R.id.media_play);

        txt_artist = (TextView) findViewById(R.id.txt_media_artist);
        txt_track = (TextView) findViewById(R.id.txt_media_track);
        registerReceiver(mReceiver, iF);

        txt_volume = (TextView) findViewById(R.id.txt_media_volume);
        txt_volume.setText("Audio Volume : " + am.getStreamVolume(AudioManager.STREAM_MUSIC) + " / " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.d("onDestory()", "브로드캐스트리시버 해제됨");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String sTrack = intent.getStringExtra("track");
            String sArtist = intent.getStringExtra("artist");

            txt_artist.setText("Artist : " + sArtist);
            txt_track.setText("Track : " + sTrack);
        }
    };
    //http://stackoverflow.com/questions/10510292/track-info-of-currently-playing-music/11929681#11929681
    //https://hashcode.co.kr/questions/2070/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%8B%A4%EB%A5%B8-%EC%95%B1%EC%97%90%EC%84%9C-%EC%9E%AC%EC%83%9D-%EC%A4%91%EC%9D%B8-%EC%9D%8C%EC%95%85%EC%9D%98-%ED%8C%8C%EC%9D%BC-%EA%B2%BD%EB%A1%9C%EB%A5%BC-%EA%B0%80%EC%A0%B8-%EC%98%AC-%EC%88%98-%EC%9E%88%EB%8A%94-%EB%B0%A9%EB%B2%95%EC%9D%B4-%EC%9E%88%EC%9D%84%EA%B9%8C%EC%9A%94


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.default_back:
                onBackPressed();
                break;

            case R.id.default_home:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_FORWARD_RESULT
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
                break;

            case R.id.default_menu:
                openOptionsMenu();
                break;

            case R.id.media_volup:
                // 현재 볼륨 가져오기
                volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                // volume이 최대 볼륨보다 작을 때만 키우기 동작
                if (volume < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, volume + 1, AudioManager.FLAG_PLAY_SOUND);
                    txt_volume.setText("Audio Volume : " + volume + " / " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                } else {
                    Toast.makeText(getApplicationContext(), "현재 최고음량입니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.media_voldown:
                // 현재 볼륨 가져오기
                volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                // volume이 0보다 클 때만 키우기 동작
                if (volume > 0) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, volume - 1, AudioManager.FLAG_PLAY_SOUND);
                    txt_volume.setText("Audio Volume : " + volume + " / " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                } else {
                    Toast.makeText(getApplicationContext(), "현재 최저음량입니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.media_play:

                try {
                    if (am.isMusicActive())
                        media_play.setText("■");
                    else
                        media_play.setText("▶");
                    Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                    registerReceiver(mReceiver, iF);
                    getBaseContext().sendOrderedBroadcast(buttonDown, null);
                    buttonDown = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //      KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE : 재생/일시정지 토글
                //      KeyEvent.KEYCODE_MEDIA_PLAY : 재생
                //      KeyEvent.KEYCODE_MEDIA_PAUSE : 일시정지
                //      KeyEvent.KEYCODE_MEDIA_NEXT : 다음 곡
                //      KeyEvent.KEYCODE_MEDIA_PREVIOUS : 이전 곡
                //      KeyEvent.KEYCODE_MEDIA_STOP : 정지
                //      http://susemi99.tistory.com/1290

                break;

            case R.id.media_pre:
                try {
                    Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                    registerReceiver(mReceiver, iF);
                    getBaseContext().sendOrderedBroadcast(buttonDown, null);
                    buttonDown = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.media_next:

                try {
                    Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
                    registerReceiver(mReceiver, iF);
                    getBaseContext().sendOrderedBroadcast(buttonDown, null);
                    buttonDown = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

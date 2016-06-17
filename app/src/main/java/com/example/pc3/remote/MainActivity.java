package com.example.pc3.remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


//      KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE : 재생/일시정지 토글
//      KeyEvent.KEYCODE_MEDIA_PLAY : 재생
//      KeyEvent.KEYCODE_MEDIA_PAUSE : 일시정지
//      KeyEvent.KEYCODE_MEDIA_NEXT : 다음 곡
//      KeyEvent.KEYCODE_MEDIA_PREVIOUS : 이전 곡
//      KeyEvent.KEYCODE_MEDIA_STOP : 정지
//      http://susemi99.tistory.com/1290

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioManager am;
    private int volume;
    private Button media_play;
    private TextView txt_artist, txt_track, txt_volume;
    private String sTrack;
    private String sArtist;
    private boolean bool_track_changed;

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";

    IntentFilter iF;

    //메시지 정수
    public static final int MSG_STATE_CHANGE = 1;
    public static final int MSG_READ = 2;

    // 요청 정수
    private static final int RQ_CONNECT_DEVICE = 1;
    private static final int RQ_ENABLE_BT = 2;

    // Bluetooth
    private BluetoothAdapter btAdapter;
    private BluetoothChatService chatService;

    // UI
    private TextView txt_received; //수신 라벨
    private EditText edtSend; //송신 텍스트 박스
    private Button btnSend; //송신 버튼

    private static String ALBUM_ART_URL;
    private static Bitmap ALBUM_ART;

    // 수신된 메시지
    private String received_message;

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
        iF.addAction("com.soundcloud.android");

        registerReceiver(mReceiver, iF);
        Log.d("onDestory()", "브로드캐스트리시버 탑재됨");

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

//        findViewById(R.id.default_back).setOnClickListener(this);
//        findViewById(R.id.default_home).setOnClickListener(this);
//        findViewById(R.id.default_menu).setOnClickListener(this);
        findViewById(R.id.media_volup).setOnClickListener(this);
        findViewById(R.id.media_voldown).setOnClickListener(this);
        findViewById(R.id.media_play).setOnClickListener(this);
        findViewById(R.id.media_pre).setOnClickListener(this);
        findViewById(R.id.media_next).setOnClickListener(this);
        findViewById(R.id.button_connect).setOnClickListener(this);

        media_play = (Button) findViewById(R.id.media_play);

        txt_artist = (TextView) findViewById(R.id.txt_media_artist);
        txt_track = (TextView) findViewById(R.id.txt_media_track);
        registerReceiver(mReceiver, iF);

        txt_volume = (TextView) findViewById(R.id.txt_media_volume);
        txt_volume.setText("Audio Volume : " + am.getStreamVolume(AudioManager.STREAM_MUSIC) + " / " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        // Bluetooth 어댑터
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        txt_received = (TextView) findViewById(R.id.txt_received);
    }

    // 수신 텍스트의 추가
    private void addText(final String text) {
        // 핸들러에 의한 사용자 인터페이스 조작
        handler.post(new Runnable() {
            public void run() {
                txt_received.setText(text +
                        System.getProperty("line.separator") +
                        txt_received.getText());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, RQ_ENABLE_BT);
        } else {
            if (chatService == null) chatService =
                    new BluetoothChatService(this, handler);
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (chatService != null) {
            if (chatService.getState() == BluetoothChatService.STATE_NONE) {
                // Bluetooth의 접속 대기(서버)
                chatService.start();

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.d("onDestory()", "브로드캐스트리시버 해제됨");
        if (chatService != null) chatService.stop();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            sTrack = intent.getStringExtra("track");
            sArtist = intent.getStringExtra("artist");

            txt_artist.setText("Artist : " + sArtist);
            txt_track.setText("Track : " + sTrack);
            Log.d("Track Changed! : ", sArtist + " - " + sTrack);
            send("3" + sArtist);
            sleep(50);
            send("4" + sTrack);

        }


        //http://stackoverflow.com/questions/10510292/track-info-of-currently-playing-music/11929681#11929681
        //https://hashcode.co.kr/questions/2070/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%8B%A4%EB%A5%B8-%EC%95%B1%EC%97%90%EC%84%9C-%EC%9E%AC%EC%83%9D-%EC%A4%91%EC%9D%B8-%EC%9D%8C%EC%95%85%EC%9D%98-%ED%8C%8C%EC%9D%BC-%EA%B2%BD%EB%A1%9C%EB%A5%BC-%EA%B0%80%EC%A0%B8-%EC%98%AC-%EC%88%98-%EC%9E%88%EB%8A%94-%EB%B0%A9%EB%B2%95%EC%9D%B4-%EC%9E%88%EC%9D%84%EA%B9%8C%EC%9A%94

    };

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

                try {
                    Intent buttonDown = new Intent(Intent.EXTRA_KEY_EVENT);
                    buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_APP_SWITCH));
                    registerReceiver(mReceiver, iF);
                    getBaseContext().sendOrderedBroadcast(buttonDown, null);
                    buttonDown = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.media_volup:
                volup();
                break;

            case R.id.media_voldown:
                voldown();
                break;

            case R.id.media_play:
                play();
                break;

            case R.id.media_pre:
                pre();
                break;

            case R.id.media_next:
                next();
                break;
            case R.id.button_connect:
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, RQ_CONNECT_DEVICE);
                break;
        }
    }

    // 채팅 서버로부터 정보를 취득하는 핸들러
    private final Handler handler = new Handler() {
        // 핸들 메시지
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 상태 변경
                case MSG_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            addText("접속 완료");
                            send("5" + String.valueOf(am.getStreamVolume(AudioManager.STREAM_MUSIC)));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            addText("접속 중");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            addText("미접속");
                            break;
                    }
                    break;
                // 메시지 수신
                case MSG_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    received_message = new String(readBuf, 0, msg.arg1);
                    Log.d("Recived Message ", received_message);

                    switch (received_message) {
                        case "play":
                            play();
                            break;
                        case "pre":
                            pre();
                            break;
                        case "next":
                            next();
                            break;
                        case "volup":
                            volup();
                            break;
                        case "voldown":
                            voldown();
                            break;

                    }

                    if (received_message.startsWith("5")) {
                        vol(received_message.substring(1, received_message.length()));
                        send("5" + String.valueOf(am.getStreamVolume(AudioManager.STREAM_MUSIC)));
                    }
                    addText(received_message);
                    break;
            }
        }
    };

    private void send(String s) {
        if (s.length() > 0)
            chatService.write(s.getBytes());
        Log.d("Send Metadata", s);
    }

    private void pre() {
        try {
            Log.d("Media Status", "Previous Track");
            Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            registerReceiver(mReceiver, iF);
            getBaseContext().sendOrderedBroadcast(buttonDown, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void play() {
        try {
            if (am.isMusicActive()) {
                Log.d("Media Status", "Pause");
                media_play.setText("■");
                send("2");
            } else {
                Log.d("Media Status", "Play");
                media_play.setText("▶");
                send("1");
            }
            Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            registerReceiver(mReceiver, iF);
            getBaseContext().sendOrderedBroadcast(buttonDown, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void next() {
        try {
            Log.d("Media Status", "Next Track");
            Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            registerReceiver(mReceiver, iF);
            getBaseContext().sendOrderedBroadcast(buttonDown, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void volup() {
        volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volume < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume + 1, AudioManager.FLAG_PLAY_SOUND);
            txt_volume.setText("Audio Volume : " + (volume + 1) + " / " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        } else {
            Toast.makeText(getApplicationContext(), "현재 최고음량입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void voldown() {
        volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volume > 0) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume - 1, AudioManager.FLAG_PLAY_SOUND);
            txt_volume.setText("Audio Volume : " + (volume - 1) + " / " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        } else {
            Toast.makeText(getApplicationContext(), "현재 최저음량입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void vol(String s) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(s), AudioManager.FLAG_PLAY_SOUND);
        txt_volume.setText("Audio Volume : " + Integer.parseInt(s) + " / " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    }

    // 다른 Bluetooth 단말기로부터의 검색을 유효하게 설정 (4)
    private void ensureDiscoverable() {
        if (btAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    // 어플리케이션 복귀 시 불린다.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 단말기 검색
            case RQ_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().
                            getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    // Bluetooth의 접속 요구(클라이언트)
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    chatService.connect(device);
                }
                break;
            // 검색 유효
            case RQ_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    chatService = new BluetoothChatService(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth가 유효하지 않습니다",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    // 옵션 메뉴 생성 시 불린다.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    // 옵션 메뉴 선택 시 불린다.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // 검색
            case R.id.menu_search:
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, RQ_CONNECT_DEVICE);
                return true;
            // 검색 유효
            case R.id.menu_able_detected:
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    private void sleep(int i) {
        // TODO Auto-generated method stub
        try {
            handler.sendMessage(handler.obtainMessage());
            Thread.sleep(i);
        } catch (Throwable t) {
        }
    }
}

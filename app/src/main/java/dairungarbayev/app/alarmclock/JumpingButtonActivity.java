package dairungarbayev.app.alarmclock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;

public class JumpingButtonActivity extends AppCompatActivity {

    private static final String TAG = "JumpingButtonActivity";
    private int onPauseCounter = 1;

    private final String ALARM_ID = "alarm_id";
    private final String ALARM_JSON_KEY = "alarm_json_";

    private ArFragment arFragment;
    private ViewRenderable alarmOffRenderable;

    private AnchorNode offAnchorNode;
    private Node offNode;

    private FloatingActionButton turnOffButton, refreshButton;
    private CustomAlarm alarm;

    private MediaPlayer player;
    private Vibrator vibrator;

    private Handler handler;
    private Handler initialHandler;
    private int height, width;
    private Pose initialPose;
    private boolean offButtonClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_jumping_button);

        int id = getIntent().getIntExtra(ALARM_ID,0);
        if (id != 0){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String json = prefs.getString(ALARM_JSON_KEY + id, "");
            if (json != null && !json.isEmpty()) {
                alarm = new CustomAlarm(getApplicationContext(), json);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),"Error: alarm JSON null or empty", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),"Error: alarm ID null", Toast.LENGTH_SHORT);
            toast.show();
        }



        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM,alarm.getVolume(),0);

        player = new MediaPlayer();
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_ALARM).build());
        player.setLooping(true);
        try {
            player.setDataSource(this, alarm.getRingtoneUri());
            player.prepare();
        } catch (IOException e){
            Toast toast = Toast.makeText(this,"Error setting media player data source",Toast.LENGTH_SHORT);
            toast.show();
        }
        player.start();

        if (alarm.isVibrationOn()){
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {1000, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createWaveform(pattern,0));
            } else vibrator.vibrate(pattern,0);
        }


        arFragment =(ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_ar_dismiss_alarm);
        refreshButton = findViewById(R.id.button_refresh_ar_fragment);

        ViewRenderable.builder()
                .setView(this,R.layout.layout_dismiss_alarm)
                .build()
                .thenAccept(viewRenderable -> alarmOffRenderable = viewRenderable)
                .exceptionally(throwable -> {
                    Toast toast = Toast.makeText(this,"Error building off renderable", Toast.LENGTH_SHORT);
                    toast.show();
                    return null;
                });

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        height = point.y;
        width = point.x;

        initialHandler = new Handler();
        initialRunnable.run();

        handler = new Handler();
        setRefreshButtonListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (alarm != null && onPauseCounter > 1){
            if (!offButtonClicked) {
                alarm.postpone();
                stopAndSave();
                Log.d(TAG, "onPause: if off buttonnot clicked");
            }
        }
        onPauseCounter++;
    }

    @Override
    public void onBackPressed() {
        //nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        initialHandler.removeCallbacks(initialRunnable);
        arFragment = null;
    }

    private Runnable initialRunnable = new Runnable() {
        @Override
        public void run() {
            Frame frame = arFragment.getArSceneView().getArFrame();
            if (frame != null){
                if (frame.getCamera().getTrackingState() == TrackingState.TRACKING){
                    initialPose = frame.getCamera().getPose();
                }
            }
            if (initialPose == null){
                initialHandler.postDelayed(initialRunnable,1);
            } else {
                Toast.makeText(JumpingButtonActivity.this,"Initial pose found",Toast.LENGTH_SHORT).show();
                initialHandler.removeCallbacks(initialRunnable);
                runnable.run();
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            boolean complete = false;
            Frame frame = arFragment.getArSceneView().getArFrame();

            if (frame != null){
                if (frame.getCamera().getTrackingState() == TrackingState.TRACKING){
                    for (HitResult hit : frame.hitTest((float)(width/2), (float)(height/2))) {
                        Trackable trackable = hit.getTrackable();
                        Camera camera = frame.getCamera();
                        if (calculateDistance(initialPose,camera.getPose()) > 4){
                            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(
                                    camera.getDisplayOrientedPose().compose(Pose.makeTranslation(0, 0, -1f))
                                            .extractTranslation());
                            if (anchor != null) {
                                offAnchorNode = new AnchorNode(anchor);
                                offAnchorNode.setParent(arFragment.getArSceneView().getScene());

                                offNode = new Node();
                                offAnchorNode.addChild(offNode);
                                offNode.setRenderable(alarmOffRenderable);

                                turnOffButton = alarmOffRenderable.getView().findViewById(R.id.button_dismiss_alarm);
                                turnOffButton.setOnClickListener(v -> {
                                    offButtonClicked = true;
                                    if (alarm.isRepeating()) {
                                        alarm.setAlarmOn();
                                    } else alarm.cancelAlarm();
                                    stopAndSave();
                                });
                                complete = true;
                            }
                        }

                        /*if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                            Pose cameraPose = frame.getCamera().getPose();
                            float distFromInitialPose = calculateDistance(initialPose, cameraPose);
                            float distFromCamera = calculateDistance(cameraPose, hit.getHitPose());

                            if (distFromInitialPose > 4 && distFromCamera < 1.3) {
                                Plane plane = (Plane) trackable;

                                Anchor anchor = plane.createAnchor(hit.getHitPose());
                                offAnchorNode = new AnchorNode(anchor);
                                offAnchorNode.setParent(arFragment.getArSceneView().getScene());

                                offNode = new Node();
                                offAnchorNode.addChild(offNode);
                                offNode.setRenderable(alarmOffRenderable);

                                turnOffButton = alarmOffRenderable.getView().findViewById(R.id.button_dismiss_alarm);
                                turnOffButton.setOnClickListener(v -> {
                                    offButtonClicked = true;
                                    if (alarm.isRepeating()) {
                                        alarm.setAlarmOn();
                                    } else alarm.cancelAlarm();
                                    stopAndSave();
                                });
                                complete = true;
                            }
                        }*/
                    }
                }
            }
            if (complete){
                handler.removeCallbacks(runnable);
            } else handler.postDelayed(runnable,1);
        }
    };

    private void setRefreshButtonListener(){
        refreshButton.setOnClickListener(v -> {
            initialHandler.removeCallbacks(initialRunnable);
            handler.removeCallbacks(runnable);

            if (offAnchorNode != null && offNode != null){
                initialPose = null;
                offAnchorNode.removeChild(offNode);
                arFragment.getArSceneView().getScene().removeChild(offAnchorNode);
                offAnchorNode.getAnchor().detach();
                offAnchorNode.setParent(null);
                offAnchorNode = null;
            }

            initialRunnable.run();
        });
    }

    private void stopAndSave(){
        player.stop();
        if (alarm.isVibrationOn()){
            vibrator.cancel();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ALARM_JSON_KEY + alarm.getId(), alarm.toJsonString());
        editor.apply();

        finish();
    }

    private float calculateDistance(Pose startPose, Pose endPose){
        float dx = startPose.tx() - endPose.tx();
        float dy = startPose.ty() - endPose.ty();
        float dz = startPose.tz() - endPose.tz();

        return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
}

package dairungarbayev.app.alarmclock;


import android.content.Context;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RingtoneChoiceDialogFragment extends DialogFragment {
    
    private static final String TAG = "RingtoneChoiceDialog";

    public interface OnRingtoneChosen{
        void sendRingtoneData(String title, Uri uri, int volume);
    }

    private OnRingtoneChosen onRingtoneChosen;

    private RadioGroup radioGroup;
    private Button cancelButton, okButton;
    private SeekBar seekBar;

    private ArrayList<String> titles;
    private ArrayList<Uri> uris;
    private Uri checkedUri;
    private String checkedTitle;

    private MediaPlayer player = new MediaPlayer();
    private int maxVolume, volume;

    RingtoneChoiceDialogFragment(Context context, Uri uri, int volume){
        titles = new ArrayList<>();
        uris = new ArrayList<>();
        checkedUri = uri;
        checkedTitle = RingtoneManager.getRingtone(context,uri).getTitle(context);
        this.volume = volume;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            onRingtoneChosen = (OnRingtoneChosen) getTargetFragment();
        } catch (ClassCastException e){
            Log.e(TAG, "onAttach: class cast exception " + e.getMessage() );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_fragment_ringtone_choice, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getReferencesToViews();
        setSeekBar();
        setPlayer();
        getRingtoneData();
        populateRadioGroup();
        setCancelButton();
        setOkButton();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.stop();
        player.release();
    }

    private void getReferencesToViews(){
        View view = getView();
        radioGroup = view.findViewById(R.id.radio_group_ringtones);
        cancelButton = view.findViewById(R.id.button_cancel_ringtone_choice);
        okButton = view.findViewById(R.id.button_ok_ringtone_choice);
        seekBar = view.findViewById(R.id.seek_bar_ringtone_volume_secondary);
    }

    private void getRingtoneData(){
        RingtoneManager manager = new RingtoneManager(getContext());
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();
        while (cursor.moveToNext() && !cursor.isAfterLast()){
            Uri uri = manager.getRingtoneUri(cursor.getPosition());
            uris.add(uri);
            Ringtone ringtone = manager.getRingtone(cursor.getPosition());
            titles.add(ringtone.getTitle(getContext()));
        }
    }

    private void populateRadioGroup(){
        int size = titles.size();

        for (int i = 0; i < size; i++){
            RadioButton radioButton = new RadioButton(getContext());
            String title = titles.get(i);
            Uri uri = uris.get(i);
            radioButton.setText(title);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.ringtone_radio_button_margin);
            radioButton.setLayoutParams(params);
            radioGroup.addView(radioButton);

            if (checkedUri.equals(uri)){
                radioButton.toggle();
            }


            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    player.stop();
                    radioButton.toggle();

                    checkedTitle = title;
                    checkedUri = uri;

                    player.reset();
                    setPlayer();
                    player.start();
                }
            });
        }
    }

    private void setPlayer(){
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_ALARM).build());
        player.setLooping(false);
        try {
            player.setDataSource(getContext(), checkedUri);
            player.prepare();
        } catch (IOException e){
            Toast toast = Toast.makeText(getContext(),"Error",Toast.LENGTH_SHORT);
            toast.show();
        }
        setVolume(volume);
    }

    private void setVolume(int volume){
        AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(AudioManager.STREAM_ALARM,volume,0);
    }

    private void setSeekBar(){
        AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        seekBar.setMax(maxVolume);
        seekBar.setProgress(volume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = progress;
                setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setCancelButton(){
        cancelButton.setOnClickListener(v -> {
            player.stop();
            getDialog().dismiss();
        });
    }

    private void setOkButton(){
        okButton.setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId() != -1 && !checkedTitle.equals("") && !checkedUri.equals(Uri.EMPTY)){
                onRingtoneChosen.sendRingtoneData(checkedTitle, checkedUri, volume);
                player.stop();
                getDialog().dismiss();
            } else {
                String text = getResources().getString(R.string.please_choose_ringtone);
                Toast toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}

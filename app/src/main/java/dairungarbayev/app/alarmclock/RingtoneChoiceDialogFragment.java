package dairungarbayev.app.alarmclock;


import android.content.Context;
import android.database.Cursor;
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
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RingtoneChoiceDialogFragment extends DialogFragment {
    
    private static final String TAG = "RingtoneChoiceDialog";

    public interface OnRingtoneChosen{
        void sendRingtoneData(String title, Uri uri);
    }

    private OnRingtoneChosen onRingtoneChosen;

    private RadioGroup radioGroup;
    private Button cancelButton, okButton;

    private ArrayList<String> titles;
    private ArrayList<Uri> uris;
    private Uri checkedUri;
    private String checkedTitle;

    private Ringtone ringtone;

    RingtoneChoiceDialogFragment(Context context, Uri uri){
        titles = new ArrayList<>();
        uris = new ArrayList<>();
        checkedUri = uri;
        ringtone = RingtoneManager.getRingtone(context,uri);
        checkedTitle = ringtone.getTitle(context);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getReferencesToViews();
        getRingtoneData();
        populateRadioGroup();
        setCancelButton();
        setOkButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onPause() {
        super.onPause();
        ringtone.stop();
    }

    private void getReferencesToViews(){
        View view = getView();
        radioGroup = view.findViewById(R.id.radio_group_ringtones);
        cancelButton = view.findViewById(R.id.button_cancel_ringtone_choice);
        okButton = view.findViewById(R.id.button_ok_ringtone_choice);
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
                    ringtone.stop();
                    radioButton.toggle();

                    checkedTitle = title;
                    checkedUri = uri;

                    ringtone = RingtoneManager.getRingtone(getContext(),uri);
                    ringtone.play();
                }
            });
        }
    }

    private void setCancelButton(){
        cancelButton.setOnClickListener(v -> {
            ringtone.stop();
            getDialog().dismiss();
        });
    }

    private void setOkButton(){
        okButton.setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId() != -1 && !checkedTitle.equals("") && !checkedUri.equals(Uri.EMPTY)){
                onRingtoneChosen.sendRingtoneData(checkedTitle, checkedUri);
                ringtone.stop();
                getDialog().dismiss();
            } else {
                String text = getResources().getString(R.string.please_choose_ringtone);
                Toast toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}

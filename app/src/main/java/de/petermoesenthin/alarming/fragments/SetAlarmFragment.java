/*
 * Copyright (C) 2014 Peter Mösenthin <peter.moesenthin@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.petermoesenthin.alarming.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;

import at.markushi.ui.CircleButton;
import de.petermoesenthin.alarming.AlarmReceiverActivity;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.AlarmGson;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.util.AlarmUtil;
import de.petermoesenthin.alarming.util.PrefUtil;
import de.petermoesenthin.alarming.util.StringUtil;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardArrayMultiChoiceAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;

public class SetAlarmFragment extends Fragment implements
        TimePickerDialogFragment.TimePickerDialogHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    //================================================================================
    // Members
    //================================================================================

    public static final String DEBUG_TAG = "SetAlarmFragment";
    private static final boolean D = true;

    private Context fragmentContext;
    CardListView mCardListView;
    /*
    private TextView textView_alarmTime;
    private TextView textView_am_pm;
    private CircleButton circleButton;
    private CheckBox checkBox_vibrate;
    */



    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_set_alarm, container, false);
        // Card view
        Card card = new Card(fragmentContext, R.layout.card_alarm_time);
        //CardView cardView = (CardView) rootView.findViewById(R.id.alarmCard);
        //cardView.setCard(card);

        //ListView
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.add(card);
        cards.add(card);
        mCardListView = (CardListView) rootView.findViewById(R.id.cardListView_alarm);
        mCardListView.setAdapter(new CardArrayAdapter(fragmentContext, cards));
        mCardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(D) {Log.d(DEBUG_TAG,"Received click." + view.toString() + " " + position + " "
                + id);}
            }
        });
        /*
        textView_alarmTime = (TextView) card.getCardView().findViewById(R.id.textView_alarmTime);
        textView_alarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        textView_am_pm = (TextView) card.getCardView().findViewById(R.id.textView_am_pm);

        circleButton = (CircleButton) card.getCardView().findViewById(R.id.button_alarm_set);
        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
                boolean alarmSet = alg.isAlarmSet();
                if(alarmSet){
                    deactivateAlarm();
                } else {
                    activateAlarm();
                }

            }
        });

        checkBox_vibrate = (CheckBox) card.getCardView().findViewById(R.id.checkBox_vibrate);
        checkBox_vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
                boolean vibrate = alg.vibrate();
                alg.setVibrate(!vibrate);
                checkBox_vibrate.setChecked(!vibrate);
                PrefUtil.setAlarmGson(fragmentContext, alg);
            }
        });


        Button test = (Button) rootView.findViewById(R.id.buttonTest);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AlarmReceiverActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        */

        PrefUtil.getApplicationPrefs(fragmentContext)
                .registerOnSharedPreferenceChangeListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlarmState();
    }

    @Override
    public void onStop() {
        super.onStop();
        PrefUtil.getApplicationPrefs(fragmentContext)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    //================================================================================
    // Methods
    //================================================================================

    /**
     * Update the alarm state view and preference
     */
    private void loadAlarmState(){
        if(D) {Log.d(DEBUG_TAG,"Updating alarm state from preference.");}
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        if(alg != null){
            if(D) {Log.d(DEBUG_TAG,"Alarm state found. Preparing views.");}
            setAlarmTimeView(alg.getHour(), alg.getMinute());
            setCircleButtonActive(alg.isAlarmSet());
            //checkBox_vibrate.setChecked(alg.vibrate());
        } else {
            if(D) {Log.d(DEBUG_TAG,"No alarm state found.");}
        }
    }

    private void setAlarmTimeView(int hour, int minute){
        String alarmFormatted = StringUtil.getTimeFormattedSystem(fragmentContext, hour,
                minute);
        String[] timeSplit = alarmFormatted.split(" ");
        //textView_am_pm.setVisibility(View.INVISIBLE);
        //textView_alarmTime.setText(timeSplit[0]);
        if(timeSplit.length > 1){
            //textView_am_pm.setText(timeSplit[1]);
            //textView_am_pm.setVisibility(View.VISIBLE);
        }
    }

    private void activateAlarm(){
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        alg.setAlarmSet(true);
        PrefUtil.setAlarmGson(fragmentContext, alg);
        Calendar calendarSet = AlarmUtil.getNextAlarmTimeAbsolute(alg.getHour(), alg.getMinute());
        AlarmUtil.setAlarm(fragmentContext, calendarSet);
        setCircleButtonActive(true);
    }

    private void deactivateAlarm(){
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        alg.setAlarmSet(false);
        PrefUtil.setAlarmGson(fragmentContext, alg);
        AlarmUtil.deactivateAlarm(fragmentContext);
        setCircleButtonActive(false);
    }

    //================================================================================
    // UI
    //================================================================================

    private void setCircleButtonActive(boolean isActive){
        if(isActive){
            //circleButton.setColor(getResources().getColor(R.color.material_yellow));
            //circleButton.setImageResource(R.drawable.ic_action_alarmclock_light);
        } else {
            //circleButton.setColor(getResources().getColor(R.color.veryLightGray));
            //circleButton.setImageResource(R.drawable.ic_alarmclock_light_no_bells);
        }
    }

    private void showColorPickerDialog(){
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        LayoutInflater inflater = (LayoutInflater) fragmentContext.getSystemService
                (Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_alarm_color,
                null);
        //Set dialog views
        //...

        builder = new AlertDialog.Builder(fragmentContext);
        builder.setView(layout);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.dialog_button_cancel, null);
        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do work on OK
                dialogInterface.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    /**
     * Show a timepicker to set the alarm time
     */
    private void showTimePicker() {
        if(D) {Log.d(DEBUG_TAG,"Showing time picker dialog.");}
        TimePickerBuilder tpb = new TimePickerBuilder()
                .setFragmentManager(getChildFragmentManager())
                .setStyleResId(R.style.BetterPicker_Alarming)
                .setTargetFragment(SetAlarmFragment.this);
        tpb.show();
    }

    //================================================================================
    // Callbacks
    //================================================================================

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        if(D) {Log.d(DEBUG_TAG,"Time picker finished. Setting alarm time.");}
        setAlarmTimeView(hourOfDay, minute);
        AlarmGson alg = PrefUtil.getAlarmGson(fragmentContext);
        alg.setHour(hourOfDay);
        alg.setMinute(minute);
        PrefUtil.setAlarmGson(fragmentContext, alg);
        activateAlarm();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PrefKey.ALARM_GSON)){
            loadAlarmState();
        }
    }
}

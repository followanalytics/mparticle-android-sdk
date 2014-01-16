package com.mparticle.particlebox;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mparticle.MParticleAPI;

/**
 * Created by sdozor on 1/7/14.
 */
public class EventTestFragment extends Fragment implements View.OnClickListener {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Spinner spinner, exceptionSpinner;
    private EditText viewEditText, screenEditText, errorEditText, unhandleErrorEditText;
    private Button eventButton, screenButton, handledErrorsButton, unhandledErrorsButton;
    private Handler exceptionHandler = new Handler();
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static EventTestFragment newInstance(int sectionNumber) {
        EventTestFragment fragment = new EventTestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public EventTestFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("eventlabel", viewEditText.getText());
        outState.putCharSequence("screenname", viewEditText.getText());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_events, container, false);

        spinner = (Spinner)v.findViewById(R.id.spinner);
        exceptionSpinner = (Spinner)v.findViewById(R.id.spinner2);

        spinner.setAdapter(new ArrayAdapter<MParticleAPI.EventType>(
                v.getContext(),
                android.R.layout.simple_list_item_1,
                MParticleAPI.EventType.values()));
        spinner.setSelection(8);

        exceptionSpinner.setAdapter(new ArrayAdapter<String>(
                v.getContext(),
                android.R.layout.simple_list_item_1,
                v.getResources().getStringArray(R.array.exceptions)));

        eventButton = (Button)v.findViewById(R.id.button);
        screenButton = (Button)v.findViewById(R.id.button2);
        eventButton.setOnClickListener(this);
        screenButton.setOnClickListener(this);
        handledErrorsButton = (Button)v.findViewById(R.id.button3);
        unhandledErrorsButton = (Button)v.findViewById(R.id.button4);
        handledErrorsButton.setOnClickListener(this);
        unhandledErrorsButton.setOnClickListener(this);
        viewEditText = (EditText)v.findViewById(R.id.edittext);
        viewEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                eventButton.setEnabled(viewEditText.getText().length() > 0);
            }
        });
        screenEditText = (EditText)v.findViewById(R.id.edittext2);
        screenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                screenButton.setEnabled(screenEditText.getText().length() > 0);
            }
        });
        errorEditText = (EditText)v.findViewById(R.id.edittext3);
        errorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handledErrorsButton.setEnabled(errorEditText.getText().length() > 0);
            }
        });
        if (savedInstanceState != null){
            viewEditText.setText(savedInstanceState.getCharSequence("eventlabel"));
        }

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ParticleActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onClick(View v) {
        String toastText = "Message logged.";
        switch (v.getId()){
            case R.id.button:
                MParticleAPI.getInstance(v.getContext()).logEvent(viewEditText.getText().toString(), (MParticleAPI.EventType)spinner.getSelectedItem());
                break;
            case R.id.button2:
                MParticleAPI.getInstance(v.getContext()).logScreenView(screenEditText.getText().toString());
                break;
            case R.id.button3:
                MParticleAPI.getInstance(v.getContext()).logErrorEvent(errorEditText.getText().toString());
                break;
            case R.id.button4:
                toastText = "Crashing...";
                switch (exceptionSpinner.getSelectedItemPosition()){
                    case 0:
                        v.postDelayed(npeRunnable, 2000);
                        break;
                    case 1:
                        v.postDelayed(ioobeRunnable, 2000);
                }
                break;
        }
        Toast.makeText(v.getContext(), toastText, 300).show();
    }

    private Runnable npeRunnable = new Runnable(){

        @Override
        public void run() {
            String someString = null;
            someString.contains("");
        }
    };

    private Runnable ioobeRunnable = new Runnable(){

        @Override
        public void run() {
            int[] someArray = new int[2];
            int someValue = someArray[500];
        }
    };

}
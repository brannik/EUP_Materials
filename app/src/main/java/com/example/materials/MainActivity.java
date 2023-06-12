package com.example.materials;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Context _context;
    LinearLayoutCompat _contentLayout;
    SendFile sendFile;
    SharedPreffManager manager;
    ArrayList<View> _elements = new ArrayList<>();
    FileManager fileManager;
    private boolean editMode = false;
    CharSequence s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _contentLayout = (LinearLayoutCompat) findViewById(R.id.content);
        _context = getApplicationContext();

        manager = new SharedPreffManager(_context);
        sendFile = new SendFile(this);
        fileManager = new FileManager(_context);

        AppCompatTextView dateNow = (AppCompatTextView) findViewById(R.id.CURRENT_DATE);
        Date d = new Date();
        s = DateFormat.format("MMMM d, yyyy ", d.getTime());
        dateNow.setText(s);

        LinearLayout addNewElement = (LinearLayout) findViewById(R.id.LAYOUT_ADD_NEW);
        addNewElement.setVisibility(View.GONE);

        BuildUI();

        // lock/unlock edit mode button
        ImageButton btnUnlock = (ImageButton) findViewById(R.id.BTN_UNLOCK);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = !editMode;
                if(editMode){
                    addNewElement.setVisibility(View.VISIBLE);
                }else{
                    addNewElement.setVisibility(View.GONE);
                }
                for (View s: _elements) {
                    LinearLayout ly = s.findViewById(R.id.LAYOUT_BUTTON);
                    if(!editMode){
                        ly.setVisibility(View.GONE);
                    }else{
                        ly.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // save data
        /*
        ImageButton btnSave = (ImageButton)findViewById(R.id.BUTTON_SAVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(_context,"Save all...",Toast.LENGTH_LONG).show();
                for (View iv: _elements) {
                    EditText colorName = (EditText) iv.findViewById(R.id.COLOR_NAME);
                    EditText colorWeight = (EditText) iv.findViewById(R.id.COLOR_WEIGHT);
                    manager.EditValue(colorName.getText().toString(),Float.parseFloat(colorWeight.getText().toString()));
                }
            }
        });
        */

        // add new element and save fields
        ImageButton btnAddElement = (ImageButton) findViewById(R.id.BUTTON_ADD_ELEMENT);
        btnAddElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText newElementName = (EditText) findViewById(R.id.NEW_ELEMENT_NAME);

                View v = LayoutInflater.from(_context).inflate(R.layout.element, _contentLayout, false);
                EditText txt = (EditText) v.findViewById(R.id.COLOR_NAME);
                txt.setText(newElementName.getText());
                EditText txtKG = (EditText) v.findViewById(R.id.COLOR_WEIGHT);
                txtKG.setText("0.0");
                txtKG.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(txt.getText().length() > 0.0f && txt.getText() != null)
                            manager.EditValue(txt.getText().toString(),Float.parseFloat(txtKG.getText().toString()));
                    }
                });
                ImageButton BTN = (ImageButton) v.findViewById(R.id.ELEMENT_DEL_BUTTON);
                BTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        manager.DeleteValue(txt.getText().toString());
                        _elements.remove(v);
                        _contentLayout.removeView(v);
                    }
                });
                manager.AddValue(newElementName.getText().toString(),0);
                newElementName.setText("");
                _elements.add(v);
                _contentLayout.addView(v);

            }
        });

        // send data
        // pupup with preview, reciever and button to send or cancel
        ImageButton btnSendData = (ImageButton) findViewById(R.id.BUTTON_SEND);
        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendDataPopup();
            }
        });

        // settings popup
        // low treshold, reciever/s, file format to send
        ImageButton btnSettings = (ImageButton) findViewById(R.id.BUTTON_SETTINGS_POPUP);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsPopup(view);
            }
        });

        //history popup
        ImageButton btnHistory = (ImageButton) findViewById(R.id.BTN_HISTORY);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryPopup(view);
                try {
                    fileManager.SaveToFile(manager.GetPrefs());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void BuildUI(){
        Map<String,?> keys = manager.GetPrefs().getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            //Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());

            View v = LayoutInflater.from(_context).inflate(R.layout.element, _contentLayout, false);
            LinearLayout warning = (LinearLayout) v.findViewById(R.id.WARNING_ICON);
            warning.setVisibility(View.GONE);
            if(Float.parseFloat(entry.getValue().toString())<= manager.GetWarningValue()){
                warning.setVisibility(View.VISIBLE);
            }else{
                warning.setVisibility(View.GONE);
            }
            EditText txt = (EditText) v.findViewById(R.id.COLOR_NAME);
            txt.setText(entry.getKey());
            // lock the text
            EditText txtKG = (EditText) v.findViewById(R.id.COLOR_WEIGHT);
            txtKG.setText(entry.getValue().toString());
            txtKG.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(txtKG.getText().length() > 0.0f && txtKG.getText() != null)
                        manager.EditValue(txt.getText().toString(),Float.parseFloat(txtKG.getText().toString()));
                }
            });

            LinearLayout btnLayout = (LinearLayout) v.findViewById(R.id.LAYOUT_BUTTON);
            btnLayout.setVisibility(View.INVISIBLE);

            ImageButton BTN = (ImageButton) v.findViewById(R.id.ELEMENT_DEL_BUTTON);
            BTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    manager.DeleteValue(txt.getText().toString());
                    _elements.remove(v);
                    _contentLayout.removeView(v);
                }
            });

            _elements.add(v);
            _contentLayout.addView(v);


        }

    }
    private void SettingsPopup(View parentView){

        LayoutInflater inflater = (LayoutInflater) parentView.getContext().getSystemService(parentView.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.p_settings, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        ImageButton btnClose = (ImageButton) popupView.findViewById(R.id.BTN_CLOSE_SETTINGS);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        DisplayEmails(popupView);

        ImageButton btnAddEmail = (ImageButton) popupView.findViewById(R.id.ADD_EMAIL);
        btnAddEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.AddEmail("");

                popupWindow.dismiss();
                SettingsPopup(parentView);
            }
        });

        EditText minWeight = (EditText) popupView.findViewById(R.id.MIN_WEIGHT_WARNING);
        minWeight.setText(String.valueOf(manager.GetWarningValue()));
        minWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(minWeight.getText().length() > 0.0f && minWeight.getText() != null)
                    manager.SetWarningValue(Float.parseFloat(minWeight.getText().toString()));
            }
        });


        CheckBox checkHTML = (CheckBox) popupView.findViewById(R.id.CHECK_HTML);
        CheckBox checkPDF = (CheckBox) popupView.findViewById(R.id.CHECK_PDF);
        CheckBox checkDOCX = (CheckBox) popupView.findViewById(R.id.CHECK_DOCX);
        CheckBox checkEXEL = (CheckBox) popupView.findViewById(R.id.CHECK_EXEL);

        checkEXEL.setChecked(manager.getCheckButtonState("EXEL"));
        checkDOCX.setChecked(manager.getCheckButtonState("DOCX"));
        checkHTML.setChecked(manager.getCheckButtonState("HTML"));
        checkPDF.setChecked(manager.getCheckButtonState("PDF"));

        checkEXEL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                manager.SaveCheckButtonState("EXEL",b);
            }
        });
        checkDOCX.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                manager.SaveCheckButtonState("DOCX",b);
            }
        });
        checkPDF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                manager.SaveCheckButtonState("PDF",b);
            }
        });
        checkHTML.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                manager.SaveCheckButtonState("HTML",b);
            }
        });

        ImageButton btnReset = (ImageButton) popupView.findViewById(R.id.RESET_ALL_DATA);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.ResetAll();
            }
        });

    }
    private void DisplayEmails(View view){
        LinearLayout emailsLayout = (LinearLayout) view.findViewById(R.id.EMAIL_LAYOUT);
        Map<String,?> keys = manager.GetEmailPrefs().getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            View v = LayoutInflater.from(_context).inflate(R.layout.e_emails, emailsLayout, false);
            EditText email = (EditText) v.findViewById(R.id.EMAIL_TEXT);
            email.setText(entry.getValue().toString());
            TextView emailId = (TextView)v.findViewById(R.id.EMAIL_ID);
            emailId.setText(entry.getKey());

            email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(email.getText().length() > 0)
                        manager.EditEmail(entry.getKey(),email.getText().toString());
                }
            });

            ImageButton delBtn = (ImageButton)v.findViewById(R.id.EMAIL_ELEMENT_DEL_BUTTON);
            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    manager.RemoveEmail(entry.getKey());
                    emailsLayout.removeView(v);
                }
            });
            emailsLayout.addView(v);
        }
    }

    private void DisplayHistory(View view){
        LinearLayout historyLayout = (LinearLayout) view.findViewById(R.id.HISTORY_LAYOUT);
        //txt.setText(fileManager.ReadFile());
        //Log.println(Log.DEBUG,"HISTORY","DEBUG MSG");
        Map<String,?> keys = manager.getHistory().getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            View v = LayoutInflater.from(_context).inflate(R.layout.e_history, historyLayout, false);
            TextView txtDate = (TextView) v.findViewById(R.id.TEXT_HISTORY_DATE);

            LinearLayout dataLayout = (LinearLayout) v.findViewById(R.id.HISTORY_DATA_LAYOUT);
            dataLayout.setVisibility(View.GONE);
            // populate datafor the current date in dataLayout with inflater

            try {
                JSONObject jsonObj = new JSONObject(entry.getValue().toString());
                JSONArray keyss = jsonObj.names ();
                for (int i = 0; i < keyss.length (); i++) {

                    String key = keyss.getString (i); // Here's your key
                    String value = jsonObj.getString (key); // Here's your value
                    // inflate the hystory data here
                    View hd = LayoutInflater.from(_context).inflate(R.layout.e_history_data, dataLayout, false);
                    LinearLayout warning = (LinearLayout) hd.findViewById(R.id.WARNING_ICON);
                    warning.setVisibility(View.GONE);
                    if(Float.parseFloat(value)<= manager.GetWarningValue()){
                        warning.setVisibility(View.VISIBLE);
                    }else{
                        warning.setVisibility(View.GONE);
                    }
                    TextView colorName = (TextView) hd.findViewById(R.id.HISTORY_DATA_COLOR_NAME);
                    TextView colorWeight = (TextView) hd.findViewById(R.id.HISTORY_DATA_COLOR_WEIGHT);
                    colorName.setText(key);
                    colorWeight.setText(value + "кг");
                    dataLayout.addView(hd);

                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            ImageButton btnShowData = (ImageButton) v.findViewById(R.id.BTN_SHOW_DATA);
            btnShowData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(dataLayout.getVisibility() == View.GONE){
                        dataLayout.setVisibility(View.VISIBLE);
                    }else{
                        dataLayout.setVisibility(View.GONE);
                    }

                }
            });
            txtDate.setText(entry.getKey());
            historyLayout.addView(v);
        }
    }
    private void SendDataPopup(){
        sendFile.Send();
        // save to history
        SaveToHistory();
    }
    private void HistoryPopup(View parentView){
        LayoutInflater inflater = (LayoutInflater) parentView.getContext().getSystemService(parentView.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.p_history, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        ImageButton btnClose = (ImageButton) popupView.findViewById(R.id.BTN_CLOSE_HISTORY);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        DisplayHistory(popupView);

    }
    private void SaveToHistory(){
        try {
            fileManager.SaveToFile(manager.GetPrefs());
            Map<String,String> data = new HashMap<>();
            Map<String,?> keys = manager.GetPrefs().getAll();
            for(Map.Entry<String,?> entry : keys.entrySet()){
                data.put(entry.getKey(),entry.getValue().toString());
            }
            manager.AddToHistory(s.toString(),data.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }
}
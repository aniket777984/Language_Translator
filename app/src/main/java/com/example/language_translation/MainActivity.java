package com.example.language_translation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner1,spinner2;
    private TextInputEditText sourceText;
    private ImageView mic;
    private MaterialButton translatebtn;
    private TextView translatedText;
    private Toolbar mtoolbar;

    String[] fromLanguages = {"From", "English", "Afrikaans", "Arabic","Belarusian","Bulgarian","Bengali","Hindi","Urdu"};
    String[] toLanguages = {"To", "English", "Afrikaans", "Arabic","Belarusian","Bulgarian","Bengali", "Hindi" , "Urdu"};

    private static final int REQUEST_PERMISSION_CODE =  1;
    int languageCode,fromLanguageCode,toLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner1 = findViewById(R.id.spinnerid1);
        spinner2 = findViewById(R.id.spinnerid2);
        sourceText = findViewById(R.id.idEditSource);
        mic = findViewById(R.id.idmic);
        translatebtn = findViewById(R.id.btntranslate);
        translatedText = findViewById(R.id.translatedtext);
        mtoolbar =  findViewById(R.id.toolbar);

        setSupportActionBar(mtoolbar);



        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(fromAdapter);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(toAdapter);

        translatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedText.setText("");
                if(sourceText.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this,"Please Enter Your Text To Translate",Toast.LENGTH_SHORT).show();
                }
                else if(fromLanguageCode == 0){
                    Toast.makeText(MainActivity.this,"Please Select Source Language",Toast.LENGTH_SHORT).show();
                }
                else if(toLanguageCode == 0){
                    Toast.makeText(MainActivity.this,"Please Select To Language",Toast.LENGTH_SHORT).show();
                }
                else{
                    translatetext(fromLanguageCode,toLanguageCode,sourceText.getText().toString());
                }
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    startActivityForResult(intent,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PERMISSION_CODE)
        {
            if(resultCode == RESULT_OK && data != null)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceText.setText(result.get(0));
            }
        }
    }

    private void translatetext(int fromLanguageCode, int toLanguageCode, String source){
        translatedText.setText("Translating ...");
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(fromLanguageCode)
                        .setTargetLanguage(toLanguageCode)
                        .build();
        FirebaseTranslator translator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedText.setText("Downloading Model...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedText.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Failed To Translate",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed to Download Language Model",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int getLanguageCode(String language) {
        int code = 0;
        switch (language){
            case "English":
                code = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                code = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                code = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                code = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                code = FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                code = FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                code = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                code = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                code = FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                code = FirebaseTranslateLanguage.HI;
                break;
            case "Urdu":
                code = FirebaseTranslateLanguage.UR;
                break;
            default:
                code = 0;
        }
        return code;
    }


}
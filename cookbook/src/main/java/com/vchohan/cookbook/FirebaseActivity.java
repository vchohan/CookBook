package com.vchohan.cookbook;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by vchohan on 11/21/16.
 */

public class FirebaseActivity extends AppCompatActivity {

    private EditText keyField;

    private EditText valueField;

    private Button addValueButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity);

        keyField = (EditText) findViewById(R.id.key_field);
        valueField = (EditText) findViewById(R.id.value_field);

        addValueButton = (Button) findViewById(R.id.add_value_button);
        addValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Write a message to the database
              FirebaseDatabase database = FirebaseDatabase.getInstance();

                String key = keyField.getText().toString();
                String value = valueField.getText().toString();

                DatabaseReference myRef = database.getReference(key);

                myRef.setValue(value);

            }
        });
    }
}

package com.kerux.admin_thesis_kerux.email;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kerux.admin_thesis_kerux.R;


public class SendMailActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextEmail;
    private EditText editTextSubject;
    private EditText editTextMessage;
    private Button buttonSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendemail);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextSubject = (EditText) findViewById(R.id.editTextSubject);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);
    }
    private void sendEmail() {
        String email = editTextEmail.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        String message = editTextMessage.getText().toString().trim();
        SendMailTask sm = new SendMailTask(this, email, subject, message);
        sm.execute();
    }
    @Override
    public void onClick(View v) {
        sendEmail();
    }
}
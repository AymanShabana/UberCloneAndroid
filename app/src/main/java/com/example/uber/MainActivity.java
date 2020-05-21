package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Switch typeSwitch;
    private Button getStarted;
    private boolean isDriver;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isDriver = false;
        mAuth = FirebaseAuth.getInstance();
        typeSwitch = findViewById(R.id.type_switch);
        typeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDriver = !isDriver;
            }
        });
        getStarted = findViewById(R.id.get_started_button);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            pd = new ProgressDialog(this);
            pd.setMessage("One moment please.");
            pd.show();
            Toast.makeText(this, "User recognized", Toast.LENGTH_SHORT).show();
            redirectUser();
        }
        else {
            getStarted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "User signed in anonymously", Toast.LENGTH_SHORT).show();
                                FirebaseDatabase.getInstance().getReference().child("AnonUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("isDriver").setValue(isDriver);
                                pd = new ProgressDialog(MainActivity.this);
                                pd.setMessage("One moment please.");
                                pd.show();
                                redirectUser();
                            } else {
                                Toast.makeText(MainActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

    }

    private void redirectUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("AnonUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((boolean)dataSnapshot.child("isDriver").getValue()){
                    Intent intent = new Intent(getApplicationContext(),ViewRequestsActivity.class);
                    startActivity(intent);
                    finish();
                    pd.dismiss();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(),RiderActivity.class);
                    pd.dismiss();
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

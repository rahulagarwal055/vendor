package com.hawkerry.vendor.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hawkerry.vendor.R;
import com.hawkerry.vendor.utils.HelperClass;
import com.hawkerry.vendor.utils.PrefManager;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText mobile, otp, countryCode;
    private String verificationId, deviceId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button btnSubmit, btnGetOtp;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        prefManager = new PrefManager(this);

        countryCode = findViewById(R.id.country_code);
        mobile = findViewById(R.id.mobile);
        otp = findViewById(R.id.otp);
        btnSubmit = findViewById(R.id.btn_submit);
        btnGetOtp = findViewById(R.id.btn_otp);

        otp.setEnabled(false);
        btnSubmit.setVisibility(View.GONE);

        btnGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mobile.getText().toString().isEmpty() || mobile.getText().toString().length() < 10) {
                    mobile.setError("Invalid Mobile Number");
                    mobile.requestFocus();
                    return;
                }

                getOtp();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private void getOtp() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                countryCode.getText().toString() + mobile.getText().toString(),
                60,
                TimeUnit.SECONDS,
                this,
                mCallBack
        );
    }

    private void submit() {
        if(otp.getText().toString().isEmpty() || otp.getText().toString().length() < 6) {
            otp.setError("Invalid Code");
            otp.requestFocus();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp.toString());
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            checkUser(task.getResult().getUser().getUid(), task.getResult().getUser().getPhoneNumber());
                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            if(phoneAuthCredential.getSmsCode() != null) {
                otp.setEnabled(true);
                otp.setText(phoneAuthCredential.getSmsCode());

                signInWithCredential(phoneAuthCredential);
            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);
            Log.d("Success", "Code Received " + s);
            verificationId = s;
        }
    };

    private void checkUser(final String firebaseUUID, final String mobile) {
        DocumentReference ref = db.collection("Vendors").document(mobile);

        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    if(doc.exists()) {
                        Toast.makeText(LoginActivity.this, "Document Exists", Toast.LENGTH_LONG).show();
                    } else {
                        createNewUser(firebaseUUID, mobile);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createNewUser(final String uuid, String mobile) {
        Map<String, String> obj = new HashMap<>();
        obj.put("deviceId", deviceId);
        obj.put("mobileNo", mobile);
        obj.put("userId", uuid);
        obj.put("createdOn", HelperClass.getDate(System.currentTimeMillis()));
        obj.put("zipCode", null);
        obj.put("name", null);
        obj.put("timeSlotId", null);
        obj.put("lat", null);
        obj.put("long", null);

        db.collection("Vendors").document(mobile)
                .set(obj)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        prefManager.setUUID(uuid);

//                        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
//                        startActivity(intent);

                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }}

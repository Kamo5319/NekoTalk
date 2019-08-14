package com.cheonghaejin.nekotalk;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.cheonghaejin.nekotalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.os.Debug.waitForDebugger;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private String main_background;
    private ImageView profile;
    private Uri imageUri; // 업로드할때 uri를 담아주는

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        main_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(main_background));
        }   // 스테이트바 색깔 적용


        // 초기화
        profile = (ImageView)findViewById(R.id.signupActivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 사진을 클릭 했을 때 앨범을 여는 이벤트
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
                //activityresult에 이벤트값이 모여 switch처럼 원하는 곳에 이벤트를 넘겨줌
            }
        });


        email = (EditText)findViewById(R.id.signupActivity_edittext_email);
        name = (EditText)findViewById(R.id.signupActivity_edittext_name);
        password = (EditText)findViewById(R.id.signupActivity_edittext_password);
        signup = (Button)findViewById(R.id.signupActivity_button_signup);
        signup.setBackgroundColor(Color.parseColor(main_background)); // 색깔 주기


        // 이 부분과
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 회원가입

                if (email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null || imageUri == null){
                    return;
                }

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        // 회원가입 완료되면 complete 로 넘어옴
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            final String uid = task.getResult().getUser().getUid(); //uid를 받아서 바로 데이터 베이스에 저장
                            // 회원 가입을 할 때 자신의 이름을 넣는 코드

                            FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

//                                    String imageUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//                                    String imageUrl = task.getResult().getStorage().getDownloadUrl().toString();
                                    StorageReference ref = FirebaseStorage.getInstance().getReference().child("userImages").child(uid);

                                    final UserModel userModel = new UserModel();
                                    userModel.userName = name.getText().toString();
                                    userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

//                                    waitForDebugger();
                                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            String imageUrl = task.getResult().toString();
                                            userModel.profileImageUrl = imageUrl;

                                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    SignupActivity.this.finish();
                                                }
                                            });
                                        }
                                    });

//                                    userModel.profileImageUrl = imageUrl;


                                }
                            });

                        }
                });

            }
        });
    }
    // 이 부분
    @Override // 결과값 , 요청한 사람이 누군지 판별
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
            profile.setImageURI(data.getData()); // 가운데 뷰를 바꿈
            imageUri = data.getData(); // 이미지 경로 원본 (이미지 원본을 가지고 있음)
        }
    }
}

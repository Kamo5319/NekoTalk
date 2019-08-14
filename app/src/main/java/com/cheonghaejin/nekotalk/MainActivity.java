package com.cheonghaejin.nekotalk;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.cheonghaejin.nekotalk.fragment.AccountFragment;
import com.cheonghaejin.nekotalk.fragment.ChatFragment;
import com.cheonghaejin.nekotalk.fragment.PeopleFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainactivity_bottomnavigationview);



        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_people:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,new PeopleFragment()).commit();
                        return true;
                    case R.id.action_chat:                                                                    // 여기
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,new ChatFragment()).commit();
                        return true;
                    case R.id.action_account:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,new AccountFragment()).commit();
                        return true;
                }
                return false;
            }
        });

        // by.hnc 로그인 했을 때 첫번째 화면 선택 고정
        bottomNavigationView.setSelectedItemId(R.id.action_people);

//        passPushTokenToServer();
    }
    // 푸쉬 메세지 토큰
//    void passPushTokenToServer(){
//
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        String token = FirebaseInstanceId.getInstance().getToken();
//        Map<String,Object> map = new HashMap<>();
//        map.put("pushToken",token);
//
//        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
//    }

}

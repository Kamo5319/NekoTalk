package com.cheonghaejin.nekotalk.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cheonghaejin.nekotalk.R;
import com.cheonghaejin.nekotalk.model.ChatModel;
import com.cheonghaejin.nekotalk.model.NotificationModel;
import com.cheonghaejin.nekotalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    // 데이터 포멧 만들어 주기
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private UserModel destinationUserModel;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    int peopleCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 글로벌 선언
        // destinationUid : 채팅을 신청 당한 사람
        // Uid : 채팅을 신청한 사람, 즉 단말기에 로그인된 UID
        destinationUid = getIntent().getStringExtra("destinationUid");

        // 버튼 찾아주기
        button = (Button) findViewById(R.id.messageActivity_button);
        editText = (EditText) findViewById(R.id.messageActivity_editText);

        recyclerView = (RecyclerView) findViewById(R.id.messageActivity_recyclerview);
        //버튼에 이벤트 넣어주기
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼을 누르면 대화방이 만들어지는 것을 만듬
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);


                if (chatRoomUid == null) {        // push()는 일종의 프라이머리(Primary key) 키, 이것을 넣어야 채팅방에 이름이 임의적으로 생성되어 채팅방이 만들어짐
                    button.setEnabled(false);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();  // 중복 체크 해결 .addOnSuccessListener(new OnSuccessListener<Void>()
                        }
                    });
                } else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    comment.timestamp = ServerValue.TIMESTAMP; // 타임스템프 출력
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
//                            sendGcm();
                            editText.setText(""); // 채팅을 칠 때 자동으로 이전 채팅 내용 초기화
                        }
                    });
                }

            }
        });
        checkChatRoom();


    }
// 푸쉬 메세지 부분
    // 푸쉬 메세지 생성 부분
//    void sendGcm() {
//
//        Gson gson = new Gson();
//
////        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
//        NotificationModel notificationModel = new NotificationModel();
//        notificationModel.to = destinationUserModel.pushToken;
//        notificationModel.notification.title = "보낸이 아이디";
//        notificationModel.notification.text = editText.getText().toString();
//
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
//
//        Request request = new Request.Builder()
//                .header("Content-Type", "application/json")
//                .addHeader("Authorization", "key=AIzaSyBHyYtcOGUC6xgCe6Qi7KVyt4CUQI09QwE")
//                .url("https://gcm-http.googleapis.com/gcm/send")
//                .post(requestBody)
//                .build();
//        OkHttpClient okHttpClient = new OkHttpClient();
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//
//            }
//        });
//    }

    // 중복을 체크해주는 코드
    void checkChatRoom() {

        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if (chatModel.users.containsKey(destinationUid) && chatModel.users.size() == 2) {
                        chatRoomUid = item.getKey();
                        button.setEnabled(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments; // 코멘트를 불러오는 코드를 불러옴, 덧글을 달아주는

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    destinationUserModel = dataSnapshot.getValue(UserModel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        void getMessageList() {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear(); //clear를 안넣어주면 데이터가 계속 쌓임
                    Map<String, Object> readUsersMap = new HashMap<>();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        // 메세지 읽음
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);
                        comment_modify.readUsers.put(uid, true);

                        readUsersMap.put(key, comment_modify);
                        // 위의 comment 담아줌
                        comments.add(comment_origin);
                    }
                    // 처음 채팅 방을 들어갔을 때 comment 가 0 보다 클 때 아래 문을 실행
                    if (comments.size() > 0){
                        if (!comments.get(comments.size() - 1).readUsers.containsKey(uid)) {

                            // addOnCompleteListener : 서버가 반영된 것을 알고 이벤트 실행
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments")
                                    .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    // 메세지가 갱신
                                    notifyDataSetChanged(); // 리스트 새로 갱신

                                    // 메세지를 보낸 다음 자동으로 제일 하단으로 스크롤
                                    recyclerView.scrollToPosition(comments.size() - 1);
                                }
                            });
                        } else {
                            notifyDataSetChanged();
                            recyclerView.scrollToPosition(comments.size() - 1);
                        }


                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder) holder);


            // 내가 보낸 메세지
            if (comments.get(position).uid.equals(uid)) {  // 내가 쓰는 말풀선과 상대가 쓰는 말풍선을 나눔
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble); // 오른쪽 말풍선 불러오기
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE); //상대방의 이름 부분을 감춰줌
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position, messageViewHolder.textView_readCounter_left);

                // 상대방이 보낸 메세지지
            } else {
                // 표시 해 주는 부분
                Glide.with(holder.itemView.getContext())
                        .load(destinationUserModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textview_name.setText(destinationUserModel.userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position, messageViewHolder.textView_readCounter_right
                );

            }

            // unixTime 가져오기
            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date); // 우리가 알아볼 수 있는 숫자(시간)로 바뀜
            messageViewHolder.textView_timestamp.setText(time);

        }

        // 총인원이 몇명인지 셈
        void setReadCounter(final int position, final TextView textView) {
            if (peopleCount == 0) {

                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // String 값과 boolean 값을 헤쉬멥으로 받겠다.
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();

                        // 읽지 않은 사람에 대한 카운터       여기는 읽은 사람
                        int count = peopleCount - comments.get(position).readUsers.size();
                        if (count > 0) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        } else {
                            // 읽지 않은 사람이 0이하면 숨겨줌
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                int count = peopleCount - comments.get(position).readUsers.size();
                if (count > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                } else {
                    // 읽지 않은 사람이 0이하면 숨겨줌
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textview_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public MessageViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textview_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                // 말풍선을 왼쪽에 정렬할 건지 오른쪽에 정렬할 건지 할 때 씀
                linearLayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_textView_timestamp);
                textView_readCounter_left = (TextView) view.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right = (TextView) view.findViewById(R.id.messageItem_textview_readCounter_right);

            }
        }

    }

    @Override
    // back 키 작동 하는 코드
    public void onBackPressed() {
        //super.onBackPressed();
        // back 키를 눌렀을 때 꺼지지 않게 하는 코드
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }

        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}

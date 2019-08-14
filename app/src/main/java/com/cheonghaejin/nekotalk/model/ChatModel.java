package com.cheonghaejin.nekotalk.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String,Boolean> users = new HashMap<>(); // 채팅방의 유저들
    // 기존의 destinationUid : 채팅을 신청 당한 사람, Uid : 채팅을 신청한 사람 을 포함
    public Map<String,Comment> comments = new HashMap<>(); // 채팅방의 대화내용

    public static class Comment {

        public String uid;
        public String message;
        public Object timestamp;
        public Map<String,Object> readUsers = new HashMap<>();
    }
}

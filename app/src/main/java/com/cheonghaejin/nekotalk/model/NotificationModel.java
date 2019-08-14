package com.cheonghaejin.nekotalk.model;

// 푸쉬 메세지 만드는 용도의 Activity 지만 현재 사용 x
public class NotificationModel {

    public String to;

    public Notification notification = new Notification();

    public static class Notification {
        public String title;
        public String text;

    }
}

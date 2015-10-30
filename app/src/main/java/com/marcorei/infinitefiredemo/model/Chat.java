package com.marcorei.infinitefiredemo.model;

public class Chat {
    String name;
    String text;

    @SuppressWarnings("unused")
    public Chat() {
    }

    public Chat(String name, String message) {
        this.name = name;
        this.text = message;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}

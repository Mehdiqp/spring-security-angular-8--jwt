package com.backend.springsecurity.payload.response;

import lombok.Data;

@Data
public class MessageRespones {
    private String message;

    public MessageRespones(String s) {
        this.message=s;
    }
}

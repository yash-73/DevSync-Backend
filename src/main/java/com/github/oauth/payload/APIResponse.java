package com.github.oauth.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse {


    String message;
    boolean status;

    public APIResponse(String message){
        this.message = message;
        this.status = false;
    }

}
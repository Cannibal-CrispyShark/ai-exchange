package com.huge.aiexchange.entity.pojo;

import lombok.Data;

@Data
public class Response <T>{

    T body;
    String message;

    public static <T> Response<T> success(T body){
        Response<T> response = new Response<>();
        response.body = body;
        response.message = "success";
        return response;
    }

    public static <T> Response<T> fail(){
        Response<T> response = new Response<>();
        response.body = null;
        response.message = "fail";
        return response;
    }

    public static <T> Response<T> fail(String reason){
        Response<T> response = new Response<>();
        response.body = null;
        response.message = reason;
        return response;
    }
}

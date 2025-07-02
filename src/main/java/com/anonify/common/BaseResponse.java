package com.anonify.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseResponse<T> {
    private String status;
    private int code;
    private String message;
    private T data;
}
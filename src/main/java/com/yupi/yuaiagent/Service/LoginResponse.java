package com.yupi.yuaiagent.Service;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
}

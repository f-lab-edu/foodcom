package com.foodcom.firstpro.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessTokenResponse {
    private String grantType;
    private String accessToken;
}

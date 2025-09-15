package com.easychat.user.userservice.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserInfoDTO implements Serializable {
    private static final long serialVersionUID = -6910208948981307451L;
    private String token;
    private String userId;
    private String nickName;
    private Boolean admin;
}

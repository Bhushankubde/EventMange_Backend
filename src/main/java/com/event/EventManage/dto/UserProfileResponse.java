package com.event.EventManage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String displayName;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImage;
    private String role;
}

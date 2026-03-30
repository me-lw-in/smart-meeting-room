package com.example.smartmeetingroom.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequestDTO {

    private String firstName;

    private String lastName;

    private String oldPassword;

    private String newPassword;
}

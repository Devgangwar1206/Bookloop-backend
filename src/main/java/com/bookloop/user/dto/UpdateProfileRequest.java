package com.bookloop.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    private String name;

    private String phone;

    private String city;

    private String location;

    private String pincode;

    private String bio;

    private String avatar;
}
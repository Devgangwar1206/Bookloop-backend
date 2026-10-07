package com.bookloop.chat.dto;

 
public class UserSummaryDto {
    private String id;
    private String name;
    private String avatar;
    private Boolean verified;
    private String location;

    public UserSummaryDto() {}

    public UserSummaryDto(String id, String name, String avatar, Boolean verified, String location) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.verified = verified;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

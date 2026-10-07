package com.bookloop.user.dto;
 
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSettingsRequest {

    private boolean emailNotifications;

    private boolean offerNotifications;

    private boolean chatNotifications;

}
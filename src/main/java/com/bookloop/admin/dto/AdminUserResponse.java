package com.bookloop.admin.dto;

 
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {

    private Long id;

    private String name;

    private String email;

    private String avatar;

    private String role;

    private String city;

    private String location;

    private Long listingsCount;

    private Double rating;

    private String status;

    private boolean verified;

    private LocalDateTime createdAt;
}
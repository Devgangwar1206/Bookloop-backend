package com.bookloop.admin.dto;

 
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReportResponse {

    private Long id;

    private Long bookId;

    private String bookTitle;

    private String bookImage;

    private String sellerName;

    private String reportedBy;

    private String reason;

    private String details;

    private String date;

    private String status;
}
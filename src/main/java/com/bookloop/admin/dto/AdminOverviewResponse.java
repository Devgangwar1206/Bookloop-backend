package com.bookloop.admin.dto;

 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewResponse {

    private Metrics metrics;

    private List<AdminReportResponse> reports;

    private List<AdminUserResponse> users;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {

        private long totalUsers;

        private long totalBooks;

        private long totalExchanges;

        private String totalSalesValue;
    }
}
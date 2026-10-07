package com.bookloop.admin.service;

 
import com.bookloop.admin.dto.AdminOverviewResponse;
import com.bookloop.admin.dto.AdminReportResponse;
import com.bookloop.admin.dto.AdminUserResponse;
import com.bookloop.admin.entity.Report;
import com.bookloop.admin.repository.ReportRepository;
import com.bookloop.book.entity.Book;
import com.bookloop.book.repository.BookRepository;
import com.bookloop.order.entity.Order;
import com.bookloop.order.repository.OrderRepository;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;

import com.bookloop.exchange.repository.ExchangeRepository;
import com.bookloop.review.repository.ReviewRepository;
import com.bookloop.chat.dto.AppNotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final ReportRepository reportRepository;
    private final ExchangeRepository exchangeRepository;
    private final ReviewRepository reviewRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private void sendRealtimeNotification(AppNotificationDto dto) {
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/topic/notifications", dto);
            } catch (Exception e) {
                // ignore messaging failure
            }
        }
    }

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // =========================================================
    // ADMIN OVERVIEW
    // =========================================================

    @Transactional(readOnly = true)
    public AdminOverviewResponse getAdminOverview() {

        List<User> users = userRepository.findAll();

        List<Book> books = bookRepository.findAll();

        List<Order> orders = orderRepository.findAll();

        List<Report> reports =
                reportRepository.findAllByOrderByCreatedAtDesc();

        long totalUsers = users.size();

        long totalBooks = books.size();

        long totalExchanges = exchangeRepository.count();

        BigDecimal totalSalesValue = orders.stream()
                .filter(order -> order.getAmount() != null)
                .filter(order -> isSuccessfulOrder(order))
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AdminOverviewResponse.Metrics metrics =
                new AdminOverviewResponse.Metrics(
                        totalUsers,
                        totalBooks,
                        totalExchanges,
                        formatCurrency(totalSalesValue)
                );

        List<AdminUserResponse> userResponses =
                users.stream()
                        .sorted(
                                Comparator.comparing(
                                        User::getCreatedAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .map(this::mapUser)
                        .toList();

        List<AdminReportResponse> reportResponses =
                reports.stream()
                        .map(this::mapReport)
                        .toList();

        return new AdminOverviewResponse(
                metrics,
                reportResponses,
                userResponses
        );
    }

    // =========================================================
    // UPDATE USER STATUS
    // =========================================================

    public AdminUserResponse updateUserStatus(
            Long userId,
            String status) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "User status is required"
            );
        }

        if (!status.equalsIgnoreCase("Active")
                && !status.equalsIgnoreCase("Suspended")) {

            throw new IllegalArgumentException(
                    "Status must be Active or Suspended"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        /*
         * Admin account ko suspend nahi karne denge
         * through this generic user-status endpoint.
         */
        if (user.getRole() != null
                && user.getRole().name().equals("ADMIN")) {

            throw new IllegalArgumentException(
                    "Admin account cannot be suspended"
            );
        }

        boolean active =
                status.equalsIgnoreCase("Active");

        user.setEnabled(active);

        User savedUser =
                userRepository.save(user);

        return mapUser(savedUser);
    }

    // =========================================================
    // TOGGLE USER VERIFICATION
    // =========================================================

    @Transactional
    public AdminUserResponse toggleUserVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        user.setVerified(!user.isVerified());
        User savedUser = userRepository.save(user);

        return mapUser(savedUser);
    }

    // =========================================================
    // RESOLVE REPORT
    // =========================================================

    public AdminReportResponse resolveReport(
            Long reportId,
            String action) {

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Report action is required"
            );
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Report not found with id: " + reportId
                        )
                );

        String normalizedAction =
                action.trim().toLowerCase();

        switch (normalizedAction) {

            case "dismiss":

                report.setStatus("Dismissed");

                break;

            case "delete_listing":

                Book book = report.getBook();

                if (book != null) {
                    book.setStatus("REMOVED");
                    bookRepository.save(book);

                    if (book.getSeller() != null) {
                        sendRealtimeNotification(AppNotificationDto.builder()
                                .id("notif-rem-" + System.currentTimeMillis())
                                .type("LISTING_REMOVED")
                                .title("Listing Removed by Admin")
                                .message("Your listing \"" + book.getTitle() + "\" was removed by moderation for policy compliance.")
                                .targetUserId(String.valueOf(book.getSeller().getId()))
                                .targetUserEmail(book.getSeller().getEmail())
                                .actionUrl("/dashboard")
                                .time("Just now")
                                .timestamp(System.currentTimeMillis())
                                .build());
                    }
                }

                report.setStatus("Resolved");

                break;

            case "warn_seller":

                Book warnedBook = report.getBook();
                if (warnedBook != null && warnedBook.getSeller() != null) {
                    sendRealtimeNotification(AppNotificationDto.builder()
                            .id("notif-warn-" + System.currentTimeMillis())
                            .type("SELLER_WARNING")
                            .title("Community Moderation Warning")
                            .message("Warning issued regarding your listing \"" + warnedBook.getTitle() + "\": " + report.getReason())
                            .targetUserId(String.valueOf(warnedBook.getSeller().getId()))
                            .targetUserEmail(warnedBook.getSeller().getEmail())
                            .actionUrl("/dashboard")
                            .time("Just now")
                            .timestamp(System.currentTimeMillis())
                            .build());
                }

                report.setStatus("Resolved");

                break;

            default:

                throw new IllegalArgumentException(
                        "Invalid report action: " + action
                );
        }

        Report savedReport =
                reportRepository.save(report);

        return mapReport(savedReport);
    }

    // =========================================================
    // CREATE REPORT
    // =========================================================

    public AdminReportResponse createReport(
            Long bookId,
            String reason,
            String details,
            String reporterEmail) {

        if (bookId == null) {
            throw new IllegalArgumentException(
                    "Book id is required"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Report reason is required"
            );
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Book not found with id: " + bookId
                        )
                );

        User reporter = null;
        if (reporterEmail != null && !reporterEmail.isBlank()) {
            reporter = userRepository.findByEmail(reporterEmail).orElse(null);
        }
        if (reporter == null) {
            reporter = userRepository.findByEmail("admin@gmail.com").orElse(null);
        }
        if (reporter == null) {
            reporter = userRepository.findAll().stream().findFirst().orElse(null);
        }

        Report report = Report.builder()
                .book(book)
                .reportedBy(reporter)
                .reason(reason)
                .details(details)
                .status("Pending")
                .build();

        Report savedReport =
                reportRepository.save(report);

        sendRealtimeNotification(AppNotificationDto.builder()
                .id("notif-rep-" + System.currentTimeMillis())
                .type("REPORT_CREATED")
                .title("New Community Report")
                .message("Listing \"" + book.getTitle() + "\" reported for: " + reason)
                .senderId(String.valueOf(reporter.getId()))
                .senderName(reporter.getName())
                .senderAvatar(reporter.getAvatar())
                .actionUrl("/admin")
                .time("Just now")
                .timestamp(System.currentTimeMillis())
                .build());

        return mapReport(savedReport);
    }

    // =========================================================
    // GET ALL USERS & REPORTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapReport)
                .toList();
    }

    // =========================================================
    // MAPPING USER
    // =========================================================

    private AdminUserResponse mapUser(User user) {

        String status =
                user.isEnabled()
                        ? "Active"
                        : "Suspended";

        long listingsCount = bookRepository.countBySeller(user);

        Double avgRating = reviewRepository.getAverageRating(user);
        double rating = avgRating != null ? avgRating : 4.8;

        String location = user.getLocation() != null && !user.getLocation().isBlank()
                ? user.getLocation()
                : (user.getCity() != null && !user.getCity().isBlank() ? user.getCity() : "Noida, UP");

        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(
                        user.getRole() != null
                                ? user.getRole().name()
                                : "USER"
                )
                .city(user.getCity())
                .location(location)
                .listingsCount(listingsCount)
                .rating(rating)
                .status(status)
                .verified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // =========================================================
    // MAPPING REPORT
    // =========================================================

    private AdminReportResponse mapReport(
            Report report) {

        Book book = report.getBook();

        User seller =
                book != null
                        ? book.getSeller()
                        : null;

        User reporter =
                report.getReportedBy();

        String date = "";

        if (report.getCreatedAt() != null) {
            date = report.getCreatedAt()
                    .format(DATE_FORMAT);
        }

        return AdminReportResponse.builder()
                .id(report.getId())
                .bookId(
                        book != null
                                ? book.getId()
                                : null
                )
                .bookTitle(
                        book != null
                                ? book.getTitle()
                                : "Unknown Book"
                )
                .bookImage(
                        getBookImage(book)
                )
                .sellerName(
                        seller != null
                                ? seller.getName()
                                : "Unknown Seller"
                )
                .reportedBy(
                        reporter != null
                                ? reporter.getName()
                                : "Unknown User"
                )
                .reason(report.getReason())
                .details(report.getDetails())
                .date(date)
                .status(report.getStatus())
                .build();
    }

    private String getBookImage(Book book) {

        if (book == null
                || book.getImages() == null
                || book.getImages().isEmpty()) {

            return null;
        }

        return book.getImages()
                .iterator()
                .next();
    }

    // =========================================================
    // ORDER CHECK
    // =========================================================

    private boolean isSuccessfulOrder(Order order) {

        if (order == null
                || order.getStatus() == null) {
            return false;
        }

        String status =
                order.getStatus().trim().toUpperCase();

        return status.equals("PENDING")
                || status.equals("CONFIRMED")
                || status.equals("PROCESSING")
                || status.equals("SHIPPED")
                || status.equals("DELIVERED")
                || status.equals("COMPLETED");
    }

    // =========================================================
    // CURRENCY
    // =========================================================

    private String formatCurrency(
            BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return "₹" + amount.toPlainString();
    }
}
package com.ticketwave.reports.infrastructure.dto.report;

import java.util.List;

/**
 * Bundle of every report the read model exposes, returned by a single
 * /api/reports/all request so dashboards can hydrate in one round-trip.
 */
public record AllReportsResponse(
        SalesOverviewResponse overview,
        List<SalesByEventResponse> salesByEvent,
        List<OrderReportResponse> orders,
        List<PaymentReportResponse> payments,
        List<TicketReportResponse> tickets,
        List<NotificationReportResponse> notifications) {
}

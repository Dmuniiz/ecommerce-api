package com.api.e_commerce.order.dto;

import com.api.e_commerce.order.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Order tracking information with timeline of events
 */
public record OrderTrackingResponse(
        UUID id,
        OrderStatus currentStatus,
        List<OrderTimelineEvent> timeline
) {
    public record OrderTimelineEvent(
            OrderStatus status,
            Instant timestamp,
            String description
    ) {
    }
}


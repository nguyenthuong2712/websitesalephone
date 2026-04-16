package org.example.websitesalephone.dto.order;

import lombok.Getter;
import lombok.Setter;
import org.example.websitesalephone.entity.OrderStatusHistory;
import org.example.websitesalephone.utils.Constants;

@Getter
@Setter
public class OrderStatusHistoryResponse {

    private String status;

    private String time;

    private String description;

    private String nameStaff;

    public static OrderStatusHistoryResponse from(OrderStatusHistory orderStatusHistory) {
        OrderStatusHistoryResponse orderStatusHistoryResponse = new OrderStatusHistoryResponse();
        orderStatusHistoryResponse.setStatus(orderStatusHistory.getStatus());
        orderStatusHistoryResponse.setDescription(orderStatusHistory.getDescription());
        orderStatusHistoryResponse.setTime(Constants.FORMATTER.format(orderStatusHistory.getCreatedAt()));
        orderStatusHistoryResponse.setNameStaff(orderStatusHistory.getOrder().getStaff() == null ? "" : orderStatusHistory.getOrder().getStaff().getFullName());
        return orderStatusHistoryResponse;
    }
}

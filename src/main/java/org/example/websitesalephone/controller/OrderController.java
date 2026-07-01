package org.example.websitesalephone.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.order.CountOrderRequest;
import org.example.websitesalephone.dto.order.OrderByUserRequest;
import org.example.websitesalephone.dto.order.OrderRequest;
import org.example.websitesalephone.dto.order.OrderSearch;
import org.example.websitesalephone.service.order.OrderService;
import org.example.websitesalephone.service.pdf.PDFGeneratorService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final PDFGeneratorService pdfGeneratorService;

    @PostMapping("/search")
    public CommonResponse search(@RequestBody OrderSearch orderSearch) {
        return orderService.search(orderSearch);
    }

    @GetMapping("/detail/{id}")
    public CommonResponse detail(@PathVariable String id) {
        return orderService.detail(id);
    }

    @PutMapping("/update")
    public CommonResponse update(@RequestBody OrderRequest orderRequest) {
        return orderService.update(orderRequest);
    }

    @GetMapping("/history/{id}")
    public CommonResponse getListHistory(@PathVariable String id) {
        return orderService.getListHistory(id);
    }

    @PostMapping("/order-by-user")
    public CommonResponse getListOrderByUser(@RequestBody OrderByUserRequest orderByUserRequest) {
        return orderService.getListOrderByUser(orderByUserRequest);
    }

    @PostMapping("/buy-now")
    public CommonResponse buyNow(@RequestBody org.example.websitesalephone.dto.order.BuyNowRequest buyNowRequest) {
        return orderService.buyNow(buyNowRequest);
    }

    @PostMapping("/count-order-user")
    public CommonResponse countOrderByUser(@RequestBody CountOrderRequest countOrderRequest) {
        return orderService.countOrderByUser(countOrderRequest);
    }

    @PostMapping("/count-order-staff")
    public CommonResponse countOrderByStaff(@RequestBody CountOrderRequest countOrderRequest) {
        return orderService.countOrderByStaff(countOrderRequest);
    }

    @GetMapping("/dashboard/{searchText}")
    public CommonResponse countDashBoard(@PathVariable String searchText, @RequestParam(required = false, defaultValue = "ALL") String range) {
        return orderService.countDashBoard(searchText, range);
    }

    @GetMapping("/pdf/generate/{id}")
    public CommonResponse generatePDF(HttpServletResponse response, @PathVariable("id") String id) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=pdf_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);
        return pdfGeneratorService.orderCouter(response, id);
    }
}

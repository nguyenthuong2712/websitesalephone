package org.example.websitesalephone.utils;


import org.example.websitesalephone.comon.PagingRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Utils {

    /**
     * Get paging request
     *
     * @param request PagingRequest
     * @return PageRequest
     */
    public static PageRequest getPaging(PagingRequest request) {
        int page = request.getPage() == null || request.getPage() < 1 ? 0 : request.getPage() - 1;
        PageRequest pageable = null;
        if (ObjectUtils.isEmpty(request.getSortBy())) {
            pageable = PageRequest.of(page, request.getSize() == null ? 10 : request.getSize());
        } else {
            pageable = PageRequest.of(page, request.getSize() == null ? 10 : request.getSize(),
                    Sort.by(request.isSortDesc() ? Sort.Direction.DESC : Sort.Direction.ASC,
                            request.getSortBy()));
        }
        return pageable;
    }

    public static String generateUniqueCode(String prefix) {
        String timePart = OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.US));

        int random = (int) (Math.random() * 900) + 100;

        return prefix + timePart + random;
    }

    public static String formatBigDecimal(BigDecimal decimal) {
        Locale locale = new Locale("vi", "VN"); // Locale cho tiếng Việt
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        symbols.setGroupingSeparator('.');

        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.setDecimalFormatSymbols(symbols);

        return decimalFormat.format(decimal);
    }

}


package org.example.websitesalephone.service.pdf.impl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.OrderItem;
import org.example.websitesalephone.repository.OrderRepository;
import org.example.websitesalephone.service.pdf.PDFGeneratorService;
import org.example.websitesalephone.utils.Constants;
import org.example.websitesalephone.utils.Utils;
import org.springframework.stereotype.Service;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PDFGeneratorServiceImpl implements PDFGeneratorService {

    private final OrderRepository orderRepository;

    @Override
    public CommonResponse orderCouter(HttpServletResponse response, String id) throws IOException {
        Order order = orderRepository.findById(id).orElse(null);

        BigDecimal tongTienKhachTra = BigDecimal.ZERO;

        for (OrderItem h : order.getOrderItems()) {
            tongTienKhachTra = tongTienKhachTra.add(h.getUnitPrice());
        }

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            String fontPath = "fonts/Roboto-Regular.ttf";

            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 18, Font.BOLD);
            Font fontParagraph = new Font(bf, 12, Font.BOLD);

            PdfPTable lineTable = new PdfPTable(1);
            lineTable.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell(new Phrase(""));
            lineCell.setBorder(Rectangle.BOTTOM);
            lineCell.setBorderWidth(1); // Độ dày của đường gạch
            lineCell.setPaddingBottom(5); // Khoảng cách giữa bảng và đường gạch
            lineCell.setBorderColor(BaseColor.BLACK); // Màu của đường gạch, có thể thay đổi
            lineTable.addCell(lineCell);

            Paragraph paragraph = new Paragraph("Website bán hàng", fontTitle);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(new Paragraph("\n"));
            document.add(lineTable);

            Paragraph paragraph2 = new Paragraph("Số điện thoại: 0987654321", fontParagraph);
            paragraph2.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph2);

            Paragraph paragraph3 = new Paragraph("Email: buitkien66@gmail.com", fontParagraph);
            paragraph3.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph3);

            Paragraph paragraph4 = new Paragraph("Địa chỉ: Trịch văn bô, Nam từ liêm Hà nội", fontParagraph);
            paragraph4.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph4);

            Paragraph paragraph5 = new Paragraph("Hóa đơn bán hàng", fontTitle);
            paragraph5.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph5);

            Paragraph paragraph12 = new Paragraph(order.getOrderCode(), fontParagraph);
            paragraph12.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph12);

            Paragraph paragraph6 = new Paragraph("Ngày mua: " + Constants.FORMATTER.format(order.getCreatedAt()), fontParagraph);
            paragraph6.setAlignment(Element.ALIGN_LEFT);
            document.add(paragraph6);

            Paragraph paragraph7 = new Paragraph("Khách hàng: " + order.getCustomer().getFullName(), fontParagraph);
            paragraph7.setAlignment(Element.ALIGN_LEFT);
            document.add(paragraph7);

            if (order.getAddressDetail() != null) {
                Paragraph paragraph8 = new Paragraph("Địa chỉ: " + order.getAddressDetail(), fontParagraph);
                paragraph8.setAlignment(Element.ALIGN_LEFT);
                document.add(paragraph8);
            }

            if (order.getCustomer().getPhone() != null) {
                Paragraph paragraph9 = new Paragraph("Số điện thoại: " + order.getCustomer().getPhone(), fontParagraph);
                paragraph9.setAlignment(Element.ALIGN_LEFT);
                document.add(paragraph9);
            }

            Paragraph paragraph10 = new Paragraph("Nhân viên bán hàng: " + order.getStaff().getFullName(), fontParagraph);
            paragraph10.setAlignment(Element.ALIGN_LEFT);
            document.add(paragraph10);

            Paragraph paragraph11 = new Paragraph("DANH SÁCH SẢN PHẨM KHÁCH HÀNG MUA", fontParagraph);
            paragraph11.setSpacingBefore(15.0f);
            paragraph11.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph11);
            document.add(new Paragraph("\n")); // Thêm một dòng trống

            Font fontTableHeader = new Font(bf, 12, Font.BOLD);
            String[] tableHeaders = {"STT", "Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 4, 1, 2, 2});

            for (String header : tableHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontTableHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            BigDecimal tongTienSanPham = BigDecimal.ZERO;
            int index = 1;

            for (OrderItem item : order.getOrderItems()) {

                BigDecimal thanhTien = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

                table.addCell(new PdfPCell(new Phrase(String.valueOf(index++), fontParagraph)));
                table.addCell(new PdfPCell(new Phrase(item.getProductVariant().getProduct().getName(), fontParagraph)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), fontParagraph)));
                table.addCell(new PdfPCell(new Phrase(Utils.formatBigDecimal(item.getUnitPrice()) + " VND", fontParagraph)));
                table.addCell(new PdfPCell(new Phrase(Utils.formatBigDecimal(thanhTien) + " VND", fontParagraph)));

                tongTienSanPham = tongTienSanPham.add(thanhTien);
            }

            Font fontTotal = new Font(bf, 12, Font.BOLDITALIC);
            PdfPCell cellTotalLabel = new PdfPCell(
                    new Phrase("Tổng tiền sản phẩm: " + Utils.formatBigDecimal(tongTienSanPham) + " VND", fontTotal)
            );
            cellTotalLabel.setColspan(5);
            cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cellTotalLabel);

            document.add(table);
            document.add(new Paragraph("\n"));

            if (order.getShippingFee() != null) {
                Paragraph paragraph20 = new Paragraph("Tiền ship: " + Utils.formatBigDecimal(order.getShippingFee()) + " VND", fontParagraph);
                paragraph20.setAlignment(Element.ALIGN_LEFT);
                document.add(paragraph20);
            }

            Paragraph paragraph18 = new Paragraph("Tổng tiền phải thanh toán: " + Utils.formatBigDecimal(order.getTotalAmount()) + " VND", fontParagraph);
            paragraph18.setAlignment(Element.ALIGN_LEFT);
            document.add(paragraph18);

            if (tongTienKhachTra != BigDecimal.ZERO) {
                Paragraph paragraph13 = new Paragraph("Tổng tiền khách trả: " + Utils.formatBigDecimal(order.getTotalAmount().add(order.getShippingFee())) + " VND", fontParagraph);
                paragraph13.setAlignment(Element.ALIGN_LEFT);
                document.add(paragraph13);
            }

            document.add(new Paragraph("\n"));

            Paragraph paragraph17 = new Paragraph("----Cảm ơn quý khách !----", fontParagraph);
            paragraph17.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph17);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }
        }
        return CommonResponse.builder().build();
    }
}

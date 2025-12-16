package org.example.websitesalephone.service.pdf;

import jakarta.servlet.http.HttpServletResponse;
import org.example.websitesalephone.comon.CommonResponse;

import java.io.IOException;

public interface PDFGeneratorService {

    CommonResponse orderCouter(HttpServletResponse response, String id) throws IOException;

}

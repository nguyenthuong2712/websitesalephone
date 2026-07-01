package org.example.websitesalephone.config.init;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.entity.Color;
import org.example.websitesalephone.repository.ColorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.LinkedHashMap;

@Component
@RequiredArgsConstructor
public class ColorDataInitializer implements CommandLineRunner {

    private final ColorRepository colorRepository;

    @Override
    public void run(String... args) {
        Map<String, String> colors = new LinkedHashMap<>();
        colors.put("BLACK", "BLACK");
        colors.put("WHITE", "WHITE");
        colors.put("RED", "RED");
        colors.put("GREEN", "GREEN");
        colors.put("BLUE", "BLUE");
        colors.put("YELLOW", "YELLOW");
        colors.put("ORANGE", "ORANGE");
        colors.put("PURPLE", "PURPLE");
        colors.put("PINK", "PINK");
        colors.put("BROWN", "BROWN");
        colors.put("GREY", "GREY");
        colors.put("SILVER", "SILVER");
        colors.put("GOLD", "GOLD");
        colors.put("CYAN", "CYAN");
        colors.put("MAGENTA", "MAGENTA");
        colors.put("NAVY", "NAVY");
        colors.put("LIME", "LIME");
        colors.put("TEAL", "TEAL");
        colors.put("OLIVE", "OLIVE");
        colors.put("MAROON", "MAROON");
        colors.put("CORAL", "CORAL");
        colors.put("TURQUOISE", "TURQUOISE");
        colors.put("INDIGO", "INDIGO");
        colors.put("VIOLET", "VIOLET");
        colors.put("BEIGE", "BEIGE");
        colors.put("TAN", "TAN");
        colors.put("CHOCOLATE", "CHOCOLATE");
        colors.put("SALMON", "SALMON");
        colors.put("KHAKI", "KHAKI");
        colors.put("MINT", "MINT");
        colors.put("PEACH", "PEACH");
        colors.put("XANH_DAM", "XANH_DAM");
        colors.put("VANG_NHAT", "VANG_NHAT");
        colors.put("MAU_DEN", "MAU_DEN");
        colors.put("XANH_DA_TROI", "XANH_DA_TROI");

        for (Map.Entry<String, String> entry : colors.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue();
            boolean exists = colorRepository.existsById(id);
            if (!exists) {
                Color color = new Color();
                color.setId(id);
                color.setName(name);
                colorRepository.save(color);
            }
        }
    }
}

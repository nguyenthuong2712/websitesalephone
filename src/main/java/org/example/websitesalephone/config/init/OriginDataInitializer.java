package org.example.websitesalephone.config.init;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.entity.Origin;
import org.example.websitesalephone.repository.OriginRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OriginDataInitializer implements CommandLineRunner {

    private final OriginRepository originRepository;

    @Override
    public void run(String... args) {
        boolean exists = originRepository.findAll().stream()
                .anyMatch(o -> "mỹ".equalsIgnoreCase(o.getName().trim()) || "my".equalsIgnoreCase(o.getId().trim()));
        if (!exists) {
            Origin origin = new Origin();
            origin.setId("MY");
            origin.setName("Mỹ");
            originRepository.save(origin);
        }
    }
}

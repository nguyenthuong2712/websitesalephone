package org.example.websitesalephone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
    org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration.class
})
@EnableAsync
public class WebsiteSalePhoneApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsiteSalePhoneApplication.class, args);
    }

    @Bean
    public CommandLineRunner databaseMigration(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Cho phép cột is_read nhận NULL để tránh lỗi INSERT khi schema cũ vẫn giữ cột này
                try {
                    jdbcTemplate.execute("IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chat_messages') AND name = 'is_read') " +
                            "ALTER TABLE chat_messages ALTER COLUMN is_read BIT NULL;");
                } catch (Exception ex) {
                    System.err.println(">>> Unable to alter is_read column: " + ex.getMessage());
                }

                // Thêm các cột cho chat_messages nếu chưa tồn tại
                jdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chat_messages') AND name = 'status') " +
                        "ALTER TABLE chat_messages ADD status VARCHAR(20) NULL;");

                jdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chat_messages') AND name = 'delivered_at') " +
                        "ALTER TABLE chat_messages ADD delivered_at datetimeoffset NULL;");

                jdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('chat_messages') AND name = 'read_at') " +
                        "ALTER TABLE chat_messages ADD read_at datetimeoffset NULL;");

                // Cập nhật giá trị cũ
                jdbcTemplate.execute("UPDATE chat_messages SET status = 'SENT' WHERE status IS NULL;");

                System.out.println(">>> DATABASE CHAT_MESSAGES MIGRATION SUCCESSFUL <<<");
            } catch (Exception e) {
                System.err.println(">>> DATABASE MIGRATION ERROR: " + e.getMessage());
            }
        };
    }
}

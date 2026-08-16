package com.event.EventManage.config;

import com.event.EventManage.model.Role;
import com.event.EventManage.model.User;
import com.event.EventManage.model.Item;
import com.event.EventManage.model.SystemSetting;
import com.event.EventManage.repository.UserRepository;
import com.event.EventManage.repository.ItemRepository;
import com.event.EventManage.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@eventdeco.com").isEmpty()) {
            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("admin@eventdeco.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .phone("1234567890")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Default Admin user created! Email: admin@eventdeco.com | Password: Admin@123");
        }

        // Seed default WhatsApp number if missing
        if (systemSettingRepository.findBySettingKey("whatsapp_number").isEmpty()) {
            SystemSetting whatsappSetting = SystemSetting.builder()
                    .settingKey("whatsapp_number")
                    .settingValue("911234568751")
                    .description("Store WhatsApp number for client booking redirections (e.g. 919876543210)")
                    .build();
            systemSettingRepository.save(whatsappSetting);
            log.info("Default WhatsApp number setting seeded: 919876543210");
        }

        // Migrate old stock to totalQuantity and availableQuantity
        log.info("Checking if items need stock data migration...");
        List<Item> items = itemRepository.findAll();
        for (Item item : items) {
            boolean updated = false;
            if (item.getTotalQuantity() == null || item.getTotalQuantity() == 0) {
                Integer oldStock = item.getStock();
                item.setTotalQuantity(oldStock != null ? oldStock : 10);
                updated = true;
            }
            if (item.getAvailableQuantity() == null || item.getAvailableQuantity() == 0) {
                Integer oldStock = item.getStock();
                item.setAvailableQuantity(oldStock != null ? oldStock : 10);
                updated = true;
            }
            if (updated) {
                log.info("Migrated stock data for item: {} (Total: {}, Available: {})", item.getName(),
                        item.getTotalQuantity(), item.getAvailableQuantity());
                itemRepository.save(item);
            }
        }
    }
}

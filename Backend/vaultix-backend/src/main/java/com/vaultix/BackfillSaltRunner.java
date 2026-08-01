package com.vaultix;

import com.vaultix.entity.User;
import com.vaultix.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * One-off runner to backfill per-user salt values for existing users missing salt.
 *
 * This runner is disabled by default. To enable, set the property
 *   app.migration.backfill-salts=true
 * in the environment or application.properties. It will run once on application
 * startup and then exit normally.
 */
@Component
@ConditionalOnProperty(name = "app.migration.backfill-salts", havingValue = "true")
public class BackfillSaltRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BackfillSaltRunner.class);

    private final UserRepository userRepository;

    public BackfillSaltRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Retrieve all users and filter to those missing a salt (null or empty)
        List<User> all = userRepository.findAll();
        List<User> missing = all.stream()
                .filter(u -> u.getSalt() == null || u.getSalt().trim().isEmpty())
                .toList();

        if (missing.isEmpty()) {
            log.info("BackfillSaltRunner: no users require salt backfill");
            return;
        }

        SecureRandom sr = new SecureRandom();
        int count = 0;
        for (User u : missing) {
            byte[] saltBytes = new byte[16];
            sr.nextBytes(saltBytes);
            String saltBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);
            u.setSalt(saltBase64);
            userRepository.save(u);
            count++;
            log.info("Backfilled salt for userId={} email={}", u.getUserId(), u.getEmail());
        }

        log.info("BackfillSaltRunner: added salt for {} users", count);
    }
}
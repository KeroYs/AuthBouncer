package com.github.multidestroy;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.time.Instant;

public class PasswordHasher {

    private final int cores;
    private final Argon2 argon2;
    private final Config config;

    PasswordHasher(Config config) {
        this.config = config;
        this.cores = Runtime.getRuntime().availableProcessors();
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    public String hashPassword(String password) {
        Instant start = Instant.now();
        int iterations = config.get().getInt("argon2id.iterations");
        int memory = config.get().getInt("argon2id.memory");
        return argon2.hash(iterations, memory, 2 * cores, password.toCharArray());
    }

    public boolean checkPasswordCorrectness(String password, String hashedPassword) {
        return argon2.verify(hashedPassword, password.toCharArray());
    }
}

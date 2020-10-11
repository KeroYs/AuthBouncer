package com.github.multidestroy;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.time.Instant;

public class PasswordHasher {

    private int cores;
    private Argon2 argon2;
    private Config config;

    PasswordHasher(Config config) {
        this.config = config;
        this.cores = Runtime.getRuntime().availableProcessors();
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    public String hashPassword(String password) {
        Instant start = Instant.now();
        int iterations = config.get().getInt("argon2id.iterations");
        int memory = config.get().getInt("argon2id.memory");
        System.out.println("MEMORY: " + memory);
        String hash = argon2.hash(iterations, memory, 2 * cores, password.toCharArray());
        System.out.println(((float) (Instant.now().toEpochMilli() - start.toEpochMilli()))/1000 + " s");
        return hash;
    }

    public boolean checkPasswordCorrectness(String password, String hashedPassword) {
        return argon2.verify(hashedPassword, password.toCharArray());
    }
}

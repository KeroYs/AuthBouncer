package com.github.multidestroy;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.time.Instant;

public class PasswordHasher {

    private int cores;
    private Argon2 argon2;

    public PasswordHasher() {
        this.cores = Runtime.getRuntime().availableProcessors();
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    public String hashPassword(String password) {
        Instant start = Instant.now();
        String pass = argon2.hash(4, 512 * 1024, 2 * cores, password.toCharArray());
        System.out.println(((float) (Instant.now().toEpochMilli() - start.toEpochMilli()))/1000 + " s");
        return pass;
    }

    public boolean checkPasswordCorrectness(String password, String hashedPassword) {
        return argon2.verify(hashedPassword, password.toCharArray());
    }
}

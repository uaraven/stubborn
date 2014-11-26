package net.ninjacat.stubborn.test;

import java.util.Date;

public class Pojo {

    private final Date createdAt;

    public Pojo() {
        this.createdAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}

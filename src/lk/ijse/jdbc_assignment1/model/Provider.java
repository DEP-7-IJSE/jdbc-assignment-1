package lk.ijse.jdbc_assignment1.model;

import java.io.Serializable;

public class Provider implements Serializable {
    private int id;
    private String provider;

    public Provider() {
    }

    public Provider(int id, String name) {
        this.id = id;
        this.provider = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return this.provider;
    }
}

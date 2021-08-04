package lk.ijse.jdbc_assignment1.tm;

import java.io.Serializable;

public class ProviderTM implements Serializable {
    private int id;
    private String provider;

    public ProviderTM() {
    }

    public ProviderTM(int id, String name) {
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
        return "ProviderTM{" +
                "id=" + id +
                ", name='" + provider + '\'' +
                '}';
    }
}

package lk.ijse.jdbc_assignment1.tm;

import java.io.Serializable;

public class ProviderTM implements Serializable {
    private int id;
    private String name;

    public ProviderTM() {
    }

    public ProviderTM(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ProviderTM{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }
}

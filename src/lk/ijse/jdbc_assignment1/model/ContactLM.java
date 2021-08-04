package lk.ijse.jdbc_assignment1.model;

import java.io.Serializable;

public class ContactLM implements Serializable {
    private String contact;
    private int providerId;
    private String providerDescription;

    public ContactLM() {
    }

    public ContactLM(String contact, int providerId, String providerDescription) {
        this.contact = contact;
        this.providerId = providerId;
        this.providerDescription = providerDescription;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderDescription() {
        return providerDescription;
    }

    public void setProviderDescription(String providerDescription) {
        this.providerDescription = providerDescription;
    }

    @Override
    public String toString() {
        return this.contact;
    }
}

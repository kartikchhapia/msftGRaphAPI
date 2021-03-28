package com.microsoft.azure.msalwebsample;

public class Email {
    String fromEmailAddress;
    String toEmailAddress;
    int numberOfExchanges;

    public Email(String fromEmailAddress, String toEmailAddress, int numberOfExchanges) {
        this.fromEmailAddress = fromEmailAddress;
        this.toEmailAddress = toEmailAddress;
        this.numberOfExchanges = numberOfExchanges;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public String getToEmailAddress() {
        return toEmailAddress;
    }

    public int getNumberOfExchanges() {
        return numberOfExchanges;
    }


}

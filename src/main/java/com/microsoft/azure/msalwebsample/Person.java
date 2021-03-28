package com.microsoft.azure.msalwebsample;

public class Person {
    String name;
    String emailAddress;
    Integer meetingsTotal;
    Integer meetingsAcepted;
    double meetingTimeTotal;
    double meetingTimeAccepted;

    public Person(String name, String emailAddress, Integer meetingsTotal, Integer meetingsAcepted, double meetingTimeTotal, double meetingTimeAccepted) {
        this.name = name;
        this.emailAddress = emailAddress;
        this.meetingsTotal = meetingsTotal;
        this.meetingsAcepted = meetingsAcepted;
        this.meetingTimeTotal = meetingTimeTotal;
        this.meetingTimeAccepted = meetingTimeAccepted;
    }
    public String getName() {
        return name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Integer getMeetingsTotal() {
        return meetingsTotal;
    }

    public Integer getMeetingsAcepted() {
        return meetingsAcepted;
    }

    public double getMeetingTimeTotal() {
        return meetingTimeTotal;
    }

    public double getMeetingTimeAccepted() {
        return meetingTimeAccepted;
    }

}

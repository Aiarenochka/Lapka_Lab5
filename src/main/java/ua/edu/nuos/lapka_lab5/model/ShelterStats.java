package ua.edu.nuos.lapka_lab5.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShelterStats {
    private String shelterName;
    private String city;
    private int total;
    private int available;
    private int reserved;
    private int adopted;
    private int archived;
    private int newApplications;
}
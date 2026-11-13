package ua.edu.nuos.lapka_lab5.model;

import lombok.*;

@Data
@AllArgsConstructor
public class Pet {
    private int id;
    private int shelterId;
    private String shelterName;
    private String name;
    private String species;
    private String sex;
    private String breed;
    private int ageMonths;
    private String description;
    private String status;
}
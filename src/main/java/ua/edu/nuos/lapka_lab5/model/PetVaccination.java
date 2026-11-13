package ua.edu.nuos.lapka_lab5.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PetVaccination {
    private int petId;
    private String petName;
    private String shelterName;
    private LocalDate lastVaccination;
}
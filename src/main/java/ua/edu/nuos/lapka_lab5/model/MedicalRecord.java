package ua.edu.nuos.lapka_lab5.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MedicalRecord {
    private int id;
    private int petId;
    private String recordType;
    private String procedureName;
    private LocalDate recordDate;
    private String notes;
}
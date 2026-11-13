package ua.edu.nuos.lapka_lab5.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdoptionApplication {
    private int id;
    private int petId;
    private String petName;
    private String fullName;
    private String phone;
    private String reason;
    private String status;
    private LocalDateTime submittedAt;

    @Override
    public String toString() {
        return "№" + id + " — " + petName + ", " + fullName;
    }
}
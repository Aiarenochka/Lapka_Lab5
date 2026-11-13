package ua.edu.nuos.lapka_lab5.model;

import lombok.*;

@Data
@AllArgsConstructor
public class Shelter {
    private int id;
    private String name;
    private String city;
    private String phone;
    private String email;
    private String address;

    @Override
    public String toString() {
        return name + " (" + city + ")";
    }
}
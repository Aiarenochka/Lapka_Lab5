package ua.edu.nuos.lapka_lab5.dao;

import ua.edu.nuos.lapka_lab5.config.DatabaseConfig;
import ua.edu.nuos.lapka_lab5.model.MedicalRecord;
import ua.edu.nuos.lapka_lab5.model.Pet;
import ua.edu.nuos.lapka_lab5.model.PetVaccination;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PetDao implements CrudDao<Pet> {
    private final DatabaseConfig databaseConfig;

    public PetDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public List<Pet> findAll() throws SQLException {
        String sql = """
                select p.*, s.name as shelter_name
                from pets p
                join shelters s on s.shelter_id = p.shelter_id
                order by p.pet_id
                """;
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pets.add(mapRowToPet(rs));
            }
        }
        return pets;
    }

    @Override
    public boolean create(Pet pet) throws SQLException {
        String sql = """
                insert into pets (shelter_id, name, species, sex, breed, age_months, description, status)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setValues(ps, pet);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Pet pet) throws SQLException {
        String sql = """
                update pets
                set shelter_id = ?, name = ?, species = ?, sex = ?, breed = ?,
                    age_months = ?, description = ?, status = ?
                where pet_id = ?
                """;
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setValues(ps, pet);
            ps.setInt(9, pet.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "delete from pets where pet_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Pet> findAvailable(String species, String city) throws SQLException {
        String sql = """
                select p.*, s.name as shelter_name
                from pets p
                join shelters s on s.shelter_id = p.shelter_id
                where p.species = ? and p.status = 'available' and s.city = ?
                order by p.name
                """;
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, species);
            ps.setString(2, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pets.add(mapRowToPet(rs));
                }
            }
        }
        return pets;
    }

    public Pet findById(int petId) throws SQLException {
        String sql = """
                select p.*, s.name as shelter_name
                from pets p
                join shelters s on s.shelter_id = p.shelter_id
                where p.pet_id = ?
                """;
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPet(rs);
                }
                return null;
            }
        }
    }

    public List<MedicalRecord> findMedicalRecords(int petId) throws SQLException {
        String sql = """
                select *
                from medical_records
                where pet_id = ?
                order by record_date desc
                """;
        List<MedicalRecord> records = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new MedicalRecord(
                            rs.getInt("record_id"),
                            rs.getInt("pet_id"),
                            rs.getString("record_type"),
                            rs.getString("procedure_name"),
                            rs.getDate("record_date").toLocalDate(),
                            rs.getString("notes")
                    ));
                }
            }
        }
        return records;
    }

    public List<PetVaccination> findNeedingVaccination(int months) throws SQLException {
        String sql = """
                select p.pet_id, p.name, s.name as shelter, max(m.record_date) as last_vaccination
                from pets p
                join shelters s on s.shelter_id = p.shelter_id
                left join medical_records m on m.pet_id = p.pet_id and m.record_type = 'vaccination'
                where p.status in ('available', 'reserved')
                group by p.pet_id, p.name, s.name
                having last_vaccination is null
                    or last_vaccination < date_sub(curdate(), interval ? month)
                order by last_vaccination
                """;
        List<PetVaccination> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date last = rs.getDate("last_vaccination");
                    list.add(new PetVaccination(
                            rs.getInt("pet_id"),
                            rs.getString("name"),
                            rs.getString("shelter"),
                            last == null ? null : last.toLocalDate()
                    ));
                }
            }
        }
        return list;
    }

    private void setValues(PreparedStatement ps, Pet pet) throws SQLException {
        ps.setInt(1, pet.getShelterId());
        ps.setString(2, pet.getName());
        ps.setString(3, pet.getSpecies());
        ps.setString(4, pet.getSex());
        ps.setString(5, pet.getBreed());
        ps.setInt(6, pet.getAgeMonths());
        ps.setString(7, pet.getDescription());
        ps.setString(8, pet.getStatus());
    }

    private Pet mapRowToPet(ResultSet rs) throws SQLException {
        return new Pet(
                rs.getInt("pet_id"),
                rs.getInt("shelter_id"),
                rs.getString("shelter_name"),
                rs.getString("name"),
                rs.getString("species"),
                rs.getString("sex"),
                rs.getString("breed"),
                rs.getInt("age_months"),
                rs.getString("description"),
                rs.getString("status")
        );
    }
}
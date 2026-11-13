package ua.edu.nuos.lapka_lab5.dao;

import ua.edu.nuos.lapka_lab5.config.DatabaseConfig;
import ua.edu.nuos.lapka_lab5.model.Shelter;
import ua.edu.nuos.lapka_lab5.model.ShelterStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShelterDao implements CrudDao<Shelter> {
    private final DatabaseConfig databaseConfig;

    public ShelterDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public List<Shelter> findAll() throws SQLException {
        String sql = "select * from shelters order by shelter_id";
        List<Shelter> shelters = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                shelters.add(new Shelter(
                        rs.getInt("shelter_id"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        }
        return shelters;
    }

    @Override
    public boolean create(Shelter shelter) throws SQLException {
        String sql = "insert into shelters (name, city, phone, email, address) values (?, ?, ?, ?, ?)";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setValues(ps, shelter);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Shelter shelter) throws SQLException {
        String sql = """
                update shelters
                set name = ?, city = ?, phone = ?, email = ?, address = ?
                where shelter_id = ?
                """;
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setValues(ps, shelter);
            ps.setInt(6, shelter.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "delete from shelters where shelter_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<String> findCities() throws SQLException {
        String sql = "select distinct city from shelters order by city";
        List<String> cities = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cities.add(rs.getString("city"));
            }
        }
        return cities;
    }

    public List<ShelterStats> getStatistics() throws SQLException {
        String sql = """
                select s.name, s.city,
                       count(p.pet_id) as total,
                       count(case when p.status = 'available' then 1 end) as available,
                       count(case when p.status = 'reserved' then 1 end) as reserved,
                       count(case when p.status = 'adopted' then 1 end) as adopted,
                       count(case when p.status = 'archived' then 1 end) as archived,
                       (select count(*)
                        from adoption_applications a
                        join pets p2 on p2.pet_id = a.pet_id
                        where p2.shelter_id = s.shelter_id and a.status = 'new') as new_applications
                from shelters s
                left join pets p on p.shelter_id = s.shelter_id
                group by s.shelter_id, s.name, s.city
                order by total desc
                """;
        List<ShelterStats> stats = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.add(new ShelterStats(
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getInt("total"),
                        rs.getInt("available"),
                        rs.getInt("reserved"),
                        rs.getInt("adopted"),
                        rs.getInt("archived"),
                        rs.getInt("new_applications")
                ));
            }
        }
        return stats;
    }

    private void setValues(PreparedStatement ps, Shelter shelter) throws SQLException {
        ps.setString(1, shelter.getName());
        ps.setString(2, shelter.getCity());
        ps.setString(3, shelter.getPhone());
        ps.setString(4, shelter.getEmail());
        ps.setString(5, shelter.getAddress());
    }
}
package ua.edu.nuos.lapka_lab5.dao;

import ua.edu.nuos.lapka_lab5.config.DatabaseConfig;
import ua.edu.nuos.lapka_lab5.model.AdoptionApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDao {
    private final DatabaseConfig databaseConfig;

    public ApplicationDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public int submit(int petId, String fullName, String phone, String reason) throws SQLException {
        String checkSql = "select name, status from pets where pet_id = ? for update";
        String insertSql = """
                insert into adoption_applications (pet_id, full_name, phone, reason)
                values (?, ?, ?, ?)
                """;
        String reserveSql = "update pets set status = 'reserved' where pet_id = ? and status = 'available'";

        try (Connection conn = databaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql);
                 PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement reservePs = conn.prepareStatement(reserveSql)) {

                checkPs.setInt(1, petId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Тварину з id " + petId + " не знайдено");
                    }
                    String status = rs.getString("status");
                    if (!status.equals("available") && !status.equals("reserved")) {
                        throw new RuntimeException(rs.getString("name") + " вже знайшов(-ла) дім");
                    }
                }

                insertPs.setInt(1, petId);
                insertPs.setString(2, fullName);
                insertPs.setString(3, phone);
                insertPs.setString(4, reason);
                insertPs.executeUpdate();
                int applicationId;
                try (ResultSet keys = insertPs.getGeneratedKeys()) {
                    keys.next();
                    applicationId = keys.getInt(1);
                }

                reservePs.setInt(1, petId);
                reservePs.executeUpdate();

                conn.commit();
                return applicationId;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<AdoptionApplication> findNew() throws SQLException {
        String sql = """
                select a.*, p.name as pet_name
                from adoption_applications a
                join pets p on p.pet_id = a.pet_id
                where a.status = 'new'
                order by a.submitted_at
                """;
        List<AdoptionApplication> list = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AdoptionApplication(
                        rs.getInt("application_id"),
                        rs.getInt("pet_id"),
                        rs.getString("pet_name"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getTimestamp("submitted_at").toLocalDateTime()
                ));
            }
        }
        return list;
    }

    public int approve(int applicationId) throws SQLException {
        String checkSql = """
                select a.pet_id, a.status as app_status, p.status as pet_status
                from adoption_applications a
                join pets p on p.pet_id = a.pet_id
                where a.application_id = ?
                for update
                """;
        String approveSql = "update adoption_applications set status = 'approved' where application_id = ?";
        String adoptSql = "update pets set status = 'adopted', adopted_at = curdate() where pet_id = ?";
        String rejectSql = """
                update adoption_applications set status = 'rejected'
                where pet_id = ? and status = 'new' and application_id <> ?
                """;

        try (Connection conn = databaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql);
                 PreparedStatement approvePs = conn.prepareStatement(approveSql);
                 PreparedStatement adoptPs = conn.prepareStatement(adoptSql);
                 PreparedStatement rejectPs = conn.prepareStatement(rejectSql)) {

                int petId;
                checkPs.setInt(1, applicationId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Анкету №" + applicationId + " не знайдено");
                    }
                    if (!rs.getString("app_status").equals("new")) {
                        throw new RuntimeException("Анкету №" + applicationId + " вже розглянуто");
                    }
                    String petStatus = rs.getString("pet_status");
                    if (!petStatus.equals("available") && !petStatus.equals("reserved")) {
                        throw new RuntimeException("Тварина вже не в притулку");
                    }
                    petId = rs.getInt("pet_id");
                }

                approvePs.setInt(1, applicationId);
                approvePs.executeUpdate();

                adoptPs.setInt(1, petId);
                adoptPs.executeUpdate();

                rejectPs.setInt(1, petId);
                rejectPs.setInt(2, applicationId);
                int rejected = rejectPs.executeUpdate();

                conn.commit();
                return rejected;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
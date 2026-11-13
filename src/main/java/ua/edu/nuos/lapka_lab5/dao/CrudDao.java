package ua.edu.nuos.lapka_lab5.dao;

import java.sql.SQLException;
import java.util.List;

public interface CrudDao<T> {
    List<T> findAll() throws SQLException;

    boolean create(T entity) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(int id) throws SQLException;
}
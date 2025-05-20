package com.example.demo.dao;

import com.example.demo.model.Exam;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    private static final Dotenv dotenv = Dotenv.load();

    private final String jdbcURL = dotenv.get("DB_URL");
    private final String jdbcUsername = dotenv.get("DB_USERNAME");
    private final String jdbcPassword = dotenv.get("DB_PASSWORD");

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    // Lấy danh sách đề thi
    public List<Exam> getAllExams() {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT * FROM Exam ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Exam exam = new Exam();
                exam.setId(rs.getInt("id"));
                exam.setName(rs.getString("name"));
                exam.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(exam);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Thêm đề thi mới
    public int insertExam(Exam exam) {
        String sql = "INSERT INTO Exam (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, exam.getName());
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Tạo đề thi thất bại, không có hàng nào bị ảnh hưởng.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    exam.setId(newId); // Nếu cần gán id vào object
                    return newId;
                } else {
                    throw new SQLException("Tạo đề thi thất bại, không lấy được ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Cập nhật đề thi
    public boolean updateExam(Exam exam) {
        String sql = "UPDATE Exam SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, exam.getName());
            ps.setInt(2, exam.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa đề thi
    public boolean deleteExam(int id) {
        String sql = "DELETE FROM Exam WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

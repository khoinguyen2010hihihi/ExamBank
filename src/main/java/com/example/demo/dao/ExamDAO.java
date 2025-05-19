package com.example.demo.dao;

import com.example.demo.model.Exam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    private final String jdbcURL = "jdbc:mysql://localhost:3306/exam_bank?useSSL=false&serverTimezone=UTC";
    private final String jdbcUsername = "root";
    private final String jdbcPassword = "123456";

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
    public boolean insertExam(Exam exam) {
        String sql = "INSERT INTO Exam (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, exam.getName());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

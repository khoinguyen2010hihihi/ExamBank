package com.example.demo.dao;

import com.example.demo.model.ExamQuestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamQuestionDAO {

    private final String jdbcURL = "jdbc:mysql://localhost:3306/exam_bank?useSSL=false&serverTimezone=UTC";
    private final String jdbcUsername = "root";
    private final String jdbcPassword = "123456";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    // Lấy danh sách câu hỏi của đề thi
    public List<Integer> getQuestionIdsByExamId(int examId) {
        List<Integer> questionIds = new ArrayList<>();
        String sql = "SELECT question_id FROM Exam_Question WHERE exam_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                questionIds.add(rs.getInt("question_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questionIds;
    }

    // Thêm câu hỏi vào đề thi
    public boolean insertExamQuestion(ExamQuestion eq) {
        String sql = "INSERT INTO Exam_Question (exam_id, question_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eq.getExamId());
            ps.setInt(2, eq.getQuestionId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa câu hỏi khỏi đề thi
    public boolean deleteExamQuestion(int examId, int questionId) {
        String sql = "DELETE FROM Exam_Question WHERE exam_id = ? AND question_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            ps.setInt(2, questionId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

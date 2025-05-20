package com.example.demo.dao;

import com.example.demo.model.Answer;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerDAO {

    private static final Dotenv dotenv = Dotenv.load();

    private final String jdbcURL = dotenv.get("DB_URL");
    private final String jdbcUsername = dotenv.get("DB_USERNAME");
    private final String jdbcPassword = dotenv.get("DB_PASSWORD");

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    // Lấy danh sách đáp án của câu hỏi
    public List<Answer> getAnswersByQuestionId(int questionId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM Answer WHERE question_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Answer a = new Answer();
                a.setId(rs.getInt("id"));
                a.setQuestionId(rs.getInt("question_id"));
                a.setContent(rs.getString("content"));
                a.setCorrect(rs.getBoolean("is_correct"));
                answers.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    // Thêm đáp án mới
    public boolean insertAnswer(Answer answer) {
        String sql = "INSERT INTO Answer (question_id, content, is_correct) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, answer.getQuestionId());
            ps.setString(2, answer.getContent());
            ps.setBoolean(3, answer.isCorrect());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật đáp án
    public boolean updateAnswer(Answer answer) {
        String sql = "UPDATE Answer SET content = ?, is_correct = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, answer.getContent());
            ps.setBoolean(2, answer.isCorrect());
            ps.setInt(3, answer.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa đáp án theo id
    public boolean deleteAnswer(int id) {
        String sql = "DELETE FROM Answer WHERE id = ?";
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

package com.example.demo.dao;

import com.example.demo.model.Question;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    private static final Dotenv dotenv = Dotenv.load();

    private final String jdbcURL = dotenv.get("DB_URL");
    private final String jdbcUsername = dotenv.get("DB_USERNAME");
    private final String jdbcPassword = dotenv.get("DB_PASSWORD");

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    // Lấy danh sách tất cả câu hỏi
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Question";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt("id"));
                q.setContent(rs.getString("content"));
                q.setType(rs.getString("type"));
                q.setMediaPath(rs.getString("media_path"));
                q.setLevel(rs.getString("level"));
                questions.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;
    }

    // Thêm câu hỏi mới
    public boolean insertQuestion(Question question) {
        String sql = "INSERT INTO Question (content, type, media_path, level) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, question.getContent());
            ps.setString(2, question.getType());
            ps.setString(3, question.getMediaPath());
            ps.setString(4, question.getLevel());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật câu hỏi
    public boolean updateQuestion(Question question) {
        String sql = "UPDATE Question SET content = ?, type = ?, media_path = ?, level = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, question.getContent());
            ps.setString(2, question.getType());
            ps.setString(3, question.getMediaPath());
            ps.setString(4, question.getLevel());
            ps.setInt(5, question.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa câu hỏi theo id
    public boolean deleteQuestion(int id) {
        String sql = "DELETE FROM Question WHERE id = ?";
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

package com.example.demo.controller;

import com.example.demo.ai.HuggingFaceClient;
import com.example.demo.dao.AnswerDAO;
import com.example.demo.model.Answer;
import com.example.demo.model.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class AnswerFormController {

    @FXML private TableView<Answer> tableAnswers;
    @FXML private TableColumn<Answer, Integer> colId;
    @FXML private TableColumn<Answer, String> colContent;
    @FXML private TableColumn<Answer, Boolean> colCorrect;

    @FXML private TextArea txtContent;
    @FXML private CheckBox cbIsCorrect;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    private AnswerDAO answerDAO = new AnswerDAO();
    private ObservableList<Answer> answerList;
    private Answer selectedAnswer = null;
    private HuggingFaceClient hfClient = new HuggingFaceClient();
    private Question currentQuestion;

    public void setCurrentQuestion(Question question) {
        this.currentQuestion = question;
        loadAnswers();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colContent.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        colCorrect.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isCorrect()));

        btnAdd.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tableAnswers.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        fillForm(newSelection);
                        selectedAnswer = newSelection;
                        btnAdd.setDisable(true);
                        btnUpdate.setDisable(false);
                        btnDelete.setDisable(false);
                    } else {
                        clearForm();
                    }
                });
    }

    private void loadAnswers() {
        if (currentQuestion == null) {
            answerList = FXCollections.observableArrayList();
        } else {
            List<Answer> list = answerDAO.getAnswersByQuestionId(currentQuestion.getId());
            answerList = FXCollections.observableArrayList(list);
        }
        tableAnswers.setItems(answerList);
    }

    private void fillForm(Answer answer) {
        txtContent.setText(answer.getContent());
        cbIsCorrect.setSelected(answer.isCorrect());
    }

    @FXML
    private void handleAddAnswer(ActionEvent event) {
        if (currentQuestion == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn câu hỏi trước");
            return;
        }

        String content = txtContent.getText().trim();
        boolean isCorrect = cbIsCorrect.isSelected();

        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Nội dung đáp án không được để trống");
            return;
        }

        Answer answer = new Answer();
        answer.setQuestionId(currentQuestion.getId());
        answer.setContent(content);
        answer.setCorrect(isCorrect);

        boolean success = answerDAO.insertAnswer(answer);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm đáp án thành công");
            clearForm();
            loadAnswers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm đáp án thất bại");
        }
    }

    @FXML
    private void handleUpdateAnswer(ActionEvent event) {
        if (selectedAnswer == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đáp án để cập nhật");
            return;
        }

        String content = txtContent.getText().trim();
        boolean isCorrect = cbIsCorrect.isSelected();

        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Nội dung đáp án không được để trống");
            return;
        }

        selectedAnswer.setContent(content);
        selectedAnswer.setCorrect(isCorrect);

        boolean success = answerDAO.updateAnswer(selectedAnswer);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật đáp án thành công");
            clearForm();
            loadAnswers();
            selectedAnswer = null;
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật đáp án thất bại");
        }
    }

    @FXML
    private void handleDeleteAnswer(ActionEvent event) {
        Answer answer = tableAnswers.getSelectionModel().getSelectedItem();
        if (answer == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đáp án cần xóa");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn chắc chắn muốn xóa đáp án này?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận xóa");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            boolean success = answerDAO.deleteAnswer(answer.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa đáp án thành công");
                loadAnswers();
                clearForm();
                selectedAnswer = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa đáp án thất bại");
            }
        }
    }

    @FXML
    private void handleCreateAnswersByAI() {
        if (currentQuestion == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn câu hỏi trước khi tạo đáp án AI");
            return;
        }

        String questionContent = currentQuestion.getContent();
        if (questionContent == null || questionContent.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Câu hỏi chưa có nội dung để tạo đáp án");
            return;
        }

        // Tạo prompt rõ ràng cho AI
        String prompt = "Hãy tạo chính xác 4 đáp án trắc nghiệm duy nhất cho câu hỏi sau: \"" + questionContent + "\".\n" +
                "Chỉ trả về 4 dòng, mỗi dòng là một đáp án, bắt đầu bằng số thứ tự 1., 2., 3., 4. Trong đó có đúng 1 đáp án đúng, đánh dấu bằng dấu * ngay sau số thứ tự.(NGAY SAU SỐ THỨ TỰ NHÉ VD: 2.* までに)\n" +
                "Không được lặp lại, không giải thích thêm, không chú thích sau câu trả lời (CHỈ CẦN ĐÁP ÁN NHÉ).\n" +
                "Ví dụ:\n" +
                "1. đáp án sai\n" +
                "2.* đáp án đúng\n" +
                "3. đáp án sai\n" +
                "4. đáp án sai\n" +
                "Chỉ trả về đáp án, không trả lời thêm câu nào khác.";

        new Thread(() -> {
            try {
                String aiResponse = hfClient.getAnswerSuggestion(prompt);

                // Phân tích kết quả để lấy từng đáp án
                List<Answer> generatedAnswers = parseAIAnswers(aiResponse);

                // Xóa đáp án cũ trong DB của câu hỏi này trước (nếu muốn)
                answerDAO.getAnswersByQuestionId(currentQuestion.getId()).forEach(ans -> {
                    answerDAO.deleteAnswer(ans.getId());
                });

                // Thêm đáp án mới từ AI
                for (Answer a : generatedAnswers) {
                    a.setQuestionId(currentQuestion.getId());
                    answerDAO.insertAnswer(a);
                }

                // Load lại đáp án lên TableView
                javafx.application.Platform.runLater(() -> {
                    loadAnswers();
                    clearForm();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "AI đã tạo 4 đáp án, trong đó có 1 đáp án đúng.");
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi AI", "Gọi API AI thất bại: " + e.getMessage()));
            }
        }).start();
    }

    private List<Answer> parseAIAnswers(String aiResponse) {
        List<Answer> answers = new ArrayList<>();
        if (aiResponse == null || aiResponse.isEmpty()) return answers;

        String[] lines = aiResponse.split("\\r?\\n");
        int count = 0;

        for (String line : lines) {
            if (count >= 4) break;  // Giới hạn tối đa 4 câu

            line = line.trim();
            if (line.isEmpty()) continue;

            if (!line.matches("^\\d+\\.\\s*.*")) continue;

            String content = line.replaceAll("^\\d+\\.\\s*", "");
            if (content.startsWith("<") && content.endsWith(">")) continue;
            if (content.toLowerCase().contains("think")) continue;

            boolean isCorrect = false;
            if (content.startsWith("*")) {
                isCorrect = true;
                content = content.substring(1).trim();
            }

            // Bỏ trùng đáp án (nếu muốn)
            boolean duplicate = false;
            for (Answer a : answers) {
                if (a.getContent().equalsIgnoreCase(content)) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) continue;

            if (content.length() < 1) continue;

            Answer ans = new Answer();
            ans.setContent(content);
            ans.setCorrect(isCorrect);
            answers.add(ans);

            count++;
        }
        return answers;
    }


    private void clearForm() {
        txtContent.clear();
        cbIsCorrect.setSelected(false);
        btnAdd.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        tableAnswers.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

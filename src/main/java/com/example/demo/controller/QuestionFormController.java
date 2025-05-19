package com.example.demo.controller;

import com.example.demo.ai.HuggingFaceClient;
import com.example.demo.dao.QuestionDAO;
import com.example.demo.model.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class QuestionFormController {

    @FXML private TextArea txtContent;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtMediaPath;
    @FXML private ComboBox<String> cbLevel;
    @FXML private TableView<Question> tableQuestions;
    @FXML private TableColumn<Question, Integer> colId;
    @FXML private TableColumn<Question, String> colContent;
    @FXML private TableColumn<Question, String> colType;
    @FXML private TableColumn<Question, String> colLevel;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private TextArea txtSuggestedAnswers;
    @FXML private TextArea txtAIPrompt;


    private QuestionDAO questionDAO = new QuestionDAO();
    private ObservableList<Question> questionList;
    private Question selectedQuestion = null;
    private HuggingFaceClient hfClient = new HuggingFaceClient();


    @FXML
    public void initialize() {
        cbType.getItems().addAll("text", "audio", "image");
        cbType.getSelectionModel().selectFirst();

        cbLevel.getItems().addAll("N5", "N4", "N3", "N2", "N1");
        cbLevel.getSelectionModel().selectFirst();

        // Cấu hình cột TableView
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colContent.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        colType.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        colLevel.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLevel()));

        loadQuestions();

        // Khi chưa chọn câu hỏi: chỉ bật nút Thêm, tắt Update, Delete
        btnAdd.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Xử lý sự kiện chọn dòng TableView
        tableQuestions.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        fillForm(newSelection);
                        selectedQuestion = newSelection;
                        btnAdd.setDisable(true);
                        btnUpdate.setDisable(false);
                        btnDelete.setDisable(false);
                    } else {
                        clearForm();
                    }
                });
    }

    private void loadQuestions() {
        List<Question> list = questionDAO.getAllQuestions();
        questionList = FXCollections.observableArrayList(list);
        tableQuestions.setItems(questionList);
    }

    private void fillForm(Question q) {
        txtContent.setText(q.getContent());
        cbType.setValue(q.getType());
        txtMediaPath.setText(q.getMediaPath() != null ? q.getMediaPath() : "");
        cbLevel.setValue(q.getLevel());
    }

    @FXML
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        Window stage = txtContent.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            txtMediaPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleAddQuestion(ActionEvent event) {
        String content = txtContent.getText().trim();
        String type = cbType.getValue();
        String mediaPath = txtMediaPath.getText().trim();
        String level = cbLevel.getValue();

        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Nội dung câu hỏi không được để trống");
            return;
        }

        Question q = new Question();
        q.setContent(content);
        q.setType(type);
        q.setMediaPath(mediaPath.isEmpty() ? null : mediaPath);
        q.setLevel(level);

        boolean success = questionDAO.insertQuestion(q);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm câu hỏi thành công");
            clearForm();
            loadQuestions();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm câu hỏi thất bại");
        }
    }

    @FXML
    private void handleUpdateQuestion(ActionEvent event) {
        if (selectedQuestion == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn câu hỏi để cập nhật");
            return;
        }

        String content = txtContent.getText().trim();
        String type = cbType.getValue();
        String mediaPath = txtMediaPath.getText().trim();
        String level = cbLevel.getValue();

        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Nội dung câu hỏi không được để trống");
            return;
        }

        selectedQuestion.setContent(content);
        selectedQuestion.setType(type);
        selectedQuestion.setMediaPath(mediaPath.isEmpty() ? null : mediaPath);
        selectedQuestion.setLevel(level);

        boolean success = questionDAO.updateQuestion(selectedQuestion);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật câu hỏi thành công");
            clearForm();
            loadQuestions();
            selectedQuestion = null;
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật câu hỏi thất bại");
        }
    }

    @FXML
    private void handleDeleteQuestion(ActionEvent event) {
        Question q = tableQuestions.getSelectionModel().getSelectedItem();
        if (q == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn câu hỏi cần xóa");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn chắc chắn muốn xóa câu hỏi này?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận xóa");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            boolean success = questionDAO.deleteQuestion(q.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa câu hỏi thành công");
                loadQuestions();
                clearForm();
                selectedQuestion = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa câu hỏi thất bại");
            }
        }
    }

    @FXML
    private void handleOpenAnswerManager() {
        Question selected = tableQuestions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn câu hỏi để quản lý đáp án");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/answer-form.fxml"));
            Parent root = loader.load();

            // Lấy controller của cửa sổ quản lý đáp án
            AnswerFormController controller = loader.getController();
            controller.setCurrentQuestion(selected);

            Stage stage = new Stage();
            stage.setTitle("Quản lý đáp án cho câu hỏi ID: " + selected.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn cửa sổ chính khi mở
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ quản lý đáp án");
        }
    }

    @FXML
    private void handleOpenExamForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/exam-form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Quản lý Đề thi");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình Quản lý đề thi");
        }
    }

    @FXML
    private void handleSuggestAnswer() {
        String content = txtContent.getText().trim();
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập nội dung câu hỏi trước.");
            return;
        }

        new Thread(() -> {
            try {
                String prompt = "Hãy đưa ra 4 đáp án trắc nghiệm cho câu hỏi sau (chỉ nội dung, không giải thích): " + content;
                String result = hfClient.getAnswerSuggestion(prompt);
                javafx.application.Platform.runLater(() -> txtSuggestedAnswers.setText(result));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi AI", e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCreateQuestionByAI() {
        // Lấy prompt từ UI nếu có, hoặc dùng prompt mặc định
        String prompt = (txtAIPrompt != null && !txtAIPrompt.getText().trim().isEmpty())
                ? txtAIPrompt.getText().trim()
                : "Hãy tạo một câu hỏi trắc nghiệm tiếng Nhật cấp độ N5 duy nhất, chỉ trả về câu hỏi bằng tiếng Nhật hoặc tiếng Nhật kèm tiếng Việt giải thích, không giải thích thêm, không liệt kê đáp án, chỉ một câu hỏi duy nhất.";

        final String finalPrompt = prompt + " Chỉ trả về 1 câu hỏi duy nhất, không thêm giải thích hay đáp án.";

        new Thread(() -> {
            try {
                String aiResponse = hfClient.getAnswerSuggestion(finalPrompt);

                // Lọc lấy dòng đầu tiên có dấu hỏi "？" hoặc "?" làm câu hỏi
                String question = extractSingleQuestion(aiResponse);

                javafx.application.Platform.runLater(() -> {
                    txtContent.setText(question);
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "AI đã tạo câu hỏi duy nhất. Bạn có thể chỉnh sửa rồi lưu.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi AI", "Gọi API AI thất bại: " + e.getMessage()));
            }
        }).start();
    }

    /********************************************************
     *  trích xuất câu hỏi duy nhất từ chuỗi trả về của AI  *
     ********************************************************/
    private String extractSingleQuestion(String text) {
        if (text == null || text.isEmpty()) return "";

        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.endsWith("？") || line.endsWith("?")) {
                return line;
            }
        }
        // Nếu không tìm thấy dòng có dấu hỏi, lấy dòng đầu tiên
        return text.split("\n")[0].trim();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        txtContent.clear();
        txtMediaPath.clear();
        cbType.getSelectionModel().selectFirst();
        cbLevel.getSelectionModel().selectFirst();
        selectedQuestion = null;
        btnAdd.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        tableQuestions.getSelectionModel().clearSelection();
    }
}

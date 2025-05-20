package com.example.demo.controller;

import com.example.demo.dao.AnswerDAO;
import com.example.demo.dao.ExamDAO;
import com.example.demo.dao.ExamQuestionDAO;
import com.example.demo.dao.QuestionDAO;
import com.example.demo.model.Answer;
import com.example.demo.model.Exam;
import com.example.demo.model.ExamQuestion;
import com.example.demo.model.Question;
import com.example.demo.util.DocxExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExamFormController {

    @FXML private TableView<Exam> tableExams;
    @FXML private TableColumn<Exam, Integer> colExamId;
    @FXML private TableColumn<Exam, String> colExamName;

    @FXML private TextField txtExamName;

    @FXML private TableView<Question> tableExamQuestions;
    @FXML private TableColumn<Question, Integer> colQuesId;
    @FXML private TableColumn<Question, String> colQuesContent;

    private ExamDAO examDAO = new ExamDAO();
    private ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
    private QuestionDAO questionDAO = new QuestionDAO();
    private AnswerDAO answerDAO = new AnswerDAO();

    private ObservableList<Exam> examList;
    private ObservableList<Question> examQuestionList;

    private Exam selectedExam = null;

    @FXML
    public void initialize() {
        // Cấu hình cột exam
        colExamId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colExamName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        // Cột câu hỏi trong đề thi
        colQuesId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colQuesContent.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));

        loadExams();

        tableExams.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedExam = newSel;
                txtExamName.setText(selectedExam.getName());
                loadExamQuestions(selectedExam.getId());
            } else {
                selectedExam = null;
                txtExamName.clear();
                examQuestionList = FXCollections.observableArrayList();
                tableExamQuestions.setItems(examQuestionList);
            }
        });
    }

    private void loadExams() {
        List<Exam> list = examDAO.getAllExams();
        examList = FXCollections.observableArrayList(list);
        tableExams.setItems(examList);
    }

    private void loadExamQuestions(int examId) {
        List<Integer> questionIds = examQuestionDAO.getQuestionIdsByExamId(examId);
        List<Question> questions = new ArrayList<>();

        for (Integer qid : questionIds) {
            Question q = questionDAO.getAllQuestions()
                    .stream()
                    .filter(x -> x.getId() == qid)
                    .findFirst()
                    .orElse(null);
            if (q != null) questions.add(q);
        }
        examQuestionList = FXCollections.observableArrayList(questions);
        tableExamQuestions.setItems(examQuestionList);
    }

    @FXML
    private void handleAddExam() {
        String name = txtExamName.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đề thi không được để trống");
            return;
        }
        Exam exam = new Exam();
        exam.setName(name);

        int newId = examDAO.insertExam(exam); // Gọi hàm trả về ID
        if (newId > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm đề thi thành công");
            loadExams();
            txtExamName.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm đề thi thất bại");
        }
    }

    @FXML
    private void handleUpdateExam() {
        if (selectedExam == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đề thi để sửa");
            return;
        }
        String name = txtExamName.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đề thi không được để trống");
            return;
        }
        selectedExam.setName(name);
        if (examDAO.updateExam(selectedExam)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật đề thi thành công");
            loadExams();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật đề thi thất bại");
        }
    }

    @FXML
    private void handleDeleteExam() {
        if (selectedExam == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đề thi để xóa");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn chắc chắn muốn xóa đề thi này?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận xóa");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (examDAO.deleteExam(selectedExam.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa đề thi thành công");
                loadExams();
                selectedExam = null;
                txtExamName.clear();
                examQuestionList = FXCollections.observableArrayList();
                tableExamQuestions.setItems(examQuestionList);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa đề thi thất bại");
            }
        }
    }

    @FXML
    private void handleAddQuestionToExam() {
        if (selectedExam == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đề thi trước");
            return;
        }

        // Lấy danh sách tất cả câu hỏi chưa có trong đề thi
        List<Question> allQuestions = questionDAO.getAllQuestions();
        List<Integer> questionIdsInExam = examQuestionDAO.getQuestionIdsByExamId(selectedExam.getId());

        List<Question> availableQuestions = allQuestions.stream()
                .filter(q -> !questionIdsInExam.contains(q.getId()))
                .toList();

        if (availableQuestions.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không còn câu hỏi để thêm");
            return;
        }

        // Tạo dialog tùy chỉnh với ComboBox để chọn câu hỏi
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Thêm câu hỏi");
        dialog.setHeaderText("Chọn câu hỏi để thêm vào đề thi");

        ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        ComboBox<Question> comboBox = new ComboBox<>(FXCollections.observableArrayList(availableQuestions));
        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "[" + item.getId() + "] " + item.getContent());
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "[" + item.getId() + "] " + item.getContent());
            }
        });
        comboBox.getSelectionModel().selectFirst();

        dialog.getDialogPane().setContent(comboBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return comboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selectedQuestion -> {
            ExamQuestion eq = new ExamQuestion(selectedExam.getId(), selectedQuestion.getId());
            if (examQuestionDAO.insertExamQuestion(eq)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm câu hỏi vào đề thi thành công");
                loadExamQuestions(selectedExam.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm câu hỏi vào đề thi thất bại");
            }
        });
    }


    @FXML
    private void handleRemoveQuestionFromExam() {
        Question selectedQuestion = tableExamQuestions.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đề thi trước");
            return;
        }
        if (selectedQuestion == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn câu hỏi trong đề thi để xóa");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn chắc chắn muốn xóa câu hỏi khỏi đề thi?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận xóa");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (examQuestionDAO.deleteExamQuestion(selectedExam.getId(), selectedQuestion.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa câu hỏi khỏi đề thi thành công");
                loadExamQuestions(selectedExam.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa câu hỏi khỏi đề thi thất bại");
            }
        }
    }

    @FXML
    private void handleCreateRandomExam() {
        String name = txtExamName.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đề thi không được để trống");
            return;
        }

        List<Question> allQuestions = questionDAO.getAllQuestions();
        if (allQuestions.size() < 4) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không đủ câu hỏi để tạo đề thi ngẫu nhiên (cần ít nhất 4 câu).");
            return;
        }

        // Xáo trộn câu hỏi
        Collections.shuffle(allQuestions);

        // Lấy 4 câu hỏi đầu
        List<Question> selectedQuestions = allQuestions.subList(0, 4);

        Exam exam = new Exam();
        exam.setName(name);

        // Tạo đề thi mới, lấy ID
        int newExamId = examDAO.insertExam(exam);
        if (newExamId == -1) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tạo đề thi thất bại");
            return;
        }

        // Thêm câu hỏi vào đề thi
        boolean allInserted = true;
        for (Question q : selectedQuestions) {
            ExamQuestion eq = new ExamQuestion(newExamId, q.getId());
            if (!examQuestionDAO.insertExamQuestion(eq)) {
                allInserted = false;
            }
        }

        if (allInserted) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo đề thi ngẫu nhiên thành công với 4 câu hỏi");
        } else {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tạo đề thi thành công nhưng thêm câu hỏi chưa hoàn chỉnh");
        }

        loadExams();
        txtExamName.clear();
    }

    @FXML
    private void handleExportExamDocx() {
        if (selectedExam == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đề thi để xuất");
            return;
        }

        List<Integer> questionIds = examQuestionDAO.getQuestionIdsByExamId(selectedExam.getId());
        List<Question> questions = new java.util.ArrayList<>();
        List<List<Answer>> answersPerQuestion = new java.util.ArrayList<>();

        for (Integer qid : questionIds) {
            Question q = questionDAO.getAllQuestions().stream()
                    .filter(x -> x.getId() == qid)
                    .findFirst()
                    .orElse(null);
            if (q != null) {
                questions.add(q);
                List<Answer> answers = answerDAO.getAnswersByQuestionId(q.getId());
                answersPerQuestion.add(answers);
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu đề thi dưới dạng DOCX");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document (*.docx)", "*.docx"));
        java.io.File file = fileChooser.showSaveDialog(txtExamName.getScene().getWindow());

        if (file != null) {
            try {
                DocxExporter.exportExamToDocxShuffled(selectedExam, questions, answersPerQuestion, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất đề thi thành công tại:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xuất đề thi thất bại: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportAnswerDocx() {
        if (selectedExam == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đề thi để xuất đáp án");
            return;
        }

        List<Integer> questionIds = examQuestionDAO.getQuestionIdsByExamId(selectedExam.getId());
        List<Question> questions = new java.util.ArrayList<>();
        List<List<Answer>> answersPerQuestion = new java.util.ArrayList<>();

        for (Integer qid : questionIds) {
            Question q = questionDAO.getAllQuestions().stream()
                    .filter(x -> x.getId() == qid)
                    .findFirst()
                    .orElse(null);
            if (q != null) {
                questions.add(q);
                List<Answer> answers = answerDAO.getAnswersByQuestionId(q.getId());
                answersPerQuestion.add(answers);
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu đáp án đề thi dưới dạng DOCX");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document (*.docx)", "*.docx"));
        java.io.File file = fileChooser.showSaveDialog(txtExamName.getScene().getWindow());

        if (file != null) {
            try {
                DocxExporter.exportAnswerKeyToDocx(selectedExam, questions, answersPerQuestion, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất đáp án thành công tại:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xuất đáp án thất bại: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

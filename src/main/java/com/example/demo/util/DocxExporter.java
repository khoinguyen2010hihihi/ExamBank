package com.example.demo.util;

import com.example.demo.model.Answer;
import com.example.demo.model.Exam;
import com.example.demo.model.Question;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DocxExporter {

    /**
     * Xuất đề thi ra file docx với chức năng sáo câu hỏi và chèn ảnh (nếu có)
     * @param exam đề thi
     * @param questions danh sách câu hỏi (chưa sáo)
     * @param answersPerQuestion danh sách đáp án tương ứng theo câu hỏi
     * @param filePath đường dẫn file cần lưu
     */
    public static void exportExamToDocxShuffled(Exam exam, List<Question> questions,
                                                List<List<Answer>> answersPerQuestion,
                                                String filePath) throws IOException {
        // Sao chép danh sách để không ảnh hưởng dữ liệu gốc
        List<Question> questionsCopy = new java.util.ArrayList<>(questions);
        List<List<Answer>> answersCopy = new java.util.ArrayList<>(answersPerQuestion);

        // Tạo list index cho câu hỏi
        List<Integer> indexes = new java.util.ArrayList<>();
        for (int i = 0; i < questionsCopy.size(); i++) {
            indexes.add(i);
        }

        // Sáo trộn vị trí câu hỏi
        Collections.shuffle(indexes);

        XWPFDocument doc = new XWPFDocument();

        // Tiêu đề đề thi
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitle = title.createRun();
        runTitle.setBold(true);
        runTitle.setFontSize(20);
        runTitle.setText("Đề thi: " + exam.getName());
        runTitle.addBreak();

        // Lặp từng câu hỏi theo thứ tự đã được shuffle
        for (int i = 0; i < indexes.size(); i++) {
            int idx = indexes.get(i);
            Question q = questionsCopy.get(idx);
            List<Answer> answers = answersCopy.get(idx);

            // Câu hỏi
            XWPFParagraph paraQ = doc.createParagraph();
            XWPFRun runQ = paraQ.createRun();
            runQ.setBold(true);
            runQ.setFontSize(14);
            runQ.setText("Câu " + (i + 1) + ": " + q.getContent());
            runQ.addBreak();

            // --- BỔ SUNG: Chèn ảnh nếu có mediaPath hợp lệ ---
            if (q.getMediaPath() != null && !q.getMediaPath().isEmpty()) {
                File imgFile = new File(q.getMediaPath());
                if (imgFile.exists() && imgFile.isFile()) {
                    try (FileInputStream is = new FileInputStream(imgFile)) {
                        String lowerName = imgFile.getName().toLowerCase();
                        int format;

                        if (lowerName.endsWith(".png")) {
                            format = XWPFDocument.PICTURE_TYPE_PNG;
                        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                            format = XWPFDocument.PICTURE_TYPE_JPEG;
                        } else if (lowerName.endsWith(".gif")) {
                            format = XWPFDocument.PICTURE_TYPE_GIF;
                        } else if (lowerName.endsWith(".bmp")) {
                            format = XWPFDocument.PICTURE_TYPE_BMP;
                        } else {
                            // Nếu định dạng không hỗ trợ, bỏ qua ảnh
                            format = -1;
                        }

                        if (format != -1) {
                            XWPFParagraph imgPara = doc.createParagraph();
                            XWPFRun imgRun = imgPara.createRun();

                            // Kích thước ảnh (điều chỉnh nếu cần)
                            int width = 300;  // pixels
                            int height = 200; // pixels

                            imgRun.addPicture(is, format, imgFile.getName(),
                                    Units.toEMU(width), Units.toEMU(height));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Nếu lỗi chèn ảnh, vẫn tiếp tục in câu hỏi và đáp án
                    }
                }
            }

            // Sáo trộn đáp án cho câu hỏi
            List<Answer> shuffledAnswers = new java.util.ArrayList<>(answers);
            Collections.shuffle(shuffledAnswers);

            // In đáp án
            char optionChar = 'A';
            for (Answer a : shuffledAnswers) {
                XWPFParagraph paraA = doc.createParagraph();
                XWPFRun runA = paraA.createRun();
                runA.setText(optionChar + ". " + a.getContent());
                optionChar++;
            }

            // Thêm dòng trống giữa câu hỏi
            doc.createParagraph().createRun().addBreak();
        }

        // Lưu file
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            doc.write(out);
        }
    }

    public static void exportAnswerKeyToDocx(Exam exam, List<Question> questions,
                                             List<List<Answer>> answersPerQuestion,
                                             String filePath) throws IOException {

        List<Question> questionsCopy = new java.util.ArrayList<>(questions);
        List<List<Answer>> answersCopy = new java.util.ArrayList<>(answersPerQuestion);

        // Sáo trộn vị trí câu hỏi nếu muốn, hoặc giữ nguyên thứ tự
        // Collections.shuffle(indexes);

        XWPFDocument doc = new XWPFDocument();

        // Tiêu đề
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitle = title.createRun();
        runTitle.setBold(true);
        runTitle.setFontSize(20);
        runTitle.setText("Đáp án đề thi: " + exam.getName());
        runTitle.addBreak();

        // Lặp từng câu hỏi
        for (int i = 0; i < questionsCopy.size(); i++) {
            Question q = questionsCopy.get(i);
            List<Answer> answers = answersCopy.get(i);

            // Câu hỏi
            XWPFParagraph paraQ = doc.createParagraph();
            XWPFRun runQ = paraQ.createRun();
            runQ.setBold(true);
            runQ.setFontSize(14);
            runQ.setText("Câu " + (i + 1) + ": " + q.getContent());
            runQ.addBreak();

            // In đáp án đúng hoặc in toàn bộ đáp án
            // Ví dụ: chỉ in đáp án đúng
            for (Answer a : answers) {
                if (a.isCorrect()) {
                    XWPFParagraph paraA = doc.createParagraph();
                    XWPFRun runA = paraA.createRun();
                    runA.setText("Đáp án đúng: " + a.getContent());
                    break; // nếu chỉ 1 đáp án đúng
                }
            }

            // Thêm dòng trống
            doc.createParagraph().createRun().addBreak();
        }

        // Lưu file
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            doc.write(out);
        }
    }
}
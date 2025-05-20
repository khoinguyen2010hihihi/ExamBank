# Exam Bank - Java Application for Japanese Exam Management with AI Support

## Mô tả dự án

Dự án quản lý ngân hàng đề thi tiếng Nhật theo cấp độ JLPT, hỗ trợ:

- Quản lý câu hỏi (text, hình ảnh, âm thanh) và đáp án.
- Quản lý đề thi, thêm/xóa/sửa câu hỏi trong đề thi.
- Tạo đề thi ngẫu nhiên từ ngân hàng câu hỏi.
- Tích hợp AI (Hugging Face) hỗ trợ tạo câu hỏi và gợi ý đáp án tự động.
- Xuất đề thi và đáp án ra file DOCX (Word) có định dạng đẹp.
- Quản lý âm thanh và hình ảnh kèm theo câu hỏi.
- Tích hợp file `.env` để bảo mật API key và cấu hình database.

---

## Yêu cầu

- Java 23.0.2
- Maven 3.9.9
- MySQL (Cấu hình database trong `.env`)
- Kết nối internet để gọi API Hugging Face
- Tạo API_KEY trong HuggingFace, đăng kí -> đăng nhập -> xác minh trong gamil -> sử dụng URL: https://huggingface.co/settings/tokens để vào trang tạo key dạng Read-> Thêm API_KEY vào .env

---

## Cấu trúc file quan trọng

- `src/main/java/com/example/demo/ai/HuggingFaceClient.java`: Lớp gọi API AI, load API key từ `.env`.
- `src/main/java/com/example/demo/controller/`: Các controller JavaFX xử lý UI.
- `src/main/java/com/example/demo/dao/`: Các lớp truy cập database.
- `src/main/java/com/example/demo/model/`: Các lớp model dữ liệu.
- `src/main/java/com/example/demo/util/DocxExporter.java`: Xử lý xuất file Word.
- `.env`: File cấu hình ( chứa API key và cấu hình DB).
- -uploads: chứa images và audios để chứa file hình ảnh và âm thanh bạn muốn đăng( để dễ nhận dạng ngoài ra không có tác dụng gì cả)
- SQL: Chứa file Database để import hoặc sử dụng lệnh sau để tạo database nếu gặp lỗi:
- CREATE TABLE Question (
    id INT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    type ENUM('text', 'audio', 'image') NOT NULL,
    media_path VARCHAR(255),
    level VARCHAR(10) -- ví dụ: N5, N4, ...
);

CREATE TABLE Answer (
    id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL,
    content TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

CREATE TABLE Exam (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Exam_Question (
    exam_id INT NOT NULL,
    question_id INT NOT NULL,
    PRIMARY KEY (exam_id, question_id),
    FOREIGN KEY (exam_id) REFERENCES Exam(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

---

## Cách cấu hình file `.env`

Tạo file `.env` ở thư mục gốc project (cùng cấp với src) với nội dung:

```dotenv
HF_API_URL=https://router.huggingface.co/novita/v3/openai/chat/completions
HF_API_KEY=your_huggingface_api_key_here

DB_URL=jdbc:mysql://localhost:3306/exam_bank?useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_database_password

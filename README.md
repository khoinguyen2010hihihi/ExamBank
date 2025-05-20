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

- Java 21
- Maven 3.8+
- MySQL (Cấu hình database trong `.env`)
- Kết nối internet để gọi API Hugging Face

---

## Cấu trúc file quan trọng

- `src/main/java/com/example/demo/ai/HuggingFaceClient.java`: Lớp gọi API AI, load API key từ `.env`.
- `src/main/java/com/example/demo/controller/`: Các controller JavaFX xử lý UI.
- `src/main/java/com/example/demo/dao/`: Các lớp truy cập database.
- `src/main/java/com/example/demo/model/`: Các lớp model dữ liệu.
- `src/main/java/com/example/demo/util/DocxExporter.java`: Xử lý xuất file Word.
- `.env`: File cấu hình (không được commit, chứa API key và cấu hình DB).

---

## Cách cấu hình file `.env`

Tạo file `.env` ở thư mục gốc project với nội dung:

```dotenv
HF_API_URL=https://router.huggingface.co/novita/v3/openai/chat/completions
HF_API_KEY=your_huggingface_api_key_here

DB_URL=jdbc:mysql://localhost:3306/exam_bank?useSSL=false&serverTimezone=UTC
DB_USERNAME=root (Tài khoản database trong MySQL)
DB_PASSWORD=your_database_password (Mật khẩu database trong MySQL)

## Lệnh để chạy file .jar trong CMD
cd /d D:\Java\Code\exam-bank\demo
mvn javafx:run

---

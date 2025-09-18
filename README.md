<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   ỨNG DỤNG TRẮC NGHIỆM TRỰC TUYẾN
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 📖 1. Giới thiệu
Ứng dụng Trắc nghiệm trực tuyến Client–Server được phát triển bằng Java, dựa trên giao thức TCP nhằm đảm bảo việc trao đổi dữ liệu tin cậy và chính xác. Hệ thống cho phép sinh viên/kỹ thuật viên kết nối tới server, thực hiện làm bài trắc nghiệm và nhận kết quả ngay sau khi hoàn thành.

Hệ thống đáp ứng các yêu cầu cơ bản của một bài thi trực tuyến:

📌 Client kết nối đến server:

Kết nối qua địa chỉ IP và port (mặc định: 5000).

Server hỗ trợ nhiều client đồng thời thông qua cơ chế đa luồng.

Yêu cầu người dùng nhập tên để xác định danh tính.

📌 Gửi và nhận câu hỏi – đáp án:

Server gửi các câu hỏi trắc nghiệm đến client.

Người dùng chọn đáp án và gửi về server.

Server kiểm tra và phản hồi kết quả đúng/sai theo thời gian thực.

📌 Quản lý kết quả:

Server lưu trữ điểm số của từng sinh viên vào cơ sở dữ liệu MySQL.

GUI admin hiển thị IP và điểm số theo thời gian thực.

📌 Client GUI trực quan:

Giao diện đẹp, scroll mượt, thân thiện với người dùng.

Nút submit sẽ disable sau khi nộp bài.

Hiển thị điểm số cuối cùng ngay trên client.

🔹 Ý nghĩa ứng dụng:
Hệ thống giúp sinh viên và nhà phát triển:

Hiểu cơ chế Client–Server và truyền nhận dữ liệu tin cậy qua TCP.

Thực hành triển khai ứng dụng Java đa luồng.

Rèn luyện kỹ năng thiết kế GUI và lưu trữ dữ liệu MySQL.

Làm nền tảng mở rộng cho các ứng dụng khảo thí trực tuyến và phân tích kết quả học tập.


## 🔧 2. Công nghệ sử dụng  

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase-downloads.html) 
[![Swing](https://img.shields.io/badge/Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/) 
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/) 
[![Eclipse](https://img.shields.io/badge/Eclipse-2C2255?style=for-the-badge&logo=eclipseide&logoColor=white)](https://www.eclipse.org/) 


## 🚀 3. Một số hình ảnh của hệ thống

<p align="center">
  <img src="docs/anhGiaoDien.jpg" alt="Ảnh 1" width="100%"/>
</p>

<p align="center">
  <em>Hình 1: Giao diện khi vào ứng dụng</em>
</p>

<p align="center">
  <img src="docs/bangsql.png" alt="Ảnh 2" width="100%"/>
</p>
<p align="center">
  <em>Hình 2: SQL lưu trữ câu hỏi</em>
</p>

<p align="center">
  <img src="docs/ketquasaukhixong.png" alt="Ảnh 3" width="100%"/>
</p>
<p align="center">
  <em>Hình 3: Kết quả sau khi làm</em>
</p>

📦 4. Cài Đặt Và Triển Khai

🔧 Bước 1. Chuẩn Bị Môi Trường

Cài đặt JDK 8+ ☕.

Cài đặt MySQL/MariaDB 🗄️.

Tạo cơ sở dữ liệu và bảng:

CREATE DATABASE BTLQuiz;
USE BTLQuiz;

CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    option1 VARCHAR(255),
    option2 VARCHAR(255),
    option3 VARCHAR(255),
    option4 VARCHAR(255),
    answerIndex INT
);

CREATE TABLE results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    score INT,
    ip VARCHAR(50),
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


📦 Bước 2. Biên Dịch Mã Nguồn

Mở terminal, điều hướng vào thư mục UngDungTracNghiem_TCP.

Biên dịch toàn bộ file:

javac quiz/*.java


Kết quả: các file .class được tạo trong thư mục quiz/.

🚀 Bước 3. Chạy Ứng Dụng

Khởi động Server:

java quiz.QuizServer


👉 Server chạy port 5000, hiển thị log kết nối, mở GUI admin ResultsViewerSwing.

Khởi động Client:

java quiz.QuizClient


👉 Mỗi client mở trong cửa sổ riêng, nhập tên → Start → làm bài → nhận điểm số cuối cùng.

## 🔧 5. Liên hệ ( cá nhân )

## 👜Thông tin cá nhân
**Họ tên**: Khổng Minh Hoài

**Lớp**: CNTT 16-04

**Email**: khonghoai.15052004@gmail.com

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.


---





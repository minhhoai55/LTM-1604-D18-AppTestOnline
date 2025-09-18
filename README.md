<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   ỨNG DỤNG TRẮC NHIỆM TRỰC TUYẾN
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
Ứng dụng Trắc nghiệm trực tuyến Client–Server được phát triển bằng Java, dựa trên giao thức TCP để đảm bảo việc trao đổi dữ liệu tin cậy và chính xác. Hệ thống cho phép sinh viên/kỹ thuật viên kết nối tới server, thực hiện làm bài trắc nghiệm, và nhận kết quả ngay sau khi hoàn thành.

Các chức năng chính:

Client kết nối đến server qua địa chỉ IP và port (mặc định: 5000). Server hỗ trợ nhiều client đồng thời thông qua cơ chế đa luồng, và yêu cầu người dùng nhập tên để xác định danh tính.

Gửi và nhận câu hỏi – đáp án: Server gửi các câu hỏi trắc nghiệm đến client. Người dùng chọn đáp án, gửi về server; server kiểm tra và phản hồi kết quả đúng/sai theo thời gian thực.

Server lưu trữ kết quả sinh viên và hiển thị IP + điểm số trên GUI admin.

Client GUI đẹp, scroll mượt, submit disable sau khi nộp, hiển thị điểm số cuối cùng.

<p align="center">
  <img src="docs/anhGiaoDien.jpg" alt="Ảnh 1" width="800"/>
</p>

<p align="center">
  <em>Hình 1: Giao diện khi vào ứng dụng  </em>
</p>

SQL
<p align="center">
  <img src="docs/bangsql.png" alt="Ảnh 3" width="500"/>
 
</p>
<p align="center">
  <em> Sql lưu trữ câu hỏi  </em>
</p>

<p align="center">
    <img src="docs/ketquasaukhixg.png" alt="Ảnh 4" width="450"/>
</p>
<p align="center">
  <em> Hình 3: Kết quả sau khi làm </em>
</p>

## 🔧 2. Công nghệ sử dụng  

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase-downloads.html) 
[![Swing](https://img.shields.io/badge/Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/) 
[![Nimbus](https://img.shields.io/badge/Nimbus%20Look&Feel-4B0082?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/nimbus.html) 
[![UDP](https://img.shields.io/badge/UDP%20Socket-00599C?style=for-the-badge&logo=socket.io&logoColor=white)](https://docs.oracle.com/javase/tutorial/networking/datagrams/) 
[![HTTP](https://img.shields.io/badge/HTTP-FF6F00?style=for-the-badge&logo=mozilla&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTTP) 
[![NTP](https://img.shields.io/badge/NTP-228B22?style=for-the-badge&logo=internet-explorer&logoColor=white)](https://www.ntp.org/) 
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/) 
[![JDBC](https://img.shields.io/badge/JDBC%20Connector-CC0000?style=for-the-badge&logo=java&logoColor=white)](https://dev.mysql.com/downloads/connector/j/) 
[![Eclipse](https://img.shields.io/badge/Eclipse-2C2255?style=for-the-badge&logo=eclipseide&logoColor=white)](https://www.eclipse.org/) 
[![NetBeans](https://img.shields.io/badge/NetBeans-1B6AC6?style=for-the-badge&logo=apachenetbeanside&logoColor=white)](https://netbeans.apache.org/) 


### 🔧 Yêu cầu hệ thống

Java Development Kit (JDK): Phiên bản 8 trở lên

Hệ điều hành: Windows, macOS, hoặc Linux

Môi trường phát triển: IDE (IntelliJ IDEA, Eclipse, VS Code) hoặc terminal/command prompt

Bộ nhớ: Tối thiểu 512MB RAM

Dung lượng: Khoảng 10MB cho mã nguồn và file thực thi

Mạng: Kết nối TCP/IP giữa client và server

### 4. 📦 Cài đặt và triển khai
**Bước 1: Chuẩn Bị Môi Trường
Kiểm tra Java:

java -version
javac -version


Đảm bảo hiển thị Java 8 trở lên.

Cài đặt MySQL/MariaDB và tạo cơ sở dữ liệu:

CREATE DATABASE BTLQuiz;
USE BTLQuiz;

-- Tạo bảng questions
CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    option1 VARCHAR(255),
    option2 VARCHAR(255),
    option3 VARCHAR(255),
    option4 VARCHAR(255),
    answerIndex INT
);

-- Tạo bảng results
CREATE TABLE results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    score INT,
    ip VARCHAR(50),
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


Tải mã nguồn:
Sao chép thư mục UngDungTracNghiem_TCP chứa các file:
QuizServer.java, QuizClient.java, QuizClientSwing.java, Question.java, ResultsViewerSwing.java.

**Bước 2: Biên Dịch Mã Nguồn
**
Mở terminal và điều hướng đến thư mục chứa mã nguồn.

Biên dịch tất cả file:

javac quiz/*.java


Hoặc biên dịch từng file:

javac quiz/QuizServer.java
javac quiz/QuizClient.java
javac quiz/QuizClientSwing.java
javac quiz/Question.java
javac quiz/ResultsViewerSwing.java


Kết quả: các file .class tương ứng sẽ được tạo ra trong thư mục quiz.

**Bước 3: Chạy Ứng Dụng
**
Khởi động Server:

java quiz.QuizServer


Server sẽ khởi động ở port mặc định 5000.

Console hiển thị log khi có client kết nối.

Server tạo GUI admin ResultsViewerSwing để hiển thị kết quả realtime.

Khởi động Client:

java quiz.QuizClient


Mỗi client mở trong cửa sổ riêng (GUI Swing).

Nhập Tên sinh viên → bấm Start để nhận câu hỏi.

Sau khi hoàn thành, điểm số sẽ hiển thị ngay trên client.

### 5. 🚀 Sử dụng ứng dụng

Kết nối: Nhập Tên sinh viên → bấm Start → client kết nối tới server và nhận câu hỏi.

Làm bài: Chọn đáp án cho từng câu hỏi. Scroll để xem tất cả câu hỏi nếu nhiều câu.

Nộp bài: Nhấn Nộp bài → điểm số hiển thị trên client. Nút Nộp bài bị disable sau khi submit.

Xem kết quả: Trên server, GUI ResultsViewerSwing hiển thị danh sách sinh viên, IP và điểm số realtime. Điểm 100% được highlight màu xanh.

Ngắt kết nối: Đóng cửa sổ client hoặc mất mạng sẽ tự động ngắt kết nối.

## 🔧 6. Ghi chú
Server fetch câu hỏi trực tiếp từ bảng questions trong MySQL.

Client và server giao tiếp qua TCP/IP, port mặc định 5000.

Kết quả luôn được lưu trong danh sách results realtime trên server GUI.

Có thể mở nhiều client cùng lúc để kiểm tra kết quả đồng thời.
## 👜Thông tin cá nhân
**Họ tên**: Khổng Minh Hoài
**Lớp**: CNTT 16-04
**Email**: khonghoai.15052004@gmail.com

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.


---

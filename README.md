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

# 📖 1. Giới thiệu
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


# 🔧 2. Công nghệ sử dụng  

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase-downloads.html) 
[![Swing](https://img.shields.io/badge/Java%20Swing-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/) 
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/) 
[![Eclipse](https://img.shields.io/badge/Eclipse-2C2255?style=for-the-badge&logo=eclipseide&logoColor=white)](https://www.eclipse.org/) 


# 🚀 3. Một số hình ảnh của hệ thống

<p align="center">
  <img src="docs/Dangnhap.png" alt="Ảnh 1" width="700"/>
</p>

<p align="center">
  <em>Hình 1: Giao diện đăng nhập</em>
</p>

<p align="center">
  <img src="docs/Chonmon.png" alt="Ảnh 2" width="700"/>
</p>
<p align="center">
  <em>Hình 2: Chọn môn thi</em>

</p>

<p align="center">
  <img src="docs/Baithi.png" alt="Ảnh 3" width="700"/>
</p>
<p align="center">
  <em>Hình 3: Bài thi</em>
</p>
  
</p>

<p align="center">
  <img src="docs/Ketqua.png" alt="Ảnh 4" width="700"/>
</p>
<p align="center">
  <em>Hình 4: Kết quả thi mà giáo viên quản lí</em>
</p>


# ⚡ 4. Các bước cài đặt

### 🔧 Bước 1: Chuẩn bị môi trường
- ☕ Cài đặt **JDK 8 hoặc 11**  
- 🗄️ Cài đặt **MySQL 8.x + Workbench**  
- 🛠️ Tạo **database `BTLQuiz`**

---
### 🗄️ Bước 2: Tạo bảng trong MySQL
- Import file SQL tạo bảng `questions` và `results` (hoặc chạy script tạo thủ công).  

---

### 📦 Bước 3: Thêm thư viện JDBC
- 📥 Tải **`mysql-connector-j-8.x.x.jar`**  
- 📂 Copy vào thư mục **`lib/`** của project  
- ➕ Chuột phải chọn **Add to Build Path** trong IDE

---

### ⚙️ Bước 4: Cấu hình kết nối
        Trong file **`DbHelper.java`**:  
        
        ```java
        public class DbHelper {
            private static final String URL = "jdbc:mysql://127.0.0.1:3306/BTLQuiz?serverTimezone=UTC";
            private static final String USER = "root";
            private static final String PASS = "your_password";
        
            public static Connection open() throws Exception {
                return DriverManager.getConnection(URL, USER, PASS);
            }
        }
🔑 Thay your_password bằng mật khẩu MySQL của bạn.

### ▶️ Bước 5: Chạy hệ thống

## 🖥️ Chạy **Server**
- Mở **`QuizServer.java`** → nhấn **Start Server 🟢**  
- 🌐 Server sẽ chạy trên **cổng `5000`**  

---

## 💻 Chạy **Client**
- Mở **`QuizClient.java`** → nhấn **Run 🚀**  
- 🔗 Client sẽ kết nối tới **`localhost:5000`**  
- 📝 Giao diện **làm bài thi** sẽ xuất hiện nếu đăng nhập tài khoản sinh viên
- 📝 Giao diện **quản lí điểm** sẽ xuất hiện nếu đăng nhập tài khoản giáo viên


---

## 🎯 Sử dụng hệ thống
1. ✍️**Đăng nhập** (hoặc đăng ký nếu là lần đầu) bằng tài khoản của bạn  
2. 📖 Chọn **môn thi** bạn muốn và hoàn thành các câu hỏi 
3. 📤 Nhấn **Nộp bài** để xem kết quả và đáp án chi tiết
 

---

        ### 🗄️ Kiểm tra dữ liệu trong MySQL Workbench
        
        SELECT * FROM questions;
        SELECT * FROM results;



# 🔧 5. Liên hệ (cá nhân)

**Họ tên**: Khổng Minh Hoài

**Lớp**: CNTT 16-04

**Email**: khonghoai.15052004@gmail.com

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.


---










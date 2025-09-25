// QuizClient.java - ĐÃ SỬA LỖI
package quiz;

import java.io.*;
import java.net.*;

public class QuizClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        // Không dùng try-with-resources ở đây nữa
        try {
            Socket socket = new Socket(HOST, PORT);
            socket.setSoTimeout(10000); // 10s timeout
            System.out.println("Connected to server at " + HOST + ":" + PORT);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Giao socket cho QuizClientSwing quản lý
            // Socket sẽ được giữ mở cho đến khi cửa sổ Swing bị đóng
            new QuizClientSwing(out, in, socket);

        } catch (IOException e) {
            e.printStackTrace();
            // Hiển thị lỗi này bằng JOptionPane sẽ thân thiện với người dùng hơn
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Không thể kết nối tới server: " + e.getMessage() + "\n" +
                    "Vui lòng đảm bảo server đang chạy và kiểm tra lại địa chỉ/cổng.",
                    "Lỗi Kết Nối",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
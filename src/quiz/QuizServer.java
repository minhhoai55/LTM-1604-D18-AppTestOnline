package quiz;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

public class QuizServer {
    private static final int PORT = 5000;
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/BTLQuiz?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public static void main(String[] args) {
        // Khởi chạy trình xem kết quả
        SwingUtilities.invokeLater(() -> new ResultsViewerSwing());
        
        // DÒNG KHỞI CHẠY QuestionManager ĐÃ ĐƯỢC XÓA Ở ĐÂY

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("QuizServer đang chạy tại cổng " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client kết nối: " + clientSocket);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) { this.socket = socket; }

        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.flush();
                List<Question> questions = fetchQuestionsFromDB();

                // Logic xáo trộn câu hỏi và đáp án
                Collections.shuffle(questions);
                for (Question q : questions) {
                    String correctAnswerText = q.getOptions().get(q.getAnswerIndex());
                    Collections.shuffle(q.getOptions());
                    int newAnswerIndex = q.getOptions().indexOf(correctAnswerText);
                    q.setAnswerIndex(newAnswerIndex);
                }

                out.writeObject(questions);
                out.flush();

                String studentName = (String) in.readObject();
                @SuppressWarnings("unchecked")
                List<Integer> answers = (List<Integer>) in.readObject();

                int score = 0;
                List<Boolean> correctList = new ArrayList<>();
                for (int i = 0; i < questions.size(); i++) {
                    boolean correct = i < answers.size() && answers.get(i) == questions.get(i).getAnswerIndex();
                    if (correct) score++;
                    correctList.add(correct);
                }

                Object[] resp = new Object[]{score, correctList};
                out.writeObject(resp);
                out.flush();

                saveResultToDB(studentName, score, socket.getInetAddress().getHostAddress());
                System.out.println("Client ngắt kết nối.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private List<Question> fetchQuestionsFromDB() {
            List<Question> list = new ArrayList<>();
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM questions ORDER BY RAND() LIMIT 10")) {

                    while (rs.next()) {
                        String content = rs.getString("content");
                        List<String> options = new ArrayList<>();
                        options.add(rs.getString("option1"));
                        options.add(rs.getString("option2"));
                        options.add(rs.getString("option3"));
                        options.add(rs.getString("option4"));
                        int answerIndex = rs.getInt("answerIndex");
                        list.add(new Question(content, options, answerIndex));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        private void saveResultToDB(String name, int score, String ip) {
            String sql = "INSERT INTO results(name, score, ip, time) VALUES (?, ?, ?, NOW())";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt(2, score);
                ps.setString(3, ip);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
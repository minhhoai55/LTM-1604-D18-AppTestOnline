package quiz;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class QuizServer {
    private static final int PORT = 5000;
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/BTLQuiz?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final Map<Socket, List<Question>> clientQuestionsMap = new ConcurrentHashMap<>();
    // THÊM: Map để lưu lại môn thi của client
    private static final Map<Socket, String> clientSubjectMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang chạy trên cổng " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private static void handleClient(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            while (!socket.isClosed()) {
                String request = (String) in.readObject();
                System.out.println("Received request: " + request);
                switch (request) {
                    case "LOGIN":
                        handleLogin(in, out);
                        break;
                    case "REGISTER":
                        handleRegister(in, out);
                        break;
                    case "START_QUIZ":
                        handleStartQuiz(in, out, socket);
                        break;
                    case "SUBMIT_ANSWERS":
                        handleSubmitAnswers(in, out, socket);
                        break;
                    default:
                       out.writeObject("Yêu cầu không hợp lệ");
                       out.flush();
                }
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Dọn dẹp cả 2 map khi client ngắt kết nối
            clientQuestionsMap.remove(socket);
            clientSubjectMap.remove(socket); // THÊM
            System.out.println("Cleaned up resources for client: " + socket.getInetAddress().getHostAddress());
        }
    }

    // handleLogin và handleRegister giữ nguyên, không thay đổi
    private static void handleLogin(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        String studentId = (String) in.readObject();
        String password = (String) in.readObject();
        try (Connection conn = getDBConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, role FROM users WHERE student_id = ? AND password = ?")) {
            stmt.setString(1, studentId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                out.writeObject("SUCCESS");
                out.writeObject(rs.getString("name"));
                out.writeObject(rs.getString("role"));
            } else {
                out.writeObject("Mã sinh viên hoặc mật khẩu không đúng!");
            }
        } catch (SQLException e) {
            out.writeObject("Lỗi database: " + e.getMessage());
            e.printStackTrace();
        }
        out.flush();
    }
    private static void handleRegister(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        String name = (String) in.readObject();
        String studentId = (String) in.readObject();
        String password = (String) in.readObject();
        String className = (String) in.readObject();
        try (Connection conn = getDBConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT student_id FROM users WHERE student_id = ?")) {
            checkStmt.setString(1, studentId);
            if (checkStmt.executeQuery().next()) {
                out.writeObject("Mã sinh viên đã tồn tại!");
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO users (name, student_id, password, class, role, created_at) VALUES (?, ?, ?, ?, 'student', NOW())")) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, studentId);
                    insertStmt.setString(3, password);
                    insertStmt.setString(4, className);
                    insertStmt.executeUpdate();
                    out.writeObject("SUCCESS");
                }
            }
        } catch (SQLException e) {
            out.writeObject("Lỗi database: " + e.getMessage());
            e.printStackTrace();
        }
        out.flush();
    }
    
    private static void handleStartQuiz(ObjectInputStream in, ObjectOutputStream out, Socket socket) throws IOException, ClassNotFoundException {
        String subject = (String) in.readObject();
        List<Question> questions = new ArrayList<>();
        try (Connection conn = getDBConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM questions WHERE subject = ? ORDER BY RAND() LIMIT 10")) {
            stmt.setString(1, subject);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                List<String> options = new ArrayList<>();
                options.add(rs.getString("option1"));
                options.add(rs.getString("option2"));
                options.add(rs.getString("option3"));
                options.add(rs.getString("option4"));
                Question q = new Question(rs.getString("content"), options, rs.getInt("answerIndex"));
                questions.add(q);
            }
            
            clientQuestionsMap.put(socket, questions);
            // THÊM: Lưu lại môn thi của client
            clientSubjectMap.put(socket, subject);

            out.writeObject(questions);
        } catch (SQLException e) {
            out.writeObject("Lỗi database khi tải câu hỏi: " + e.getMessage());
            e.printStackTrace();
        }
        out.flush();
    }
    
    private static void handleSubmitAnswers(ObjectInputStream in, ObjectOutputStream out, Socket socket) throws IOException, ClassNotFoundException {
        List<Question> questions = clientQuestionsMap.get(socket);
        // THÊM: Lấy môn thi từ map
        String subject = clientSubjectMap.get(socket);

        String[] studentInfo = (String[]) in.readObject();
        List<Integer> answers = (List<Integer>) in.readObject();
        String studentName = studentInfo[0];
        String studentId = studentInfo[1];
        
        if (questions == null || subject == null) {
            out.writeObject(new Object[]{-1, new ArrayList<Boolean>()});
            out.flush();
            System.out.println("Lỗi: Không tìm thấy bộ câu hỏi hoặc môn thi cho client.");
            return;
        }

        int score = 0;
        List<Boolean> correctList = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            boolean isCorrect = (answers.get(i) != null && answers.get(i) == questions.get(i).getAnswerIndex());
            correctList.add(isCorrect);
            if (isCorrect) score++;
        }

        // SỬA: Thay đổi câu lệnh INSERT để thêm cả môn thi
        String sql = "INSERT INTO results (name, student_id, score, subject, ip, time) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = getDBConnection();
             PreparedStatement saveStmt = conn.prepareStatement(sql)) {
            saveStmt.setString(1, studentName);
            saveStmt.setString(2, studentId);
            saveStmt.setInt(3, score);
            saveStmt.setString(4, subject); // THÊM: Gán giá trị cho cột subject
            saveStmt.setString(5, socket.getInetAddress().getHostAddress());
            saveStmt.executeUpdate();

            out.writeObject(new Object[]{score, correctList});
        } catch (SQLException e) {
            out.writeObject("Lỗi database khi lưu kết quả: " + e.getMessage());
            e.printStackTrace();
        }
        out.flush();
    }
}
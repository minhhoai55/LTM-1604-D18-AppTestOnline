package quiz;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.List;

public class QuizClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(HOST, PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            @SuppressWarnings("unchecked")
            List<Question> questions = (List<Question>) in.readObject();

            SwingUtilities.invokeLater(() ->
                new QuizClientSwing(questions, out, in, socket)
            );
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Không thể kết nối server: " + e.getMessage());
        }
    }
}

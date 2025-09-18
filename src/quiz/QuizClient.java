package quiz;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class QuizClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            @SuppressWarnings("unchecked")
            List<Question> questions = (List<Question>) in.readObject();

            final Socket finalSocket = socket;
            final ObjectOutputStream finalOut = out;
            final ObjectInputStream finalIn = in;

            SwingUtilities.invokeLater(() -> new QuizClientSwing(questions, finalOut, finalIn, finalSocket));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Không thể kết nối server: " + e.getMessage());
            try { if (in != null) in.close(); } catch (Exception ex) {}
            try { if (out != null) out.close(); } catch (Exception ex) {}
            try { if (socket != null) socket.close(); } catch (Exception ex) {}
        }
    }
}

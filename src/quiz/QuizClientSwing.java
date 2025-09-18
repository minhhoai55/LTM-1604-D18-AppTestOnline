package quiz;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class QuizClientSwing extends JFrame {
    private List<Question> questions;
    private List<List<JRadioButton>> optionButtons;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private JTextField nameField;

    public QuizClientSwing(List<Question> questions, ObjectOutputStream out,
                           ObjectInputStream in, Socket socket) {
        this.questions = questions;
        this.out = out;
        this.in = in;
        this.socket = socket;
        this.optionButtons = new ArrayList<>();

        setTitle("Ứng dụng Trắc nghiệm");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tên sinh viên
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Tên sinh viên:"));
        nameField = new JTextField(20);
        namePanel.add(nameField);
        mainPanel.add(namePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Câu hỏi
        for (int qi = 0; qi < questions.size(); qi++) {
            Question q = questions.get(qi);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createTitledBorder("Câu " + (qi + 1)));
            panel.setBackground(new Color(245, 245, 245));
            panel.add(new JLabel("<html><body style='width: 500px'>" + q.getContent() + "</body></html>"));

            ButtonGroup group = new ButtonGroup();
            List<JRadioButton> buttons = new ArrayList<>();

            for (int i = 0; i < q.getOptions().size(); i++) {
                JRadioButton rb = new JRadioButton(q.getOptions().get(i));
                rb.setBackground(new Color(245, 245, 245));
                group.add(rb);
                panel.add(rb);
                buttons.add(rb);
            }

            optionButtons.add(buttons);
            mainPanel.add(panel);
            mainPanel.add(Box.createVerticalStrut(5));
        }

        JButton submitBtn = new JButton("Nộp bài");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.setBackground(new Color(100, 149, 237));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);

        submitBtn.addActionListener(e -> {
            submitAnswers();
            submitBtn.setEnabled(false);
        });

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        add(submitBtn, BorderLayout.SOUTH);

        setSize(650, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void submitAnswers() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sinh viên!");
                return;
            }

            List<Integer> answers = new ArrayList<>();
            for (List<JRadioButton> buttons : optionButtons) {
                int selected = -1;
                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i).isSelected()) {
                        selected = i;
                        break;
                    }
                }
                answers.add(selected);
            }

            new Thread(() -> {
                try {
                    out.writeObject(name);
                    out.writeObject(answers);
                    out.flush();

                    Object resp = in.readObject();
                    int score = (resp instanceof Integer) ? (Integer) resp : Integer.parseInt(resp.toString());

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Điểm của bạn: " + score);
                        dispose();
                    });

                    in.close();
                    out.close();
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Lỗi khi nộp bài: " + ex.getMessage())
                    );
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }
}

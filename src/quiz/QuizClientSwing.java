package quiz;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class QuizClientSwing extends JFrame {
    // --- KHAI BÁO BIẾN ---
    private List<Question> questions;
    private List<List<JRadioButton>> optionButtons;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private JFrame loginFrame;
    private JComboBox<String> subjectComboBox;
    private String loggedInStudentId;
    private String loggedInStudentName;
    private Timer timer;
    private int timeLeftInSeconds;
    private JLabel timerLabel;
    private boolean isSubmitted = false;
    private JButton submitButton;

    // --- CÁC HẰNG SỐ GIAO DIỆN ---
    private static final Font FONT_MAIN = new Font("Arial", Font.PLAIN, 20);
    private static final Font FONT_BOLD = new Font("Arial", Font.BOLD, 22);
    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 30);
    private static final Font FONT_TIMER = new Font("Arial", Font.BOLD, 22);
    private static final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private static final Color COLOR_SUCCESS = new Color(34, 139, 34);
    private static final Color COLOR_ERROR = new Color(200, 35, 51);
    private static final Color COLOR_WARNING = new Color(240, 173, 78);
    private static final Color COLOR_BORDER = new Color(222, 226, 230);
    private static final Color COLOR_CORRECT_BG = new Color(200, 230, 201);
    private static final Color COLOR_INCORRECT_BG = new Color(240, 200, 200);
    private static final int SECONDS_PER_QUESTION = 30;
    private static final Color APP_BACKGROUND_COLOR = new Color(45, 52, 71);

    public QuizClientSwing(ObjectOutputStream out, ObjectInputStream in, Socket socket) {
        this.out = out;
        this.in = in;
        this.socket = socket;
        this.optionButtons = new ArrayList<>();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        showLoginScreen();
    }

    private void showLoginScreen() {
        loginFrame = new JFrame("Đăng Nhập");
        loginFrame.setSize(600, 550);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        

        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBackground(APP_BACKGROUND_COLOR);
        loginFrame.setContentPane(backgroundPanel);
        

        RoundedPanel loginPanel = new RoundedPanel(30, Color.WHITE);
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setPreferredSize(new Dimension(480, 450));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Đăng Nhập Quiz");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridwidth = 2;
        gbc.gridy = 0;
        loginPanel.add(titleLabel, gbc);

        JLabel idLabel = new JLabel("Mã sinh viên/giáo viên:");
        idLabel.setFont(FONT_BOLD);
        gbc.gridy = 1;
        loginPanel.add(idLabel, gbc);

        JTextField studentIdField = new JTextField();
        studentIdField.setFont(FONT_MAIN);
        gbc.gridy = 2;
        loginPanel.add(studentIdField, gbc);

        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(FONT_BOLD);
        gbc.gridy = 3;
        loginPanel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(FONT_MAIN);
        gbc.gridy = 4;
        loginPanel.add(passwordField, gbc);

        JButton loginBtn = new JButton("Đăng Nhập");
        loginBtn.setFont(FONT_BOLD);
        loginBtn.setBackground(COLOR_SUCCESS);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> login(studentIdField.getText(), new String(passwordField.getPassword())));
        
        JButton registerBtn = new JButton("Đăng Ký");
        registerBtn.setFont(FONT_BOLD);
        registerBtn.setBackground(COLOR_PRIMARY);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> showRegisterScreen());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        gbc.gridy = 5;
        loginPanel.add(buttonPanel, gbc);

        backgroundPanel.add(loginPanel);
        loginFrame.setVisible(true);
    }

    private void showRegisterScreen() {
        JFrame registerFrame = new JFrame("Đăng Ký");
        registerFrame.setSize(600, 650);
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerFrame.setLocationRelativeTo(loginFrame);
        
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBackground(APP_BACKGROUND_COLOR);
        registerFrame.setContentPane(backgroundPanel);

        RoundedPanel registerPanel = new RoundedPanel(30, Color.WHITE);
        registerPanel.setLayout(new GridBagLayout());
        registerPanel.setPreferredSize(new Dimension(480, 550));
        registerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Đăng Ký Tài Khoản");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_PRIMARY);
        gbc.gridwidth = 2; gbc.gridy = 0;
        registerPanel.add(titleLabel, gbc);

        JLabel nameLabel = new JLabel("Họ và tên:");
        nameLabel.setFont(FONT_BOLD);
        gbc.gridy = 1;
        registerPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField();
        nameField.setFont(FONT_MAIN);
        gbc.gridy = 2;
        registerPanel.add(nameField, gbc);

        JLabel studentIdLabel = new JLabel("Mã sinh viên:");
        studentIdLabel.setFont(FONT_BOLD);
        gbc.gridy = 3;
        registerPanel.add(studentIdLabel, gbc);

        JTextField newStudentIdField = new JTextField();
        newStudentIdField.setFont(FONT_MAIN);
        gbc.gridy = 4;
        registerPanel.add(newStudentIdField, gbc);

        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(FONT_BOLD);
        gbc.gridy = 5;
        registerPanel.add(passwordLabel, gbc);

        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setFont(FONT_MAIN);
        gbc.gridy = 6;
        registerPanel.add(newPasswordField, gbc);

        JLabel classLabel = new JLabel("Lớp:");
        classLabel.setFont(FONT_BOLD);
        gbc.gridy = 7;
        registerPanel.add(classLabel, gbc);
        
        JTextField classField = new JTextField();
        classField.setFont(FONT_MAIN);
        gbc.gridy = 8;
        registerPanel.add(classField, gbc);

        JButton registerBtn = new JButton("Xác nhận Đăng Ký");
        registerBtn.setFont(FONT_BOLD);
        registerBtn.setBackground(COLOR_SUCCESS);
        registerBtn.setForeground(Color.WHITE);
        gbc.gridy = 9; gbc.fill = GridBagConstraints.NONE;
        registerBtn.addActionListener(e -> {
            try {
                out.writeObject("REGISTER");
                out.writeObject(nameField.getText());
                out.writeObject(newStudentIdField.getText());
                out.writeObject(new String(newPasswordField.getPassword()));
                out.writeObject(classField.getText());
                out.flush();
                String response = (String) in.readObject();
                JOptionPane.showMessageDialog(registerFrame, response, "Kết quả", "SUCCESS".equals(response) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if ("SUCCESS".equals(response)) {
                    registerFrame.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(registerFrame, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        registerPanel.add(registerBtn, gbc);

        backgroundPanel.add(registerPanel);
        registerFrame.setVisible(true);
    }
    
    private void showSubjectSelectionScreen() {
        JFrame subjectFrame = new JFrame("Chọn Môn Thi");
        subjectFrame.setSize(600, 400);
        subjectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        subjectFrame.setLocationRelativeTo(null);

        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBackground(APP_BACKGROUND_COLOR);
        subjectFrame.setContentPane(backgroundPanel);

        RoundedPanel panel = new RoundedPanel(30, Color.WHITE);
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(450, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Chọn Môn Thi");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_PRIMARY);
        panel.add(titleLabel, gbc);

        String[] subjects = {"Lập trình mạng", "Tiếng Anh"};
        subjectComboBox = new JComboBox<>(subjects);
        subjectComboBox.setFont(FONT_MAIN);
        panel.add(subjectComboBox, gbc);

        JButton startQuizBtn = new JButton("Bắt Đầu Thi");
        startQuizBtn.setFont(FONT_BOLD);
        startQuizBtn.setBackground(COLOR_SUCCESS);
        startQuizBtn.setForeground(Color.WHITE);
        startQuizBtn.setBorderPainted(false);
        startQuizBtn.setFocusPainted(false);
        startQuizBtn.addActionListener(e -> {
            subjectFrame.dispose();
            startQuiz();
        });
        panel.add(startQuizBtn, gbc);

        backgroundPanel.add(panel);
        subjectFrame.setVisible(true);
    }
    
    private void startQuiz() {
        try {
            out.writeObject("START_QUIZ");
            out.writeObject(subjectComboBox.getSelectedItem().toString());
            out.flush();
            Object response = in.readObject();

            if (response instanceof List) {
                questions = (List<Question>) response;
                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Không có câu hỏi cho môn học này.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    showSubjectSelectionScreen();
                    return;
                }
                
                timeLeftInSeconds = questions.size() * SECONDS_PER_QUESTION;

                setTitle("Bài Thi Trắc Nghiệm - " + loggedInStudentName);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setSize((int)(screenSize.width * 0.75), (int)(screenSize.height * 0.75));
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLocationRelativeTo(null);
                
                JPanel backgroundPanel = new JPanel(new BorderLayout());
                backgroundPanel.setBackground(APP_BACKGROUND_COLOR);
                backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                setContentPane(backgroundPanel);
                
                RoundedPanel mainRoundedPanel = new RoundedPanel(30, Color.WHITE);
                mainRoundedPanel.setLayout(new BorderLayout());
                
                mainRoundedPanel.add(createHeaderPanel(), BorderLayout.NORTH);
                mainRoundedPanel.add(createQuestionsPanel(), BorderLayout.CENTER);
                mainRoundedPanel.add(createFooterPanel(), BorderLayout.SOUTH);
                
                backgroundPanel.add(mainRoundedPanel, BorderLayout.CENTER);
                
                setVisible(true);
                startTimer();
            } else {
                JOptionPane.showMessageDialog(null, "Lỗi từ server: " + response.toString(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Lỗi khi bắt đầu bài thi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20,0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("Môn: " + subjectComboBox.getSelectedItem().toString());
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_PRIMARY);

        timerLabel = new JLabel();
        timerLabel.setFont(FONT_TIMER);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(timerLabel, BorderLayout.EAST);
        return headerPanel;
    }

    private JScrollPane createQuestionsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        for (int i = 0; i < questions.size(); i++) {
            mainPanel.add(createQuestionPanel(questions.get(i), i));
            mainPanel.add(Box.createVerticalStrut(15));
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20); // Tăng tốc độ cuộn
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JPanel createQuestionPanel(Question q, int index) {
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setBackground(Color.WHITE);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel questionNumber = new JLabel("Câu " + (index + 1) + ": " + q.getContent());
        questionNumber.setFont(FONT_BOLD);
        questionNumber.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionPanel.add(questionNumber);
        questionPanel.add(Box.createVerticalStrut(10));
        
        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> buttonsForThisQuestion = new ArrayList<>();
        char optionChar = 'A';
        for (String optionText : q.getOptions()) {
            JRadioButton rb = new JRadioButton(optionChar + ". " + optionText);
            rb.setFont(FONT_MAIN);
            rb.setBackground(Color.WHITE);
            rb.setFocusPainted(false);
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);

            group.add(rb);
            questionPanel.add(rb);
            buttonsForThisQuestion.add(rb);
        }
        optionButtons.add(buttonsForThisQuestion);

        return questionPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        submitButton = new JButton("NỘP BÀI");
        submitButton.setFont(FONT_BOLD);
        submitButton.setBackground(COLOR_SUCCESS); // Nền xanh
        submitButton.setForeground(Color.WHITE);    // Chữ trắng
        submitButton.setPreferredSize(new Dimension(200, 70));
        
        // ✅ THÊM 2 DÒNG NÀY ĐỂ NÚT TRÔNG ĐƠN GIẢN HƠN
        submitButton.setBorderPainted(false); // Tắt vẽ viền mặc định
        submitButton.setFocusPainted(false);  // Tắt viền khi click

        submitButton.addActionListener(e -> submitAnswers(false));
        
        footerPanel.add(submitButton);
        return footerPanel;
    }

    private void login(String id, String password) {
        try {
            if (socket == null || socket.isClosed()) {
                JOptionPane.showMessageDialog(loginFrame, "Kết nối đã đóng.", "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
                return;
            }
            out.writeObject("LOGIN");
            out.writeObject(id);
            out.writeObject(password);
            out.flush();
            String response = (String) in.readObject();
            if ("SUCCESS".equals(response)) {
                loggedInStudentId = id;
                loggedInStudentName = (String) in.readObject();
                String role = (String) in.readObject();
                loginFrame.dispose();
                if ("teacher".equals(role)) {
                    SwingUtilities.invokeLater(ResultsViewerSwing::new);
                    dispose();
                } else if ("student".equals(role)) {
                    showSubjectSelectionScreen();
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, response, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(loginFrame, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startTimer() {
        timer = new Timer(1000, e -> {
            timeLeftInSeconds--;
            int minutes = timeLeftInSeconds / 60;
            int seconds = timeLeftInSeconds % 60;
            timerLabel.setText(String.format("Thời gian: %02d:%02d", minutes, seconds));

            if (timeLeftInSeconds <= 60 && timeLeftInSeconds > 10) {
                 timerLabel.setForeground(COLOR_WARNING);
            } else if (timeLeftInSeconds <= 10) {
                 timerLabel.setForeground(COLOR_ERROR);
            }

            if (timeLeftInSeconds <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Đã hết giờ! Bài thi sẽ được nộp tự động.", "Hết Giờ", JOptionPane.WARNING_MESSAGE);
                submitAnswers(true);
            }
        });
        timer.start();
    }

    private void submitAnswers(boolean isAutoSubmit) {
        if (isSubmitted) return;
        isSubmitted = true;
        if (timer != null) timer.stop();

        try {
            if (!isAutoSubmit) {
                int unanswered = (int) optionButtons.stream()
                        .filter(group -> group.stream().noneMatch(AbstractButton::isSelected))
                        .count();
                if (unanswered > 0) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Bạn còn " + unanswered + " câu chưa trả lời.\nBạn có chắc chắn muốn nộp bài không?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) {
                        isSubmitted = false;
                        if (timer != null) timer.start();
                        return;
                    }
                }
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
                    out.writeObject("SUBMIT_ANSWERS");
                    out.writeObject(new String[]{loggedInStudentName, loggedInStudentId});
                    out.writeObject(answers);
                    out.flush();
                    Object[] resp = (Object[]) in.readObject();
                    int score = (int) resp[0];
                    List<Boolean> correctList = (List<Boolean>) resp[1];
                    SwingUtilities.invokeLater(() -> {
                        showResultDialog(loggedInStudentName, score);
                        updateAnswerColors(correctList);
                        submitButton.setEnabled(false);
                    });
                } catch (Exception ex) {
                    isSubmitted = false;
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi khi nộp bài: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        } catch (Exception ex) {
            isSubmitted = false;
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAnswerColors(List<Boolean> correctList) {
        for (int i = 0; i < optionButtons.size(); i++) {
            List<JRadioButton> buttons = optionButtons.get(i);
            int correctAnswerIndex = questions.get(i).getAnswerIndex();
            for (int j = 0; j < buttons.size(); j++) {
                JRadioButton button = buttons.get(j);
                button.setEnabled(false);
                if (j == correctAnswerIndex) {
                    button.setBackground(COLOR_CORRECT_BG);
                } else if (button.isSelected() && !correctList.get(i)) {
                    button.setBackground(COLOR_INCORRECT_BG);
                }
            }
        }
    }
    
    private void showResultDialog(String name, int score) {
        JOptionPane.showMessageDialog(this,
                String.format("Sinh viên: %s\nĐiểm số: %d/%d", name, score, questions.size()),
                "Kết quả bài thi",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
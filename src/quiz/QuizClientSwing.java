package quiz;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuizClientSwing extends JFrame {
    private List<Question> questions;
    private List<List<JRadioButton>> optionButtons;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private JTextField nameField;

    private Timer timer;
    private int timeLeftInSeconds = 600; // 10 phút
    private JLabel timerLabel;
    
    private boolean isSubmitted = false; // Biến cờ để kiểm tra đã nộp bài chưa

    // --- CÁC HẰNG SỐ CHO GIAO DIỆN ---
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_TIMER = new Font("Segoe UI", Font.BOLD, 22);

    private static final Color COLOR_PRIMARY = new Color(0, 123, 255);
    private static final Color COLOR_BACKGROUND = new Color(245, 247, 250);
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_WARNING = new Color(255, 193, 7);
    private static final Color COLOR_BORDER = new Color(222, 226, 230);
    private static final Color COLOR_SELECTED_BG = new Color(236, 240, 241); // Màu khi chọn đáp án

    private static final Color COLOR_CORRECT_BG = new Color(212, 237, 218);
    private static final Color COLOR_INCORRECT_BG = new Color(248, 215, 218);


    public QuizClientSwing(List<Question> questions, ObjectOutputStream out,
                           ObjectInputStream in, Socket socket) {
        this.questions = questions;
        this.out = out;
        this.in = in;
        this.socket = socket;
        this.optionButtons = new ArrayList<>();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeUI();
        startTimer();
    }

    private void initializeUI() {
        setTitle("Ứng dụng Trắc nghiệm Online");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);

        add(createHeaderPanel(), BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(createMainPanel());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        add(createFooterPanel(), BorderLayout.SOUTH);

        setSize(850, 750);
        setLocationRelativeTo(null);
        setIconImage(createIcon());
        setVisible(true);
    }
    
    private void startTimer() {
        timer = new Timer(1000, e -> {
            timeLeftInSeconds--;
            int minutes = timeLeftInSeconds / 60;
            int seconds = timeLeftInSeconds % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

            if (timeLeftInSeconds <= 60 && timeLeftInSeconds > 10) {
                timerLabel.setForeground(COLOR_WARNING);
            } else if (timeLeftInSeconds <= 10) {
                timerLabel.setForeground(COLOR_ERROR);
            }

            if (timeLeftInSeconds <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Đã hết giờ làm bài!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                submitAnswers();
            }
        });
        timer.start();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(COLOR_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel titleLabel = new JLabel("BÀI THI TRẮC NGHIỆM");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_PRIMARY);

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        timerPanel.setBackground(COLOR_CARD);
        
        ImageIcon clockIconResource = new ImageIcon("clock.png"); 
        JLabel clockIcon = new JLabel(clockIconResource);
        
        timerLabel = new JLabel(String.format("%02d:00", timeLeftInSeconds / 60));
        timerLabel.setFont(FONT_TIMER);
        timerLabel.setForeground(new Color(52, 58, 64));
        
        timerPanel.add(clockIcon);
        timerPanel.add(timerLabel);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(timerPanel, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        mainPanel.add(createNameSection());
        mainPanel.add(Box.createVerticalStrut(30));

        for (int i = 0; i < questions.size(); i++) {
            mainPanel.add(createQuestionPanel(questions.get(i), i));
            mainPanel.add(Box.createVerticalStrut(20));
        }
        return mainPanel;
    }

    private JPanel createQuestionPanel(Question q, int index) {
        JPanel questionPanel = createCardPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));

        JLabel questionNumber = new JLabel("Câu " + (index + 1));
        questionNumber.setFont(FONT_BOLD);
        questionNumber.setForeground(COLOR_PRIMARY);

        JLabel questionContent = new JLabel("<html><div style='width: 650px; line-height: 1.4;'>" + q.getContent() + "</div></html>");
        questionContent.setFont(FONT_MAIN);
        
        questionPanel.add(questionNumber);
        questionPanel.add(Box.createVerticalStrut(10));
        questionPanel.add(questionContent);
        questionPanel.add(Box.createVerticalStrut(15));

        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> buttonsForThisQuestion = new ArrayList<>();
        char optionChar = 'A';
        for (String optionText : q.getOptions()) {
            JRadioButton rb = createStyledRadioButton(optionText, optionChar++);
            group.add(rb);
            questionPanel.add(rb);
            questionPanel.add(Box.createVerticalStrut(10));
            buttonsForThisQuestion.add(rb);
        }
        
        // === LOGIC MỚI ĐỂ SỬA LỖI TÔ MÀU KHI CHỌN ===
        // Thêm listener cho từng nút để cập nhật màu nền của cả nhóm
        ActionListener selectionListener = e -> {
            for (JRadioButton btn : buttonsForThisQuestion) {
                if (btn.isSelected()) {
                    btn.setBackground(COLOR_SELECTED_BG);
                } else {
                    btn.setBackground(COLOR_CARD);
                }
            }
        };

        for (JRadioButton rb : buttonsForThisQuestion) {
            rb.addActionListener(selectionListener);
        }
        // === KẾT THÚC LOGIC MỚI ===

        optionButtons.add(buttonsForThisQuestion);
        return questionPanel;
    }

    private JRadioButton createStyledRadioButton(String text, char letter) {
        JRadioButton rb = new JRadioButton("<html><b style='color: #007BFF;'>" + letter + ".</b> " + text + "</html>");
        rb.setFont(FONT_MAIN);
        rb.setBackground(COLOR_CARD);
        rb.setFocusPainted(false);
        rb.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Đã xóa MouseListener gây lỗi ở đây
        
        return rb;
    }
    
    private void submitAnswers() {
        if(isSubmitted) return; // Chặn nộp bài nhiều lần
        isSubmitted = true;
        
        if (timer != null) timer.stop();
        
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty() || name.equals("Nhập họ và tên của bạn...")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                isSubmitted = false; // cho phép nộp lại nếu chỉ là lỗi nhập tên
                return;
            }
            
            int unanswered = 0;
            for (List<JRadioButton> buttons : optionButtons) {
                if (buttons.stream().noneMatch(AbstractButton::isSelected)) unanswered++;
            }

            if (unanswered > 0) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Bạn còn " + unanswered + " câu chưa trả lời.\nBạn có chắc chắn muốn nộp bài không?",
                        "Xác nhận nộp bài", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    isSubmitted = false; // Cho phép nộp lại nếu hủy
                    return;
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
                    out.writeObject(name);
                    out.writeObject(answers);
                    out.flush();

                    Object[] resp = (Object[]) in.readObject();
                    int score = (int) resp[0];
                    @SuppressWarnings("unchecked")
                    List<Boolean> correctList = (List<Boolean>) resp[1];
                    String ip = socket.getInetAddress().getHostAddress();

                    SwingUtilities.invokeLater(() -> {
                        showResultDialog(name, score, ip);
                        updateAnswerColors(correctList);
                        
                        // Vô hiệu hóa tất cả các nút và xóa listener để kết quả không bị thay đổi
                        for (List<JRadioButton> buttons : optionButtons) {
                            for(JRadioButton btn : buttons){
                                btn.setEnabled(false);
                                // Xóa listener để không còn sự kiện nào tác động
                                for(ActionListener al : btn.getActionListeners()){
                                    btn.removeActionListener(al);
                                }
                            }
                        }
                    });
                } catch (Exception ex) {
                    isSubmitted = false; // Cho phép thử lại nếu có lỗi mạng
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

                // Luôn tô màu xanh cho đáp án đúng
                if (j == correctAnswerIndex) {
                    button.setBackground(COLOR_CORRECT_BG);
                    button.setText(button.getText().replace("</html>", "  ✔</html>"));
                }

                // Nếu người dùng chọn sai, tô màu đỏ cho lựa chọn đó
                if (button.isSelected() && !correctList.get(i)) {
                    button.setBackground(COLOR_INCORRECT_BG);
                    button.setText(button.getText().replace("</html>", "  ❌</html>"));
                }
            }
        }
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COLOR_BACKGROUND);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 50, 30, 50));

        JButton submitBtn = new JButton("NỘP BÀI THI");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitBtn.setForeground(Color.BLUE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> submitAnswers());
        submitBtn.setBackground(COLOR_SUCCESS);

        footer.add(submitBtn, BorderLayout.EAST);
        return footer;
    }

    private JPanel createNameSection() {
        JPanel nameSection = createCardPanel();
        nameSection.setLayout(new BoxLayout(nameSection, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Thông tin sinh viên");
        nameLabel.setFont(FONT_BOLD);
        nameLabel.setForeground(COLOR_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameField = new JTextField("Nhập họ và tên của bạn...");
        nameField.setFont(FONT_MAIN);
        nameField.setForeground(Color.GRAY);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (nameField.getText().equals("Nhập họ và tên của bạn...")) {
                    nameField.setText("");
                    nameField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (nameField.getText().isEmpty()) {
                    nameField.setText("Nhập họ và tên của bạn...");
                    nameField.setForeground(Color.GRAY);
                }
            }
        });

        nameSection.add(nameLabel);
        nameSection.add(Box.createVerticalStrut(15));
        nameSection.add(nameField);
        return nameSection;
    }
    
    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        return card;
    }
    
    private Image createIcon() {
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) icon.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(COLOR_PRIMARY);
        g2.fillRoundRect(4, 4, 24, 24, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.drawString("Q", 11, 22);
        g2.dispose();
        return icon;
    }
    
    private void showResultDialog(String name, int score, String ip) {
        JOptionPane.showMessageDialog(this,
                String.format("Sinh viên: %s\nĐiểm số: %d/%d", name, score, questions.size()),
                "Kết quả bài thi",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
package quiz;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
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
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(74, 144, 226);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);

    public QuizClientSwing(List<Question> questions, ObjectOutputStream out,
                           ObjectInputStream in, Socket socket) {
        this.questions = questions;
        this.out = out;
        this.in = in;
        this.socket = socket;
        this.optionButtons = new ArrayList<>();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Ung dung Trac nghiem Online");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = createMainPanel();
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        add(scrollPane, BorderLayout.CENTER);

        // Footer with Submit Button
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        // Window properties
        setSize(800, 700);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        
        // Add window icon
        setIconImage(createIcon());
        
        setVisible(true);
    }

    private Image createIcon() {
        // Create a simple icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) icon.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PRIMARY_COLOR);
        g2.fillRoundRect(4, 4, 24, 24, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Q", 11, 21);
        g2.dispose();
        return icon;
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title
        JLabel titleLabel = new JLabel("BAI THI TRAC NGHIEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Vui long doc ky cau hoi va chon dap an dung nhat", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);

        header.add(titlePanel, BorderLayout.CENTER);

        // Question count info
        JLabel countLabel = new JLabel(questions.size() + " cau hoi");
        countLabel.setFont(new Font("Arial", Font.BOLD, 14));
        countLabel.setForeground(Color.WHITE);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(countLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Student name section
        JPanel nameSection = createNameSection();
        mainPanel.add(nameSection);
        mainPanel.add(Box.createVerticalStrut(30));

        // Questions
        for (int qi = 0; qi < questions.size(); qi++) {
            Question q = questions.get(qi);
            JPanel questionPanel = createQuestionPanel(q, qi);
            mainPanel.add(questionPanel);
            mainPanel.add(Box.createVerticalStrut(20));
        }

        return mainPanel;
    }

    private JPanel createNameSection() {
        JPanel nameSection = new JPanel();
        nameSection.setLayout(new BoxLayout(nameSection, BoxLayout.Y_AXIS));
        nameSection.setBackground(CARD_COLOR);
        nameSection.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Title
        JLabel nameLabel = new JLabel("Thong tin sinh vien");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(PRIMARY_COLOR);

        // Name field
        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        nameField.setBackground(Color.WHITE);
        
        // Placeholder effect
        nameField.setText("Nhap ho va ten cua ban...");
        nameField.setForeground(Color.GRAY);
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (nameField.getText().equals("Nhap ho va ten cua ban...")) {
                    nameField.setText("");
                    nameField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (nameField.getText().isEmpty()) {
                    nameField.setText("Nhap ho va ten cua ban...");
                    nameField.setForeground(Color.GRAY);
                }
            }
        });

        nameSection.add(nameLabel);
        nameSection.add(Box.createVerticalStrut(10));
        nameSection.add(nameField);

        return nameSection;
    }

    private JPanel createQuestionPanel(Question q, int index) {
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setBackground(CARD_COLOR);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Question number and content
        JLabel questionNumber = new JLabel("Câu " + (index + 1));
        questionNumber.setFont(new Font("Arial", Font.BOLD, 16));
        questionNumber.setForeground(PRIMARY_COLOR);

        JLabel questionContent = new JLabel("<html><div style='width: 600px; padding: 10px 0;'>" 
            + q.getContent() + "</div></html>");
        questionContent.setFont(new Font("Arial", Font.PLAIN, 15));
        questionContent.setForeground(new Color(33, 37, 41));

        questionPanel.add(questionNumber);
        questionPanel.add(Box.createVerticalStrut(10));
        questionPanel.add(questionContent);
        questionPanel.add(Box.createVerticalStrut(15));

        // Options
        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> buttons = new ArrayList<>();

        for (int i = 0; i < q.getOptions().size(); i++) {
            JRadioButton rb = createStyledRadioButton(q.getOptions().get(i), (char)('A' + i));
            group.add(rb);
            questionPanel.add(rb);
            questionPanel.add(Box.createVerticalStrut(10));
            buttons.add(rb);
            
            // Add selection effect using border
            final CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            );
            
            final CompoundBorder selectedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)
            );
            
            rb.addActionListener(e -> {
                // Reset all buttons to default border
                for (JRadioButton button : buttons) {
                    if (!button.isSelected()) {
                        button.setBorder(defaultBorder);
                    }
                }
                // Set selected button border
                if (rb.isSelected()) {
                    rb.setBorder(selectedBorder);
                }
            });
        }

        optionButtons.add(buttons);
        return questionPanel;
    }

    private JRadioButton createStyledRadioButton(String text, char letter) {
        JRadioButton rb = new JRadioButton("<html><b>" + letter + ".</b> " + text + "</html>");
        rb.setFont(new Font("Arial", Font.PLAIN, 14));
        rb.setBackground(CARD_COLOR);
        rb.setOpaque(true);
        rb.setFocusPainted(false);
        
        // Use border for visual effects instead of background
        final CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        );
        
        final CompoundBorder hoverBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(11, 14, 11, 14)  // Adjust padding for thicker border
        );
        
        final CompoundBorder selectedBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
            BorderFactory.createEmptyBorder(11, 14, 11, 14)
        );
        
        rb.setBorder(defaultBorder);
        
        // Hover effect using border
        rb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!rb.isSelected()) {
                    rb.setBorder(hoverBorder);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!rb.isSelected()) {
                    rb.setBorder(defaultBorder);
                }
            }
        });
        
        return rb;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BACKGROUND_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Progress info
        JLabel progressLabel = new JLabel("Hay kiem tra lai cac cau tra loi truoc khi nop bai");
        progressLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        progressLabel.setForeground(SECONDARY_COLOR);

        // Submit button
        JButton submitBtn = new JButton("NOP BAI THI");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        submitBtn.setBackground(SUCCESS_COLOR);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Button hover effect
        submitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                submitBtn.setBackground(new Color(34, 139, 58));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                submitBtn.setBackground(SUCCESS_COLOR);
            }
        });

        submitBtn.addActionListener(e -> submitAnswers());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(submitBtn);

        footer.add(progressLabel, BorderLayout.WEST);
        footer.add(buttonPanel, BorderLayout.EAST);

        return footer;
    }

    private void submitAnswers() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty() || name.equals("Nhap ho va ten cua ban...")) {
                showStyledMessage("Vui long nhap ten sinh vien!", "Thong bao", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }

            // Check if all questions are answered
            int unanswered = 0;
            for (List<JRadioButton> buttons : optionButtons) {
                boolean hasSelection = buttons.stream().anyMatch(JRadioButton::isSelected);
                if (!hasSelection) unanswered++;
            }

            if (unanswered > 0) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Ban con " + unanswered + " cau chua tra loi.\nBan co muon nop bai khong?",
                    "Xac nhan nop bai",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
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

            // Disable submit button to prevent multiple submissions
            Component[] components = ((JPanel)getContentPane().getComponent(2)).getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    comp.setEnabled(false);
                    ((JButton)comp).setText("Dang xu ly...");
                }
            }

            new Thread(() -> {
                try {
                    System.out.println("Sending name: " + name);
                    out.writeObject(name);
                    out.flush();
                    
                    System.out.println("Sending answers: " + answers);
                    out.writeObject(answers);
                    out.flush();

                    System.out.println("Waiting for response...");
                    Object resp = in.readObject();
                    System.out.println("Received response: " + resp);
                    
                    AtomicInteger score = new AtomicInteger(0);
                    List<Boolean> correctList = new ArrayList<>();

                    if (resp instanceof Object[]) {
                        Object[] arr = (Object[]) resp;
                        if (arr.length > 0 && arr[0] instanceof Number)
                            score.set(((Number) arr[0]).intValue());
                        if (arr.length > 1 && arr[1] instanceof List<?>) {
                            for (Object o : (List<?>) arr[1])
                                correctList.add(Boolean.TRUE.equals(o));
                        }
                    } else if (resp instanceof Number) {
                        score.set(((Number) resp).intValue());
                    }

                    String ip = socket.getLocalAddress().getHostAddress();

                    SwingUtilities.invokeLater(() -> {
                        // Re-enable submit button
                        Component[] comps = ((JPanel)getContentPane().getComponent(2)).getComponents();
                        for (Component comp : comps) {
                            if (comp instanceof JButton) {
                                comp.setEnabled(true);
                                ((JButton)comp).setText("NOP BAI THI");
                            }
                        }
                        
                        // Show result with styled dialog
                        showResultDialog(name, score.get(), ip);

                        // Update UI to show correct/incorrect answers
                        updateAnswerColors(correctList);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        // Re-enable submit button
                        Component[] comps = ((JPanel)getContentPane().getComponent(2)).getComponents();
                        for (Component comp : comps) {
                            if (comp instanceof JButton) {
                                comp.setEnabled(true);
                                ((JButton)comp).setText("NOP BAI THI");
                            }
                        }
                        showStyledMessage("Loi khi nop bai: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
            showStyledMessage("Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResultDialog(String name, int score, String ip) {
        // Create custom dialog
        JDialog resultDialog = new JDialog(this, "Ket qua bai thi", true);
        resultDialog.setLayout(new BorderLayout());
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        contentPanel.setBackground(Color.WHITE);
        
        // Score
        JLabel scoreLabel = new JLabel("Diem so: " + score + "/" + questions.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(score >= questions.size() * 0.8 ? SUCCESS_COLOR : 
                                 score >= questions.size() * 0.6 ? WARNING_COLOR : ERROR_COLOR);
        
        // Student name
        JLabel nameLabel = new JLabel("Sinh vien: " + name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setForeground(SECONDARY_COLOR);
        
        // IP
        JLabel ipLabel = new JLabel("IP: " + ip, SwingConstants.CENTER);
        ipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        ipLabel.setForeground(SECONDARY_COLOR);
        
        // Percentage
        double percentage = (double) score / questions.size() * 100;
        JLabel percentLabel = new JLabel(String.format("Ty le dung: %.1f%%", percentage), SwingConstants.CENTER);
        percentLabel.setFont(new Font("Arial", Font.BOLD, 16));
        percentLabel.setForeground(PRIMARY_COLOR);
        
        contentPanel.add(scoreLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(ipLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(percentLabel);
        
        // OK button
        JButton okButton = new JButton("Đóng");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(PRIMARY_COLOR);
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> resultDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);
        
        resultDialog.add(contentPanel, BorderLayout.CENTER);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);
        resultDialog.setSize(350, 250);
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setVisible(true);
    }

    private void updateAnswerColors(List<Boolean> correctList) {
        for (int i = 0; i < optionButtons.size() && i < correctList.size(); i++) {
            List<JRadioButton> buttons = optionButtons.get(i);
            boolean isCorrect = correctList.get(i);
            
            for (int j = 0; j < buttons.size(); j++) {
                JRadioButton button = buttons.get(j);
                
                if (j == questions.get(i).getAnswerIndex()) {
                    // Correct answer - always green
                    button.setBackground(new Color(40, 167, 69, 150));
                    button.setForeground(Color.WHITE);
                } else if (button.isSelected() && !isCorrect) {
                    // Wrong selected answer - red
                    button.setBackground(new Color(220, 53, 69, 150));
                    button.setForeground(Color.WHITE);
                } else {
                    // Keep original color for unselected options
                    button.setBackground(CARD_COLOR);
                    button.setForeground(Color.BLACK);
                }
            }
        }
    }

    private void showStyledMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}
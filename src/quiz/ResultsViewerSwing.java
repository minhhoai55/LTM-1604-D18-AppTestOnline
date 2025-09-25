package quiz;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResultsViewerSwing extends JFrame {

    private DefaultTableModel tableModel;
    private JTable resultsTable;
    private JLabel totalStudentsLabel;
    private JLabel averageScoreLabel;
    private JLabel refreshStatusLabel;
    private Timer refreshTimer;
    private boolean isAutoRefreshEnabled = true;
    private JButton toggleRefreshButton;

    // --- CÁC HẰNG SỐ GIAO DIỆN ---
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_STATS = new Font("Segoe UI", Font.BOLD, 16);
    private static final Color COLOR_PRIMARY = new Color(0, 123, 255);
    private static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_WARNING = new Color(255, 193, 7);
    private static final Color COLOR_BORDER = new Color(222, 226, 230);
    private static final Color COLOR_HEADER_BG = new Color(248, 249, 250);
    private static final Color COLOR_TEXT_DARK = new Color(52, 58, 64);
    private static final Color COLOR_TEXT_LIGHT = new Color(108, 117, 125);
    
    // THÊM: Màu nền chính cho ứng dụng
    private static final Color APP_BACKGROUND_COLOR = new Color(45, 52, 71);

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/BTLQuiz?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public ResultsViewerSwing() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeUI();
        startAutoRefresh();
    }

    private void initializeUI() {
        setTitle("Bảng Kết Quả Thi Trắc Nghiệm");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // SỬA ĐỔI: Bắt đầu cấu trúc giao diện nền và bo tròn
        // 1. Panel nền chính với màu tối
        JPanel backgroundPanel = new JPanel(new BorderLayout());
        backgroundPanel.setBackground(APP_BACKGROUND_COLOR);
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Khoảng đệm xung quanh
        setContentPane(backgroundPanel);

        // 2. Panel nội dung màu trắng, bo tròn
        RoundedPanel mainRoundedPanel = new RoundedPanel(30, Color.WHITE);
        mainRoundedPanel.setLayout(new BorderLayout());

        // 3. Thêm các thành phần gốc vào panel bo tròn
        mainRoundedPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainRoundedPanel.add(createMainPanel(), BorderLayout.CENTER);
        mainRoundedPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        // 4. Thêm panel bo tròn vào panel nền
        backgroundPanel.add(mainRoundedPanel, BorderLayout.CENTER);
        
        refreshData();
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setOpaque(false); // Làm trong suốt để hiển thị nền trắng của panel cha
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel titleLabel = new JLabel("Bảng Kết Quả Thi");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_TEXT_DARK);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        statsPanel.setOpaque(false); // Làm trong suốt

        totalStudentsLabel = new JLabel("Tổng SV: 0");
        totalStudentsLabel.setFont(FONT_STATS);
        
        averageScoreLabel = new JLabel("Điểm TB: 0.0");
        averageScoreLabel.setFont(FONT_STATS);

        statsPanel.add(totalStudentsLabel);
        statsPanel.add(averageScoreLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);

        return headerPanel;
    }
    
    private void updateStatistics(List<ResultData> results) {
        totalStudentsLabel.setText("Tổng SV: " + results.size());
        if (!results.isEmpty()) {
            double average = results.stream().mapToInt(r -> r.score).average().orElse(0.0);
            averageScoreLabel.setText(String.format("Điểm TB: %.1f", average));
        } else {
            averageScoreLabel.setText("Điểm TB: 0.0");
        }
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setOpaque(false); // Làm trong suốt
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        mainPanel.add(createControlPanel(), BorderLayout.NORTH);

        String[] columns = {"STT", "Mã Sinh Viên", "Tên Sinh Viên", "Điểm", "Môn Thi", "Địa Chỉ IP", "Thời Gian Nộp Bài", "Đánh Giá"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        setupTable();

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE); // Đặt nền cho vùng chứa bảng
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setOpaque(false); // Làm trong suốt

        JLabel infoLabel = new JLabel("Dữ liệu tự động làm mới sau mỗi 5 giây.");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(COLOR_TEXT_LIGHT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false); // Làm trong suốt
        
        toggleRefreshButton = createStyledButton("Tắt Tự Động Làm Mới", COLOR_WARNING);
        toggleRefreshButton.addActionListener(e -> toggleAutoRefresh());

        JButton refreshButton = createStyledButton("Làm mới", COLOR_PRIMARY);
        refreshButton.addActionListener(e -> refreshData());

        JButton exportButton = createStyledButton("Xuất CSV", COLOR_SUCCESS);
        exportButton.addActionListener(e -> exportToCSV());

        JButton clearButton = createStyledButton("Xóa Tất Cả", COLOR_ERROR);
        clearButton.addActionListener(e -> clearAllData());

        buttonPanel.add(toggleRefreshButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);

        controlPanel.add(infoLabel, BorderLayout.WEST);
        controlPanel.add(buttonPanel, BorderLayout.EAST);

        return controlPanel;
    }

    private void setupTable() {
        resultsTable.setFont(FONT_MAIN);
        resultsTable.setRowHeight(40);
        resultsTable.setGridColor(COLOR_BORDER);
        resultsTable.setBackground(Color.WHITE); // Nền bảng
        resultsTable.setSelectionBackground(COLOR_PRIMARY.brighter());
        resultsTable.setSelectionForeground(Color.WHITE);
        resultsTable.setShowGrid(true);

        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_TEXT_DARK);
        header.setPreferredSize(new Dimension(100, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(60);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        resultsTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        resultsTable.getColumnModel().getColumn(7).setPreferredWidth(120);

        resultsTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false); // Làm trong suốt
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        refreshStatusLabel = new JLabel("Đang tải dữ liệu...");
        refreshStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshStatusLabel.setForeground(COLOR_TEXT_LIGHT);

        footerPanel.add(refreshStatusLabel, BorderLayout.WEST);
        return footerPanel;
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(5000, e -> {
            if (isAutoRefreshEnabled) {
                refreshData();
            }
        });
        refreshTimer.start();
    }
    
    private void toggleAutoRefresh() {
        isAutoRefreshEnabled = !isAutoRefreshEnabled;
        toggleRefreshButton.setText(isAutoRefreshEnabled ? "Tắt Tự Động Làm Mới" : "Bật Tự Động Làm Mới");
        toggleRefreshButton.setBackground(isAutoRefreshEnabled ? COLOR_WARNING : COLOR_SUCCESS);
    }

    private void refreshData() {
        new SwingWorker<List<ResultData>, Void>() {
            @Override
            protected List<ResultData> doInBackground() throws Exception {
                return fetchResultsFromDB();
            }

            @Override
            protected void done() {
                try {
                    List<ResultData> results = get();
                    updateTable(results);
                    updateStatistics(results);
                    if(isAutoRefreshEnabled) {
                        refreshStatusLabel.setText("Cập nhật lần cuối lúc: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                        refreshStatusLabel.setForeground(COLOR_SUCCESS);
                    }
                } catch (Exception e) {
                    refreshStatusLabel.setText("Lỗi: " + e.getMessage());
                    refreshStatusLabel.setForeground(COLOR_ERROR);
                }
            }
        }.execute();
    }

    private void updateTable(List<ResultData> results) {
        tableModel.setRowCount(0);
        int stt = 1;
        for (ResultData result : results) {
            String rating = getRating(result.score);
            tableModel.addRow(new Object[]{stt++, result.studentId, result.name, result.score, result.subject, result.ip, result.time, rating});
        }
    }

    private String getRating(int score) {
        if (score >= 9) return "Xuất sắc";
        if (score >= 7) return "Giỏi";
        if (score >= 5) return "Khá";
        return "Yếu";
    }

    private List<ResultData> fetchResultsFromDB() throws SQLException {
        List<ResultData> list = new ArrayList<>();
        String sql = "SELECT student_id, name, score, subject, ip, time FROM results ORDER BY time DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ResultData(
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getInt("score"),
                    rs.getString("subject"),
                    rs.getString("ip"),
                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(rs.getTimestamp("time"))
                ));
            }
        }
        return list;
    }
    
    private void clearAllData() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa TẤT CẢ dữ liệu kết quả không?", "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("TRUNCATE TABLE results");
                refreshData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file CSV");
        fileChooser.setSelectedFile(new java.io.File("ket_qua_thi_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile(), java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write('\ufeff'); // BOM for UTF-8 Excel compatibility
                
                String[] columns = {"STT","Mã Sinh Viên","Tên Sinh Viên","Điểm","Môn Thi","Địa Chỉ IP","Thời Gian Nộp Bài","Đánh Giá"};
                writer.write(String.join(",", columns) + "\n");
                
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        writer.write("\"" + tableModel.getValueAt(i, j).toString() + "\"");
                        if (j < tableModel.getColumnCount() - 1) writer.write(",");
                    }
                    writer.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Xuất file CSV thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file CSV: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static class ResultData {
        final String studentId;
        final String name;
        final int score;
        final String subject;
        final String ip;
        final String time;
        ResultData(String studentId, String name, int score, String subject, String ip, String time) {
            this.studentId = studentId;
            this.name = name;
            this.score = score;
            this.subject = subject;
            this.ip = ip;
            this.time = time;
        }
    }
    
    private static class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // SỬA: Thay đổi màu nền xen kẽ
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
            }
            
            setHorizontalAlignment(column == 2 || column == 4 ? SwingConstants.LEFT : SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            c.setForeground(COLOR_TEXT_DARK);
            c.setFont(FONT_MAIN);

            if (column == 3 && value instanceof Integer) {
                int score = (Integer) value;
                if (score >= 7) c.setForeground(COLOR_SUCCESS);
                else if (score >= 5) c.setForeground(COLOR_WARNING);
                else c.setForeground(COLOR_ERROR);
                setFont(FONT_BOLD);
            }

            if (column == 7 && value != null) {
                String rating = value.toString();
                switch (rating) {
                    case "Xuất sắc": c.setForeground(COLOR_SUCCESS); break;
                    case "Giỏi": c.setForeground(COLOR_PRIMARY); break;
                    case "Khá": c.setForeground(COLOR_WARNING); break;
                    case "Yếu": c.setForeground(COLOR_ERROR); break;
                }
                 setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 13));
            }
            
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ResultsViewerSwing::new);
    }
}
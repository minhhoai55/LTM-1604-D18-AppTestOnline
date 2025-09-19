package quiz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultsViewerSwing extends JFrame {
    private DefaultTableModel tableModel;
    private JTable resultsTable;
    private JLabel totalStudentsLabel;
    private JLabel averageScoreLabel;
    private JLabel refreshStatusLabel;
    private Timer refreshTimer;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(74, 144, 226);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    private static final Color HEADER_COLOR = new Color(52, 58, 64);

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/BTLQuiz?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public ResultsViewerSwing() {
        initializeUI();
        startAutoRefresh();
    }

    private void initializeUI() {
        setTitle("Bảng Kết Quả Thi Trắc Nghiệm");
        setSize(1000, 700);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Main Content
        add(createMainPanel(), BorderLayout.CENTER);
        
        // Footer Panel
        add(createFooterPanel(), BorderLayout.SOUTH);

        // Initial load
        refreshData();

        // Window properties
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        
        // Add window icon
        setIconImage(createIcon());
        
        setVisible(true);
    }

    private Image createIcon() {
        // Create a simple icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = icon.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PRIMARY_COLOR);
        g2.fillRoundRect(4, 4, 24, 24, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("📊", 8, 21);
        g2.dispose();
        return icon;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Title section
        JPanel titleSection = new JPanel();
        titleSection.setLayout(new BoxLayout(titleSection, BoxLayout.Y_AXIS));
        titleSection.setBackground(PRIMARY_COLOR);

        JLabel titleLabel = new JLabel("BẢNG KẾT QUẢ THI", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Theo dõi kết quả thi trắc nghiệm của sinh viên", SwingConstants.LEFT);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));

        titleSection.add(titleLabel);
        titleSection.add(Box.createVerticalStrut(5));
        titleSection.add(subtitleLabel);

        // Statistics section
        JPanel statsPanel = createStatsPanel();
        
        headerPanel.add(titleSection, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(PRIMARY_COLOR);

        totalStudentsLabel = new JLabel("👥 Tổng SV: 0");
        totalStudentsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalStudentsLabel.setForeground(Color.WHITE);
        totalStudentsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        averageScoreLabel = new JLabel("📈 Điểm TB: 0.0");
        averageScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        averageScoreLabel.setForeground(Color.WHITE);
        averageScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statsPanel.add(totalStudentsLabel);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(averageScoreLabel);

        return statsPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table
        String[] columns = {"STT", "Tên Sinh Viên", " Điểm", "Địa Chỉ IP", "Thời Gian", "Đánh Giá"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        setupTable();

        // Table in scroll pane
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1));
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.getViewport().setBackground(CARD_COLOR);

        // Control panel
        JPanel controlPanel = createControlPanel();

        // Add to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(CARD_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Left side - info
        JLabel infoLabel = new JLabel("🔄 Dữ liệu tự động cập nhật mỗi 2 giây");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(SECONDARY_COLOR);

        // Right side - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton refreshButton = createStyledButton("Làm mới", PRIMARY_COLOR);
        refreshButton.addActionListener(e -> refreshData());

        JButton exportButton = createStyledButton("Xuất Excel", SUCCESS_COLOR);
        exportButton.addActionListener(e -> exportToExcel());

        JButton clearButton = createStyledButton("Xóa dữ liệu", ERROR_COLOR);
        clearButton.addActionListener(e -> clearAllData());

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);

        controlPanel.add(infoLabel, BorderLayout.WEST);
        controlPanel.add(buttonPanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        Color originalColor = bgColor;
        Color hoverColor = bgColor.darker();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    private void setupTable() {
        // Table properties
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultsTable.setRowHeight(50);
        resultsTable.setBackground(CARD_COLOR);
        resultsTable.setGridColor(new Color(0, 0, 0, 30));
        resultsTable.setShowVerticalLines(true);
        resultsTable.setShowHorizontalLines(true);
        resultsTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 50));

        // Header styling
        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

        // Column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // STT
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // Name
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // Score
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // IP
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(150);  // Time
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Rating

        // Custom cell renderers
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new CenterAlignRenderer());
        resultsTable.getColumnModel().getColumn(2).setCellRenderer(new ScoreCellRenderer());
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(new RatingCellRenderer());

        // Alternating row colors
        resultsTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(BACKGROUND_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        refreshStatusLabel = new JLabel("Kết nối database thành công");
        refreshStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        refreshStatusLabel.setForeground(SUCCESS_COLOR);

        JLabel versionLabel = new JLabel("Quiz Management System v1.0 | Developed with ❤️");
        versionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        versionLabel.setForeground(SECONDARY_COLOR);
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        footerPanel.add(refreshStatusLabel, BorderLayout.WEST);
        footerPanel.add(versionLabel, BorderLayout.EAST);

        return footerPanel;
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        refreshTimer.start();
    }

    private void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<ResultData> results = fetchResultsFromDB();
                updateTable(results);
                updateStatistics(results);
                refreshStatusLabel.setText("Cập nhật lúc: " + new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
                refreshStatusLabel.setForeground(SUCCESS_COLOR);
            } catch (Exception e) {
                refreshStatusLabel.setText("Lỗi kết nối database");
                refreshStatusLabel.setForeground(ERROR_COLOR);
                e.printStackTrace();
            }
        });
    }

    private void updateTable(List<ResultData> results) {
        tableModel.setRowCount(0);
        for (int i = 0; i < results.size(); i++) {
            ResultData result = results.get(i);
            String rating = getRating(result.score);
            Object[] row = {
                i + 1,
                result.name,
                result.score,
                result.ip,
                result.time,
                rating
            };
            tableModel.addRow(row);
        }
    }

    private void updateStatistics(List<ResultData> results) {
        totalStudentsLabel.setText("Tổng SV: " + results.size());
        
        if (!results.isEmpty()) {
            double average = results.stream().mapToInt(r -> r.score).average().orElse(0.0);
            averageScoreLabel.setText(String.format("Điểm TB: %.1f", average));
        } else {
            averageScoreLabel.setText(" Điểm TB: 0.0");
        }
    }

    private String getRating(int score) {
        if (score >= 8) return "Xuất sắc";
        else if (score >= 7) return "Giỏi";
        else if (score >= 6) return "Khá";
        else if (score >= 5) return "Trung bình";
        else return "Yếu";
    }

    private List<ResultData> fetchResultsFromDB() {
        List<ResultData> list = new ArrayList<>();
        String sql = "SELECT name, score, ip, time FROM results ORDER BY time DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ResultData result = new ResultData();
                result.name = rs.getString("name");
                result.score = rs.getInt("score");
                result.ip = rs.getString("ip");
                result.time = rs.getTimestamp("time").toString();
                list.add(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database connection failed", e);
        }
        
        return list;
    }

    private void exportToExcel() {
        // Simple CSV export (you can enhance this with Apache POI for real Excel)
        try {
            java.io.FileWriter writer = new java.io.FileWriter("quiz_results.csv");
            writer.write("STT,Tên Sinh Viên,Điểm,Địa Chỉ IP,Thời Gian,Đánh Giá\n");
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write(tableModel.getValueAt(i, j).toString());
                    if (j < tableModel.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");
            }
            
            writer.close();
            JOptionPane.showMessageDialog(this, "Đã xuất dữ liệu ra file quiz_results.csv", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAllData() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa TẤT CẢ dữ liệu kết quả?\nHành động này không thể hoàn tác!",
            "Xác nhận xóa dữ liệu",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {
                
                stmt.executeUpdate("DELETE FROM results");
                refreshData();
                JOptionPane.showMessageDialog(this, " Đã xóa tất cả dữ liệu kết quả", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, " Lỗi khi xóa dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Inner classes for table renderers
    private class CenterAlignRenderer extends DefaultTableCellRenderer {
        public CenterAlignRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private class ScoreCellRenderer extends DefaultTableCellRenderer {
        public ScoreCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font.BOLD, 14));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Integer) {
                int score = (Integer) value;
                if (score >= 8) setForeground(SUCCESS_COLOR);
                else if (score >= 6) setForeground(WARNING_COLOR);
                else setForeground(ERROR_COLOR);
            }
            
            return this;
        }
    }

    private class RatingCellRenderer extends DefaultTableCellRenderer {
        public RatingCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font.BOLD, 12));
        }
    }

    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    setBackground(CARD_COLOR);
                } else {
                    setBackground(new Color(248, 249, 250));
                }
            }
            
            return this;
        }
    }

    // Data class
    private static class ResultData {
        String name;
        int score;
        String ip;
        String time;
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
}
package quiz;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ResultsViewerSwing extends JFrame {
    private DefaultListModel<String> listModel;
    private List<String> results;
    private int lastSize = 0;

    public ResultsViewerSwing(List<String> results) {
        this.results = results;
        setTitle("Kết quả thi");
        setSize(400, 300);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        JList<String> resultList = new JList<>(listModel);

        // Custom cell renderer highlight điểm cao
        resultList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            if (value.contains("/")) {
                String[] parts = value.split(":")[1].trim().split("/");
                int score = Integer.parseInt(parts[0]);
                int total = Integer.parseInt(parts[1]);
                if (score == total) label.setBackground(new Color(144, 238, 144)); // điểm cao
                else label.setBackground(Color.WHITE);
            }
            return label;
        });

        JScrollPane scrollPane = new JScrollPane(resultList);
        add(scrollPane, BorderLayout.CENTER);

        new Thread(() -> {
            while (true) {
                if (results.size() != lastSize) {
                    lastSize = results.size();
                    SwingUtilities.invokeLater(() -> {
                        listModel.clear();
                        for (String r : results) listModel.addElement(r);
                        if (!listModel.isEmpty())
                            resultList.ensureIndexIsVisible(listModel.getSize() - 1); // scroll xuống cuối
                    });
                }
                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            }
        }).start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

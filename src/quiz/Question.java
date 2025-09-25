package quiz;

import java.io.Serializable;
import java.util.List;

// SỬA: Giữ nguyên file, chỉ thêm comment để xác nhận đã kiểm tra
public class Question implements Serializable {
    private String content;
    private List<String> options;
    private int answerIndex;

    public Question(String content, List<String> options, int answerIndex) {
        this.content = content;
        this.options = options;
        this.answerIndex = answerIndex;
    }

    public String getContent() {
        return content;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }
}
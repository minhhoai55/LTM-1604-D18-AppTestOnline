package quiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private String content;
    private List<String> options;
    private int answerIndex;

    public Question() {
        // Constructor mặc định
    }

    public Question(String content, List<String> options, int answerIndex) {
        this.content = content;
        this.options = new ArrayList<>(options);
        this.answerIndex = answerIndex;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getAnswerIndex() { return answerIndex; }
    public void setAnswerIndex(int answerIndex) { this.answerIndex = answerIndex; }
}

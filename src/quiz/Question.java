package quiz;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Model Question - tương thích với JSON từ MockAPI và với serialization Java.
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;            // id từ MockAPI (có thể null)
    private String content;
    private List<String> options;
    private int answerIndex;      // index đáp án đúng (0-based)

    // Constructor mặc định cần cho việc deserialize (ObjectInputStream / JSON libs)
    public Question() {
        // khởi mặc định
    }

    // Constructor dùng trong code khi không có id
    public Question(String content, List<String> options, int answerIndex) {
        this.content = content;
        this.options = new ArrayList<String>(options);
        this.answerIndex = answerIndex;
    }

    // Constructor đầy đủ (nếu cần)
    public Question(String id, String content, List<String> options, int answerIndex) {
        this.id = id;
        this.content = content;
        this.options = new ArrayList<String>(options);
        this.answerIndex = answerIndex;
    }

    // ----- getters / setters -----
    public String getId() {
        return id;
    }

    public void setId(String id) {   // đây là method mà server gọi
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getAnswerIndex() {    // tên getter khớp với server
        return answerIndex;
    }

    public void setAnswerIndex(int answerIndex) {
        this.answerIndex = answerIndex;
    }
}

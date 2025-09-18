package quiz;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuizServer {
    private static final int PORT = 5000;
    private static final List<String> results = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ResultsViewerSwing(results));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("QuizServer đang chạy tại cổng " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client kết nối: " + clientSocket);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                List<Question> questions = fetchQuestionsFromAPI();
                out.writeObject(questions);
                out.flush();

                String studentName = (String) in.readObject();
                InetAddress clientIP = socket.getInetAddress();

                @SuppressWarnings("unchecked")
                List<Integer> answers = (List<Integer>) in.readObject();

                int score = 0;
                for (int i = 0; i < questions.size(); i++) {
                    if (i < answers.size() && answers.get(i) == questions.get(i).getAnswerIndex()) {
                        score++;
                    }
                }

                String result = studentName + " (" + clientIP.getHostAddress() + ") : " 
                                + score + "/" + questions.size();
                results.add(result);

                out.writeObject(score);
                out.flush();

                in.close();
                out.close();
                socket.close();
                System.out.println("Client ngắt kết nối.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private List<Question> fetchQuestionsFromAPI() {
            List<Question> list = new ArrayList<>();
            try {
                URL url = new URL("https://68cb54b1430c4476c34c8db1.mockapi.io/questions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                conn.disconnect();

                JSONArray arr = new JSONArray(sb.toString());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String content = obj.getString("content");

                    JSONArray optsJson = obj.getJSONArray("options");
                    List<String> options = new ArrayList<>();
                    for (int j = 0; j < optsJson.length(); j++) {
                        options.add(optsJson.getString(j));
                    }

                    int answerIndex = obj.getInt("answerIndex");
                    String id = obj.optString("id");

                    Question q = new Question(content, options, answerIndex);
                    q.setId(id);
                    list.add(q);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }
    }
}

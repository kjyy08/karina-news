import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Gemini {
    private final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final String[] GEMINI_MODELS = {
            "gemini-2.0-flash-001",
            "gemini-2.0-pro-exp-02-05",
            "gemini-2.0-flash-exp",
            "gemini-1.5-pro",
            "gemini-exp-1206",
            "gemini-1.5-flash",
            "gemini-1.5-flash-8b"
    };

    private final CircularQueue queue = new CircularQueue(GEMINI_MODELS);

    // https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${API_KEY}
    public String requestText(String prompt) {
        String model = queue.peek();
        String apiKey = System.getenv("GEMINI_API_KEY");
        String apiUrl = GEMINI_BASE_URL + model + ":generateContent?key=" + apiKey;
        String payload = """
                {
                    "contents": [
                        {
                          "role": "user",
                          "parts": [
                            {
                              "text": "%s"
                            }
                          ]
                        },
                    ],
                    "generationConfig": {
                      "temperature": 2,
                      "topK": 40,
                      "topP": 0.95,
                      "maxOutputTokens": 8192,
                      "responseMimeType": "text/plain"
                    }
                }
                """.formatted(prompt);

        HttpClient client = HttpClient.newHttpClient(); // 요청할 클라이언트 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        String result = "";

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            result = response.body()
                    .split("\"text\": \"")[1]  // "text": " 이후 문자열 가져오기
                    .split("\"")[0];

            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());
            System.out.println("result = " + result);

            return result.replace("**", "").replace("'", "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

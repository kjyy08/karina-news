import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SlackBot {
    private static final String SLACK_BASE_URL = "https://hooks.slack.com/services/";

    public void sendSlackMsg(String keyword, String text, String imageUrl) {
        String slackUrl = SLACK_BASE_URL + System.getenv("SLACK_WEBHOOK_URL");

        // JSON 형식에 안전하도록 문자열 이스케이프 처리
        String safeKeyword = escapeJson(keyword);
        String safeText = escapeJson(text);
        String safeImageUrl = escapeJson(imageUrl);

        String payload = """
                 {
                   "text": "📢 오늘의 %s 뉴스 📢",
                   "attachments": [
                     {
                       "color": "#2eb886",
                       "text": "%s",
                       "image_url": "%s"
                     }
                   ]
                 }
                """.formatted(safeKeyword, safeText, safeImageUrl);


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(slackUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 간단한 JSON 이스케이프 유틸리티 함수
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

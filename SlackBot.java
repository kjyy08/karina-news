import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SlackBot {
    private static final String SLACK_BASE_URL = "https://hooks.slack.com/services/";

    public void sendSlackMsg(String keyword, String text, String imageUrl) {
        String slackUrl = SLACK_BASE_URL + System.getenv("SLACK_WEBHOOK_URL");
        String payload = """
                 {
                   "attachments": [
                     {
                       "color": "#2eb886",
                       "author_name": "%s",
                       "title": "%s",
                       "fields": [
                         {
                           "title": "📝 오늘의 뉴스",
                           "value": "%s",
                           "short": true
                         }
                       ],
                       "image_url": "%s"
                     }
                   ]
                 }
                """.formatted(keyword, text, text, imageUrl);

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
}

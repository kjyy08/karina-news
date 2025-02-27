import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class NaverMonitoring {
    private final Logger logger;

    public NaverMonitoring() {
        logger = Logger.getLogger(NaverMonitoring.class.getName());
        logger.info("Monitoring 객체 생성");
        logger.setLevel(Level.SEVERE);
    }

    public static void main(String[] args) {
        NaverMonitoring naverMonitoring = new NaverMonitoring();
        naverMonitoring.getNews("카리나", 10, 1, SortType.DATE);
    }

    public void getNews(String keyword, int display, int start, SortType sort) {
        String imageLink = "";

        try {
            String response = requestAPI("news.json", keyword, display, start, sort);
            String[] tmp = response.split("title\":\"");
            String[] result = new String[display];

            for (int i = 1; i < tmp.length; i++) {
                result[i - 1] = tmp[i].split("\",")[0];
            }

            logger.info(Arrays.toString(result));
            File file = new File("./news-data/%s.txt".formatted(keyword));

            if (!file.exists()) {
                logger.info(file.createNewFile() ? "신규 생성" : "이미 있음");
            }

            try (FileWriter fileWriter = new FileWriter(file)) {
                for (String s : result) {
                    fileWriter.write(s + "\n");
                }
                logger.info("기록 성공");
            }

            logger.info("제목 목록 생성 완료");
            String imageResponse = requestAPI("image", keyword, display, start, SortType.SIM);

            imageLink = imageResponse
                    .split("link\":\"")[new Random().nextInt(10)].split("\",")[0]
                    .split("\\?")[0]
                    .replace("\\", "");
            logger.info(imageLink);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageLink))
                    .build();
            String[] tmp2 = imageLink.split("\\.");
            Path path = Path.of("./news-data/%s.%s".formatted(keyword, tmp2[tmp2.length - 1]));
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String requestAPI(String path, String keyword, int display, int start, SortType sort) throws Exception {
        String naverClientId = System.getenv("NAVER_CLIENT_ID");
        String naverClientSecret = System.getenv("NAVER_CLIENT_SECRET");

        String url = "https://openapi.naver.com/v1/search/%s".formatted(path);
        String params = "query=%s&display=%d&start=%d&sort=%s".formatted(
                keyword, display, start, sort.value
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?" + params))
                .GET()
                .header("X-Naver-Client-Id", naverClientId)
                .header("X-Naver-Client-Secret", naverClientSecret)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info(Integer.toString(response.statusCode()));
            logger.info(response.body());

            return response.body();
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new Exception("연결 에러");
        }
    }
}
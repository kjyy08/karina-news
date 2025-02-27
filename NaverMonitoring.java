import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

            // 폴더 경로와 파일 이름 생성
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            File directory = new File("./news-data/%s".formatted(currentDate));

            // 디렉토리가 없으면 생성
            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs();
                logger.info(dirCreated ? "디렉토리 생성됨" : "디렉토리 이미 존재");
            }

            // 파일 생성
            File file = new File(directory, "%s.txt".formatted(keyword));

            // 파일이 없으면 생성
            if (!file.exists()) {
                boolean fileCreated = file.createNewFile();
                logger.info(fileCreated ? "파일 생성됨" : "파일 이미 존재");
            }

            try (FileWriter fileWriter = new FileWriter(file)) {
                for (String s : result) {
                    fileWriter.write(s + "\n");
                }
                logger.info("기록 성공");
            }

            logger.info("제목 목록 생성 완료");

            // 이미지 다운로드
            String imageResponse = requestAPI("image", keyword, display, start, SortType.SIM);

            // 이미지 URL 추출
            try {
                imageLink = imageResponse
                        .split("link\":\"")[new Random().nextInt(10)].split("\",")[0]
                        .split("\\?")[0]
                        .replace("\\", "");
                logger.info("이미지 URL: " + imageLink);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(imageLink))
                        .build();
                String[] tmp2 = imageLink.split("\\.");
                Path path = Path.of(directory.getAbsolutePath(), "%s.%s".formatted(keyword, tmp2[tmp2.length - 1]));
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(path));
            } catch (Exception e) {
                logger.severe("이미지 다운로드 오류: " + e.getMessage());
            }

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

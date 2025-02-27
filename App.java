import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Stream;

public class App {
    public static void main(String[] args) {
        NaverMonitoring naverMonitoring = new NaverMonitoring();
        SlackBot slackBot = new SlackBot();
        Gemini gemini = new Gemini();

        String keyword = System.getenv("NAVER_KEYWORD");
        naverMonitoring.getNews(keyword, 10, 1, SortType.DATE);

        String txtFilePath = "./news-data/%s.txt".formatted(keyword);
        String content = "";

        try {
            content = new String(Files.readAllBytes(Paths.get(txtFilePath))); // 파일 내용 읽기
        } catch (IOException e) {
            e.printStackTrace();
        }

        String prompt = "%s 의 내용은 %s를 키워드로 네이버에서 오늘의 뉴스 기사를 검색한거야. %s의 뉴스 기사를 핵심 기사 위주 3줄로 요약해서 평문으로 출력해줘."
                .formatted(content, keyword, keyword);
        String response = gemini.requestText(prompt);

        // 개행 문자와 * 제거
        response = response.replaceAll("\\\\n", " ");
        response = response.replaceAll("\t", "");
        response = response.replace("*", "");

        System.out.println("response = " + response);

        String imageUrl = findImageUrl("./news-data/");
        System.out.println("imageUrl = " + imageUrl);
        slackBot.sendSlackMsg(keyword, response, imageUrl);
    }

    public static String findImageUrl(String directoryPath) {
        try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
            return paths
                    .filter(Files::isRegularFile) // 파일만 필터링
                    .map(path -> path.toString().replace("\\", "/")) // 경로를 슬래시(`/`)로 변환
                    .filter(path -> path.matches(".*/.*\\.(jpg|png|jpeg)$")) // jpg, png, jpeg 확장자 체크
                    .findFirst() // 첫 번째 일치 파일 찾기
                    .map(path -> {
                        String fileName = Paths.get(path).getFileName().toString();
                        try {
                            // 파일명을 URL 인코딩 처리
                            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
                            return "https://github.com/kjyy08/karina-news/blob/main/news-data/" + encodedFileName + "?raw=true";
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "";
                        }
                    })
                    .orElse(""); // 일치하는 파일 없으면 빈 문자열 반환
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}

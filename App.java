import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class App {
    public static void main(String[] args) {
        NaverMonitoring naverMonitoring = new NaverMonitoring();
        SlackBot slackBot = new SlackBot();
        Gemini gemini = new Gemini();

        String keyword = System.getenv("NAVER_KEYWORD");
        naverMonitoring.getNews(keyword, 10, 1, SortType.SIM);

        String txtFilePath = "./news-data/%s.txt".formatted(keyword);
        String content = "";

        try {
            content = new String(Files.readAllBytes(Paths.get(txtFilePath))); // 파일 내용 읽기
        } catch (IOException e) {
            e.printStackTrace();
        }

        String prompt = "%s 의 내용은 %s를 키워드로 네이버에서 오늘의 뉴스 기사를 검색한거야. %s의 뉴스 기사를 요약해서 평문으로 출력해줘."
                .formatted(content, keyword, keyword);
        String response = gemini.requestText(prompt);

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
                    .map(path -> "https://github.com/kjyy08/karina-news/blob/main/news-data/" + Paths.get(path).getFileName() + "?raw=true") // URL로 변환
                    .orElse(""); // 일치하는 파일 없으면 빈 문자열 반환
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}



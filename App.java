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

        String imageUrl = findImageUrl("./news-data/", keyword);
        System.out.println("imageUrl = " + imageUrl);
        slackBot.sendSlackMsg(keyword, response, imageUrl);
    }

    public static String findImageUrl(String directoryPath, String keyword) {
        try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
            return paths.filter(Files::isRegularFile) // 1. 파일만 필터링
                    .map(Path::getFileName) // 2. 파일명만 가져오기
                    .map(Path::toString) // 3. 문자열 변환
                    .filter(fileName -> fileName.matches(keyword + "\\.(png|jpg|jpeg)")) // 4. 정규식 검사
                    .findFirst() // 5. 첫 번째 매칭된 파일 찾기
                    .map(fileName -> "https://github.com/kjyy08/karina-monitoring/news-data/" + fileName) // 6. URL 변환
                    .orElse("");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}



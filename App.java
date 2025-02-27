import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    // Logger 인스턴스 생성
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    // 날짜 포맷 상수 정의
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        try {
            // 환경 변수에서 키워드 가져오기 (없을 경우 예외 발생)
            String keyword = Optional.ofNullable(System.getenv("NAVER_KEYWORD"))
                    .orElseThrow(() -> new IllegalArgumentException("NAVER_KEYWORD 환경 변수가 필요합니다"));

            // 필요한 서비스 객체 초기화
            NaverMonitoring naverMonitoring = new NaverMonitoring();
            SlackBot slackBot = new SlackBot();
            Gemini gemini = new Gemini();

            // 네이버에서 뉴스 데이터와 이미지 URL 가져오기
            String imageUrl = naverMonitoring.getNews(keyword, 10, 1, SortType.DATE);

            // 파일에서 뉴스 내용 읽기
            String currentDate = LocalDate.now().format(DATE_FORMATTER);
            String txtFilePath = String.format("./news-data/%s/%s.txt", currentDate, keyword);
            String content = readNewsContent(txtFilePath);

            // Gemini를 사용하여 요약 생성
            String summary = generateSummary(gemini, content, keyword);

            // 결과 로깅 및 Slack으로 전송
            LOGGER.info("생성된 요약: " + summary);
            LOGGER.info("이미지 URL: " + imageUrl);
            slackBot.sendSlackMsg(keyword, summary, imageUrl);

        } catch (Exception e) {
            // 예외 발생 시 로깅 후 프로그램 종료
            LOGGER.log(Level.SEVERE, "애플리케이션 실행 실패", e);
            System.exit(1);
        }
    }

    /**
     * 지정된 경로에서 뉴스 콘텐츠를 읽어오는 메소드
     * @param filePath 파일 경로
     * @return 파일 내용 문자열
     * @throws IOException 파일 읽기 오류 발생 시
     */
    private static String readNewsContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try {
            // 파일 내용 읽기
            return Files.readString(path);
        } catch (NoSuchFileException e) {
            // 파일이 없는 경우 처리
            LOGGER.warning("뉴스 파일을 찾을 수 없음: " + filePath);
            return "이용 가능한 뉴스 내용이 없습니다";
        } catch (IOException e) {
            // 기타 IO 예외 처리
            LOGGER.log(Level.WARNING, "뉴스 파일 읽기 실패: " + filePath, e);
            throw e;
        }
    }

    /**
     * Gemini API를 사용하여 뉴스 요약을 생성하는 메소드
     * @param gemini Gemini API 인스턴스
     * @param content 요약할 뉴스 내용
     * @param keyword 검색 키워드
     * @return 생성된 요약 문자열
     */
    private static String generateSummary(Gemini gemini, String content, String keyword) {
        // 프롬프트 생성
        String prompt = String.format(
                "%s 의 내용은 %s를 키워드로 네이버에서 오늘의 뉴스 기사를 검색한거야. %s의 뉴스 기사를 짧게 요약해서 평문으로 출력해줘.",
                content, keyword, keyword
        );

        // Gemini API 호출 및 응답 정리
        String response = gemini.requestText(prompt);
        // 응답에서 개행 문자와 별표 제거
        return response.replaceAll("\\\\n", "\n").replace("*", "");
    }
}
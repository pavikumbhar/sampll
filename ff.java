import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OAuth2TokenService {

    @Value("${spring.security.oauth2.client.provider.custom.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.custom.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.custom.client-secret}")
    private String clientSecret;

    private final WebClient webClient;

    public OAuth2TokenService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> getToken() {
        return webClient.post()
                .uri(tokenUri)
                .bodyValue(createTokenRequestBody())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseTokenFromResponse);
    }

    private Map<String, String> createTokenRequestBody() {
        Map<String, String> formParameters = new HashMap<>();
        formParameters.put("grant_type", "client_credentials");
        formParameters.put("client_id", clientId);
        formParameters.put("client_secret", clientSecret);
        return formParameters;
    }

    private String parseTokenFromResponse(String response) {
        // Implement your custom token parsing logic here using Jsoup
        Document doc = Jsoup.parse(response);
        // Assuming the token is in an element with id "token" (adjust this as needed)
        Element tokenElement = doc.getElementById("token");
        return tokenElement != null ? tokenElement.text() : null;
    }
}







import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final OAuth2TokenService oAuth2TokenService;

    @Autowired
    public WebClientConfig(OAuth2TokenService oAuth2TokenService) {
        this.oAuth2TokenService = oAuth2TokenService;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter((request, next) -> oAuth2TokenService.getToken()
                        .flatMap(token -> {
                            request.headers().setBearerAuth(token);
                            return next.exchange(request);
                        }))
                .build();
    }
}



<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.14.3</version>
</dependency>

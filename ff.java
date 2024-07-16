import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@Configuration
public class OAuth2ClientConfig {

    @Value("${spring.security.oauth2.client.provider.custom.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.custom.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.custom.client-secret}")
    private String clientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("custom")
                .tokenUri(tokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        DefaultOAuth2AuthorizedClientManager authorizedClientManager = 
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build());

        return authorizedClientManager;
    }
}





import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CustomOAuth2ClientConfig {

    @Bean
    public WebClient customWebClient() {
        return WebClient.builder()
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .map(responseBody -> {
                                // Custom parsing logic for non-JSON response
                                String tokenValue = parseTokenFromResponse(responseBody);
                                Map<String, Object> additionalParameters = new HashMap<>();
                                additionalParameters.put(OAuth2ParameterNames.ACCESS_TOKEN, tokenValue);
                                return OAuth2AccessTokenResponse.withToken(tokenValue)
                                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                                        .additionalParameters(additionalParameters)
                                        .build();
                            })
                            .flatMap(accessTokenResponse -> Mono.just(clientResponse));
                }))
                .build();
    }

    private String parseTokenFromResponse(String response) {
        // Implement your custom token parsing logic here
        return response; // Modify this to extract the actual token
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> accessTokenResponseClient(WebClient customWebClient) {
        DefaultClientCredentialsTokenResponseClient tokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        tokenResponseClient.setWebClient(customWebClient);
        return tokenResponseClient;
    }
}




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OAuth2ClientTokenResponseClientConfig {

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> accessTokenResponseClient(WebClient customWebClient) {
        DefaultClientCredentialsTokenResponseClient tokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        tokenResponseClient.setWebClient(customWebClient);
        return tokenResponseClient;
    }
}

package com.nexavault.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import com.nexavault.exception.FileOperationException;

import reactor.core.publisher.Mono;

@Configuration
public class WebclientConfig {

	@Value("${pinata.api.key}")
	private String pinataApiKey;

	@Value("${pinata.secret.key}")
	private String pinataSecretKey;

	private static final int MAX_SIZE = 100 * 1024 * 1024; // 100 MB limit

	@Bean
	WebClient webClient() {
		return WebClient.builder().baseUrl("https://api.pinata.cloud/pinning")
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE).filter(addApiKeys())
				.exchangeStrategies(ExchangeStrategies.builder().codecs(this::configureCodec).build()).build();
	}

	// Configure buffer size for large file uploads (50 MB)
	private void configureCodec(ClientCodecConfigurer configurer) {
		configurer.defaultCodecs().maxInMemorySize(MAX_SIZE);
	}

	// Exchange filter to add Pinata API keys dynamically
	private ExchangeFilterFunction addApiKeys() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			ClientRequest updatedRequest = ClientRequest.from(clientRequest).header("pinata_api_key", pinataApiKey)
					.header("pinata_secret_api_key", pinataSecretKey).build();
			return Mono.just(updatedRequest);
		});
	}

	// Generic POST method for Pinata Upload
	public Mono<Map<String, Object>> postToPinata(String uri, Object body) {
		return webClient().post().uri(uri).contentType(MediaType.MULTIPART_FORM_DATA).bodyValue(body).retrieve()
				.onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
						errorBody -> Mono.error(new FileOperationException("Upload to Pinata failed: " + errorBody))))
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				});
	}

	// Generic GET method for downloading files from IPFS
	public Mono<byte[]> getFromIPFS(String ipfsHash) {
		String url = "https://gateway.pinata.cloud/ipfs/" + ipfsHash;

		return webClient().get().uri(url).retrieve()
				.onStatus(HttpStatusCode::isError,
						clientResponse -> clientResponse.bodyToMono(String.class)
								.flatMap(errorBody -> Mono.error(new FileOperationException(
										"Download from IPFS failed for hash: " + ipfsHash + ". " + errorBody))))
				.bodyToMono(byte[].class);
	}

	// Generic DELETE method for Unpinning (Deleting)
	public Mono<Void> deleteFromPinata(String ipfsHash) {
		return webClient().delete().uri("/unpin/{ipfsHash}", ipfsHash).retrieve()
				.onStatus(HttpStatusCode::isError,
						clientResponse -> clientResponse.bodyToMono(String.class)
								.flatMap(errorBody -> Mono.error(new FileOperationException(
										"Delete from Pinata failed for hash: " + ipfsHash + ". " + errorBody))))
				.bodyToMono(Void.class);
	}
}

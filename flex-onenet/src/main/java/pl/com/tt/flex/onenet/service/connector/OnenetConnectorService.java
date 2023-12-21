package pl.com.tt.flex.onenet.service.connector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import pl.com.tt.flex.onenet.ApiClient;
import pl.com.tt.flex.onenet.api.ConsumedDataApi;
import pl.com.tt.flex.onenet.api.OfferedServicesApi;
import pl.com.tt.flex.onenet.api.ProvideDataApi;
import pl.com.tt.flex.onenet.api.UserApi;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataEntity;
import pl.com.tt.flex.onenet.model.FileResponse;
import pl.com.tt.flex.onenet.service.connector.mapper.JwtAuthenticationResponseMapper;
import pl.com.tt.flex.onenet.service.connector.mapper.LoginDTOMapper;
import pl.com.tt.flex.onenet.service.connector.mapper.OfferedServiceOnenetResponseMapper;
import pl.com.tt.flex.onenet.service.connector.mapper.ProvideDataDTOMapper;
import pl.com.tt.flex.onenet.service.consumedata.mapper.ConsumeDataMapper;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceFullDTO;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetAuthResponseDTO;
import pl.com.tt.flex.onenet.service.providedata.dto.ProvideDataResponseDTO;
import pl.com.tt.flex.onenet.web.rest.errors.OnenetSystemRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static pl.com.tt.flex.onenet.web.rest.errors.ErrorConstants.*;

/**
 * Klasa odpowiedzialna za wysyłanie i odbieranie danych z API onenet oraz mapowanie obiektów klas zdefiniowanych przez onenet na klasy systemu flex
 */
@Slf4j
@Service
public class OnenetConnectorService {

	private static final String USER_NOT_FOUND = "User Not Found";
	private static final String INCORRECT_PASSWORD = "Incorrect Password";

	private final UserApi userApi;
	private final OfferedServicesApi offeredServicesApi;
	private final ProvideDataApi provideDataApi;
	private final JwtAuthenticationResponseMapper jwtAuthenticationResponseMapper;
	private final LoginDTOMapper loginDTOMapper;
	private final ProvideDataDTOMapper provideDataDTOMapper;
	private final OfferedServiceOnenetResponseMapper offeredServiceOnenetResponseMapper;
	private final ConsumedDataApi consumedDataApi;
	private final ConsumeDataMapper consumeDataMapper;

	public OnenetConnectorService(@Value("${application.onenet.url}") String onenetUrl, final UserApi userApi, final ProvideDataApi provideDataApi, final OfferedServicesApi offeredServicesApi,
			final JwtAuthenticationResponseMapper jwtAuthenticationResponseMapper, final LoginDTOMapper loginDTOMapper,
			final ProvideDataDTOMapper provideDataDTOMapper, final OfferedServiceOnenetResponseMapper offeredServiceOnenetResponseMapper,
			final ConsumedDataApi consumedDataApi, final ConsumeDataMapper consumeDataMapper) {

		setBasePathForApiClient(userApi.getApiClient(), onenetUrl);
		setBasePathForApiClient(offeredServicesApi.getApiClient(), onenetUrl);
		setBasePathForApiClient(provideDataApi.getApiClient(), onenetUrl);
		setBasePathForApiClient(consumedDataApi.getApiClient(), onenetUrl);
		
		this.userApi = userApi;
		this.offeredServicesApi = offeredServicesApi;
		this.provideDataApi = provideDataApi;
		this.consumedDataApi = consumedDataApi;
		this.consumeDataMapper = consumeDataMapper;
		this.jwtAuthenticationResponseMapper = jwtAuthenticationResponseMapper;
		this.loginDTOMapper = loginDTOMapper;
		this.provideDataDTOMapper = provideDataDTOMapper;
		this.offeredServiceOnenetResponseMapper = offeredServiceOnenetResponseMapper;
	}

	/**
	 * Funkcja autoryzuje użytkownika podanym loginem i hasłem w systemie onenet
	 */
	public OnenetAuthResponseDTO authOnenetUser(String username, String password) throws OnenetSystemRequestException {
		try {
			return jwtAuthenticationResponseMapper.toDto(userApi.authenticate(loginDTOMapper.toLoginDTO(username, password)));
		} catch (HttpServerErrorException e) {
			if (e.getMessage().contains(USER_NOT_FOUND)) {
				throw new OnenetSystemRequestException("Onenet system could not find the user with given username",
						WRONG_LOGIN, username);
			}
			if (e.getMessage().contains(INCORRECT_PASSWORD)) {
				throw new OnenetSystemRequestException("Could not authorize onenet user with given password",
						WRONG_PASSWORD, username);
			}
			log.error("Error occured while authorizing onenet user");
			log.error(e.getMessage(), e);
			throw new OnenetSystemRequestException("Unknown problem occured while trying to connect with onenet system",
					PROBLEM_CONNECTING_TO_ONENET);
		}
	}

	/**
	 * Funkcja pobiera z systemu onenet listę usług oferowanych użytkownikowi dla którego został wystawiony podany token
	 */
	public List<OfferedServiceFullDTO> getOfferedServices(String token) {
		setTokenForApiClient(offeredServicesApi.getApiClient(), token);
		List<OfferedServiceFullDTO> offeredServices = new ArrayList<>();
		try {
			for (Map<String, Object> offeredServiceMap : offeredServicesApi.getList1()) {
				offeredServices.add(offeredServiceOnenetResponseMapper.getOfferedServiceFullDTOFromApiResponseMap(offeredServiceMap));
			}
			return offeredServices;
		} catch (Exception e) {
			log.error("Error occured while retrieving offered services from onenet");
			log.error(e.getMessage(), e);
			throw new OnenetSystemRequestException("Unknown problem occured while trying to connect with onenet system",
					PROBLEM_CONNECTING_TO_ONENET);
		}
	}

	/**
	 * Metoda pobiera z systemu OneNet listę konsumowanych danych dla danego użytkownika.
	 * Przed pobraniem danych jest ustawiony token sprawdzany w AbstractOnenetJwtService,
	 * a następnie jest wysyłane zapytanie do Onenet API.
	 *
	 * @param token token aktywnego użytkownika Onenet
	 * @return lista ze skonsumowanymi danymi z Onenetu
	 */
	public List<ConsumeDataEntity> getConsumedData(String token) {
		try {
			setTokenForApiClient(consumedDataApi.getApiClient(), token);
			List<Map<String, Object>> consumeDataList = consumedDataApi.getList2();
			List<ConsumeDataEntity> consumeDataEntities = new ArrayList<>();
			for (Map<String, Object> consumeDataMap : consumeDataList) {
				ConsumeDataEntity consumeDataEntity = consumeDataMapper.toEntity(consumeDataMap);
				consumeDataEntities.add(consumeDataEntity);
			}

			return consumeDataEntities;
		} catch (Exception e) {
			throw new OnenetSystemRequestException("Unknown problem occurred while trying to connect with OneNet system",
					PROBLEM_CONNECTING_TO_ONENET);
		}
	}

	/**
	 * Metoda wysyłająca zapytanie do Onenet API o pobranie pliku o podanym ONS ID.
	 * Przed pobraniem danych jest ustawiony token sprawdzany w AbstractOnenetJwtService,
	 * a następnie jest wysyłane zapytanie do Onenet API.
	 *
	 * @param token token aktywnego użytkownika Onenet
	 * @param onsId identyfikator nadany przez Onenet
	 * @return odpowiedź z API (filedata i retrieved)
	 */
	public FileResponse getConsumeDataFile(String token, String onsId) {
		try {
			setTokenForApiClient(consumedDataApi.getApiClient(), token);
			return consumedDataApi.getObjectData(onsId);
		} catch (Exception e) {
			throw new OnenetSystemRequestException("Unknown problem occurred while trying to connect with OneNet system",
					PROBLEM_CONNECTING_TO_ONENET);
		}
	}

	/**
	 * Funkcja przesyłająca plik dotyczący usługi do systemu onenet, zwraca id pliku w systemie onenet
	 */
	public String postProvideData(String token, String encodedFile, String title, String description,
			String filename, String dataOfferingId, String code) {
		setTokenForApiClient(provideDataApi.getApiClient(), token);
		try {
			return Optional.of(provideDataDTOMapper.toPostDTO(encodedFile, title, description, filename, dataOfferingId, code))
					.map(provideDataApi::post)
					.map(String::valueOf)
					.map(response -> response.substring(response.indexOf('=') + 1, response.length() - 1))
					.orElseThrow(() -> new OnenetSystemRequestException("Onenet POST Provide Data endpoint did not respond with a valid ID", UNEXPECTED_ERROR));
		} catch (RestClientException e) {
			log.error("Error occured while sending data to onenet");
			log.error(e.getMessage(), e);
			throw new OnenetSystemRequestException("Could not send a file", COULD_NOT_SEND_A_FILE);
		}
	}

	public List<ProvideDataResponseDTO> getProvideData(String token) {
		try {
			setTokenForApiClient(provideDataApi.getApiClient(), token);
			List<ProvideDataResponseDTO> provideDataEntities = new ArrayList<>();
			for (Map<String, Object> provideDataMap : provideDataApi.getList()) {
				provideDataEntities.add(provideDataDTOMapper.toResponseDTO(provideDataMap));
			}
			return provideDataEntities;
		} catch (Exception e) {
			throw new OnenetSystemRequestException("Unknown problem occurred while trying to connect with OneNet system",
					PROBLEM_CONNECTING_TO_ONENET);
		}
	}

	private ApiClient setTokenForApiClient(ApiClient apiClient, String token) {
		apiClient.setBearerToken(token);
		return apiClient;
	}

	private ApiClient setBasePathForApiClient(ApiClient apiClient, String basePath) {
		apiClient.setBasePath(basePath);
		return apiClient;
	}

}

package pl.com.tt.flex.onenet.service.onenetuser;

import static pl.com.tt.flex.onenet.web.rest.errors.ErrorConstants.CANNOT_REMOVE_ACTIVE_USER;
import static pl.com.tt.flex.onenet.web.rest.errors.ErrorConstants.NO_ACTIVE_USER_FOUND;
import static pl.com.tt.flex.onenet.web.rest.errors.ErrorConstants.UNEXPECTED_ERROR;
import static pl.com.tt.flex.onenet.web.rest.errors.ErrorConstants.USER_ALREADY_ADDED;

import java.time.Instant;
import java.util.List;

import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.domain.onenetuser.ActiveOnenetUserEntiy;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.repository.onenetuser.ActiveOnenetUserRepository;
import pl.com.tt.flex.onenet.repository.onenetuser.OnenetUserRepository;
import pl.com.tt.flex.onenet.security.SecurityUtils;
import pl.com.tt.flex.onenet.service.common.AbstractOnenetService;
import pl.com.tt.flex.onenet.service.connector.OnenetConnectorService;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetAuthDTO;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetAuthResponseDTO;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetUserDTO;
import pl.com.tt.flex.onenet.service.onenetuser.mapper.OnenetUserMapper;
import pl.com.tt.flex.onenet.web.rest.errors.ObjectValidationException;
import pl.com.tt.flex.onenet.web.rest.errors.OnenetSystemRequestException;

@Slf4j
@Service
public class OnenetUserService extends AbstractOnenetService {

	private final OnenetUserRepository onenetUserRepository;
	private final ActiveOnenetUserRepository activeOnenetUserRepository;
	private final OnenetUserMapper onenetUserMapper;

	public OnenetUserService(final OnenetConnectorService onenetConnectorService,
							 final OnenetUserRepository onenetUserRepository,
							 final ActiveOnenetUserRepository activeOnenetUserRepository,
							 final StrongTextEncryptor encoder,
							 final OnenetUserMapper onenetUserMapper) {
		super(onenetConnectorService, encoder);
		this.onenetUserRepository = onenetUserRepository;
		this.activeOnenetUserRepository = activeOnenetUserRepository;
		this.onenetUserMapper = onenetUserMapper;
	}

	@Transactional
	public OnenetUserDTO addOnenetUser(OnenetAuthDTO onenetAuthDTO) throws OnenetSystemRequestException, ObjectValidationException {
		if (onenetUserRepository.existsByUsername(onenetAuthDTO.getUsername())) {
			throw new ObjectValidationException("Onenet user with given username already exists in the database",
					USER_ALREADY_ADDED, onenetAuthDTO.getUsername());
		}
		OnenetAuthResponseDTO onenetAuthResponse = onenetConnectorService.authOnenetUser(onenetAuthDTO.getUsername(), onenetAuthDTO.getPassword());
		OnenetUserEntity onenetUser = new OnenetUserEntity();
		onenetUser.setUsername(onenetAuthDTO.getUsername());
		onenetUser.setPasswordHash(encoder.encrypt(onenetAuthDTO.getPassword()));
		onenetUser.setEmail(onenetAuthResponse.getEmail());
		onenetUser.setOnenetId(onenetAuthResponse.getOnenetId());
		onenetUser.setTokenHash(encoder.encrypt(onenetAuthResponse.getAccessToken()));
		Instant tokenValidTo = getTokenValidTo(onenetAuthResponse.getAccessToken());
		onenetUser.setTokenValidTo(tokenValidTo);
		return onenetUserMapper.toDto(onenetUserRepository.save(onenetUser));
	}

	@Transactional
	public void setActiveUser(Long onenetUserId) throws ObjectValidationException {
		String currentFlexUsername = getCurrentLoggedInUserName();
		OnenetUserEntity onenetUser = onenetUserRepository.findById(onenetUserId)
				.orElseThrow(() -> new IllegalStateException("Cannot find onenet user with id: " + onenetUserId));
		activeOnenetUserRepository.findByFlexUsernameEquals(currentFlexUsername)
				.ifPresentOrElse(activeOnenetUser -> activeOnenetUser.setActiveOnenetUser(onenetUser), () -> {
					ActiveOnenetUserEntiy activeOnenetUser = new ActiveOnenetUserEntiy();
					activeOnenetUser.setFlexUsername(currentFlexUsername);
					activeOnenetUser.setActiveOnenetUser(onenetUser);
					activeOnenetUserRepository.save(activeOnenetUser);
				});
	}

	@Transactional
	public void removeOnenetUser(Long id) throws ObjectValidationException {
		OnenetUserEntity dbOnenetUser = onenetUserRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException("Cannot find onenet user with id: " + id));
		if (activeOnenetUserRepository.existsByActiveOnenetUser(dbOnenetUser)) {
			throw new ObjectValidationException("Cannot remove active onenet user", CANNOT_REMOVE_ACTIVE_USER);
		}
		onenetUserRepository.delete(dbOnenetUser);
	}

	@Transactional(readOnly = true)
	public OnenetUserEntity getCurrentActiveUser() {
		return activeOnenetUserRepository.findByFlexUsernameEquals(getCurrentLoggedInUserName())
				.map(ActiveOnenetUserEntiy::getActiveOnenetUser)
				.orElseThrow(() -> new ObjectValidationException("Cannot find active onenet user for current logged in flex user",
						NO_ACTIVE_USER_FOUND));
	}

	@Transactional(readOnly = true)
	public List<OnenetUserEntity> getAllOnenetUsers() {
		return onenetUserRepository.findAll();
	}

	private String getCurrentLoggedInUserName() throws ObjectValidationException {
		return SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
				new ObjectValidationException("Could not find currently logged in user", UNEXPECTED_ERROR));
	}

}

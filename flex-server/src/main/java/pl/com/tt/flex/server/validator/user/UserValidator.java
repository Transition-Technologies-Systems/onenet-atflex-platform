package pl.com.tt.flex.server.validator.user;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.validator.ObjectValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.vm.user.ManagedUserVM;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.user.UserResource.ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class UserValidator implements ObjectValidator<UserDTO, String> {

    private static final String ENTITY_TRANSLATE_ERROR_PATH = "error." + ENTITY_NAME + ".";

    private final FspService fspService;
    private final ProductService productService;
    private final UserRepository userRepository;

    public void checkDeletable(Long id) throws ObjectValidationException {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isPresent()) {
            if (fspService.isUserOwnerOfAnyFSP(user.get().getLogin())) {
                throw new ObjectValidationException("User is FSP owner",
                    CANNOT_DELETE_BECAUSE_USER_IS_FSP_OWNER, ENTITY_TRANSLATE_ERROR_PATH,
                    ActivityEvent.USER_DELETED_ERROR, id);
            }
            if (productService.isPsoUserOfAnyProduct(user.get().getLogin())) {
                throw new ObjectValidationException("User has active Product",
                    CANNOT_DELETE_BECAUSE_USER_HAS_ACTIVE_PRODUCT, ENTITY_TRANSLATE_ERROR_PATH,
                    ActivityEvent.USER_DELETED_ERROR, id);
            }
        }
    }

    public void checkCreateRequest(UserDTO userDTO) throws ObjectValidationException {

        if (userDTO.getId() != null) {
            throw new ObjectValidationException("A new user cannot already have an ID",
                CANNOT_CREATE_BECAUSE_NEW_USER_CANNOT_ALREADY_HAVE_ID, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
        if (Objects.nonNull(userDTO.getPassword()) && !checkPasswordLength(userDTO.getPassword())) {
            throw new ObjectValidationException("Wrong password length",
                CANNOT_SET_PASSWORD_DUE_TO_INCORRECT_LENGTH, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
    }

    public void checkUpdateRequest(UserDTO userDTO) throws ObjectValidationException {
        if (userDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

//        Optional<UserEntity> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
//        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
//            throw new EmailAlreadyUsedException();
//        }
//        existingUser = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
//        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
//            throw new UsernameAlreadyUsedException();
//        }
        UserEntity existingUser = userRepository.findById(userDTO.getId()).get();
        if (fspService.isUserOwnerOfAnyFSP(existingUser.getLogin()) && !existingUser.getFsp().getId().equals(userDTO.getFspId())) {
            throw new ObjectValidationException("Cannot change FSP for User who is owner of another FSP",
                CANNOT_CHANGE_USER_FSP_BECAUSE_USER_IS_FSP_OWNER, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
        if (fspService.isUserOwnerOfAnyFSP(existingUser.getLogin()) && !existingUser.getRoles().equals(userDTO.getRoles())) {
            throw new ObjectValidationException("Cannot change role for FSP User owner",
                CANNOT_CHANGE_ROLE_FOR_FSP_USER_OWNER, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
        if (existingUser.getRoles().contains(Role.ROLE_FSP_USER_REGISTRATION) && !userDTO.getRoles().contains(Role.ROLE_FSP_USER_REGISTRATION)) {
            throw new ObjectValidationException("Cannot change role for FSP User candidate (ROLE_FSP_USER_REGISTRATION) whose registration process has not yet completed",
                CANNOT_CHANGE_ROLE_FOR_FSP_USER_CANDIDATE, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
        if (!existingUser.getEmail().equals(userDTO.getEmail())) {
            throw new ObjectValidationException("Cannot change email for existing user",
                CANNOT_CHANGE_EMAIL_FOR_EXISTING_USER, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
        if (!existingUser.getLogin().equals(userDTO.getLogin())) {
            throw new ObjectValidationException("Cannot change login for existing user",
                CANNOT_CHANGE_LOGIN_FOR_EXISTING_USER, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
        if (nonNull(existingUser.getFsp()) && isFspUserActivationOperation(userDTO, existingUser)) {
            throw new ObjectValidationException("Cannot activate User which FSP is not activated",
                CANNOT_ACTIVATE_FSP_USER_WHICH_FSP_IS_NOT_ACTIVATED, ENTITY_TRANSLATE_ERROR_PATH,
                getActivityEvent(userDTO), userDTO.getId());
        }
    }

    private boolean isFspUserActivationOperation(UserDTO userToSave, UserEntity existingUser) {
        FspEntity existingUserFsp = fspService.findFspOfUser(existingUser.getId(), existingUser.getLogin()).get();
        return !existingUserFsp.isActive() && userToSave.isActivated() && !existingUser.isActivated();
    }

    private boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
            password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }

    private ActivityEvent getActivityEvent(UserDTO userDTO) {
        return userDTO.getId() == null ? ActivityEvent.USER_CREATED_ERROR : ActivityEvent.USER_UPDATED_ERROR;
    }

}

package pl.com.tt.flex.server.service.user.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.security.permission.factory.AuthoritiesContainerFactory;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Mapper for the entity {@link UserEntity} and its DTO called {@link UserDTO}.
 * <p>
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
@RequiredArgsConstructor
public class UserMapper {

    private final AuthoritiesContainerFactory authoritiesContainerFactory;

    public List<UserDTO> usersToUserDTOs(List<UserEntity> users) {
        return users.stream()
            .filter(Objects::nonNull)
            .map(this::userToUserDTO)
            .collect(Collectors.toList());
    }

    public UserDTO userToUserDTO(UserEntity user) {
        UserDTO userDTO = new UserDTO(user);
        userDTO.setAuthorities(getAuthorities(user));
        userDTO.setCompanyName(user.getCompanyName());
        return userDTO;
    }

    private Set<String> getAuthorities(UserEntity userEntity) {
        return authoritiesContainerFactory.getUserAuthorities(userEntity.getRoles());
    }

    public List<UserEntity> userDTOsToUsers(List<UserDTO> userDTOs) {
        return userDTOs.stream()
            .filter(Objects::nonNull)
            .map(this::userDTOToUser)
            .collect(Collectors.toList());
    }

    public UserEntity userDTOToUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        } else {
            UserEntity user = new UserEntity();
            user.setId(userDTO.getId());
            user.setLogin(userDTO.getLogin());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setActivated(userDTO.isActivated());
            user.setLangKey(userDTO.getLangKey());
            user.setRoles(nonNull(userDTO.getRoles()) ? userDTO.getRoles() : new HashSet<>());
            return user;
        }
    }

    public UserEntity userFromId(Long id) {
        if (id == null) {
            return null;
        }
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }

    public List<UserMinDTO> usersToUserMinDTOs(List<UserEntity> users) {
        return users.stream()
            .filter(Objects::nonNull)
            .map(this::userToUserMinDTO)
            .collect(Collectors.toList());
    }

    public UserMinDTO userToUserMinDTO(UserEntity user) {
        if (nonNull(user)) {
            return new UserMinDTO(user);
        }
        return null;
    }
}

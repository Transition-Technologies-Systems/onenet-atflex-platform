package pl.com.tt.flex.server.service.user;

import io.github.jhipster.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.UserEntity_;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.service.user.dto.UserCriteria;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

import static pl.com.tt.flex.server.service.common.QueryServiceUtil.setDefaultOrder;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserQueryService extends QueryService<UserEntity> {


    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserQueryService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    @Transactional(readOnly = true)
    public List<UserDTO> findByCriteria(UserCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<UserEntity> specification = createSpecification(criteria);
        return userMapper.usersToUserDTOs(userRepository.findAll(specification));
    }

    @Transactional(readOnly = true)
    public List<UserMinDTO> findMinByCriteria(UserCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<UserEntity> specification = createSpecification(criteria);
        return userMapper.usersToUserMinDTOs(userRepository.findAll(specification));
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> findByCriteria(UserCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<UserEntity> specification = createSpecification(criteria);
        page = setDefaultOrder(page, UserEntity_.ID);
        return userRepository.findAll(specification, page)
            .map(userMapper::userToUserDTO);
    }

    protected Specification<UserEntity> createSpecification(UserCriteria criteria) {
        Specification<UserEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), UserEntity_.id));
            }
            if (criteria.getFirstName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFirstName(), UserEntity_.firstName));
            }
            if (criteria.getLastName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastName(), UserEntity_.lastName));
            }
            if (criteria.getCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getCompanyName(), root -> root.join(UserEntity_.fsp, JoinType.LEFT).get(FspEntity_.companyName)));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), UserEntity_.email));
            }
            if (criteria.getPhoneNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhoneNumber(), UserEntity_.phoneNumber));
            }
            if (criteria.getLogin() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLogin(), UserEntity_.login));
            }
            if (criteria.getActivated() != null) {
                specification = specification.and(buildSpecification(criteria.getActivated(), UserEntity_.activated));
            }
            if (criteria.getDeleted() != null) {
                specification = specification.and(buildSpecification(criteria.getDeleted(), UserEntity_.deleted));
            }
            if (criteria.getRoles() != null) {
                specification = specification.and(buildSpecification(criteria.getRoles(), root -> root.joinSet(UserEntity_.ROLES, JoinType.LEFT)));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), UserEntity_.createdBy));
            }
            if (criteria.getCreatedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), UserEntity_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), UserEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), UserEntity_.lastModifiedDate));
            }

        }
        return specification;
    }

}

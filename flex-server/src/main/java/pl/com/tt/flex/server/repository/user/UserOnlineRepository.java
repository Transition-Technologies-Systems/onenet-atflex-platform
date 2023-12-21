package pl.com.tt.flex.server.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.UserOnlineEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;
import java.util.Set;


@Repository
public interface UserOnlineRepository extends AbstractJpaRepository<UserOnlineEntity, Long> {

    @Query("SELECT u FROM UserEntity u WHERE u.id IN (SELECT DISTINCT uo.user.id FROM UserOnlineEntity uo)")
    Page<UserEntity> findAllLoggedUsers(Pageable pageable);

    @Query(value = "SELECT DISTINCT u.login FROM users u " +
        "INNER JOIN user_role ur ON ur.user_id = u.id \n" +
        "INNER JOIN users_online uo ON uo.user_id = u.id \n" +
        "WHERE ur.role IN :roles", nativeQuery = true)
    List<String> findAllLoggedUsersByRoles(@Param("roles") Set<String> roles);

    @Modifying
    @Query("DELETE FROM UserOnlineEntity o WHERE o.user = :user")
    void deactivateUserToken(@Param("user") UserEntity user);

    @Query("SELECT uo FROM UserOnlineEntity uo WHERE uo.user.login = :login AND uo.addressId = :addressId ORDER BY uo.createdDate DESC")
    List<UserOnlineEntity> findByUserLoginAndAddressId(@Param("login") String login, @Param("addressId") String ipAddress);

    @Query("SELECT uo FROM UserOnlineEntity uo WHERE uo.user.login = :login AND uo.addressId = :addressId ORDER BY uo.createdDate DESC")
    List<UserOnlineEntity> findByLoginAndIpAdress(@Param("login") String login, @Param("addressId") String ipAddress);

    List<UserOnlineEntity> findByUserLogin(String login);

}

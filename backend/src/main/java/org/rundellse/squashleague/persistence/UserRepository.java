package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    User findUserByEmail(String email);

}

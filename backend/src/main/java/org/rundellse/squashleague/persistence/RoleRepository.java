package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.user.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByName(String name);
}

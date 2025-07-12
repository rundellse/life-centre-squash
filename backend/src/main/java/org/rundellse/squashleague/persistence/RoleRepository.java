package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.model.user.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<UserRole, Long> {
    UserRole findByRole(Role role);
}

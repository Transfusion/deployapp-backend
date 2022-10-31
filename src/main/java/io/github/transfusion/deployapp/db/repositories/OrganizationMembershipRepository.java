package io.github.transfusion.deployapp.db.repositories;

import io.github.transfusion.deployapp.db.entities.OrganizationMembership;
import io.github.transfusion.deployapp.db.entities.OrganizationMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, OrganizationMembershipId> {
}
package com.rushtix.core.feature.grouppay.repository;

import com.rushtix.core.domain.entities.GroupPaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupPaymentItemRepository extends JpaRepository<GroupPaymentItem, UUID> {
}

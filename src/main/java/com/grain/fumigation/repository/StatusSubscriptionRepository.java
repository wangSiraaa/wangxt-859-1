package com.grain.fumigation.repository;

import com.grain.fumigation.entity.StatusSubscription;
import com.grain.fumigation.enums.OperationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusSubscriptionRepository extends JpaRepository<StatusSubscription, Long> {

    List<StatusSubscription> findByOperationIdAndActiveTrue(Long operationId);

    List<StatusSubscription> findBySubscriberIdAndActiveTrue(String subscriberId);

    List<StatusSubscription> findBySubscriberRoleAndActiveTrue(OperationRole subscriberRole);

    Optional<StatusSubscription> findByOperationIdAndSubscriberIdAndActiveTrue(Long operationId, String subscriberId);

    boolean existsByOperationIdAndSubscriberIdAndActiveTrue(Long operationId, String subscriberId);
}

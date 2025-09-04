package com.crediya.loan.r2dbc.repository;

import com.crediya.loan.r2dbc.entity.StatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface StatusReactiveRepository extends ReactiveCrudRepository<StatusEntity, UUID>, ReactiveQueryByExampleExecutor<StatusEntity> {

}

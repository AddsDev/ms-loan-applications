package com.crediya.loan.r2dbc.adapter;

import com.crediya.loan.model.common.gateways.CatalogResolverPort;
import com.crediya.loan.model.common.gateways.TraceLoggerPort;
import com.crediya.loan.model.loan.ApplicationStatus;
import com.crediya.loan.model.loan.LoanType;

import com.crediya.loan.r2dbc.repository.LoanTypeReactiveRepository;
import com.crediya.loan.r2dbc.repository.StatusReactiveRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@RequiredArgsConstructor
public class CatalogResolverPortAdapter implements CatalogResolverPort {
    private final ConcurrentMap<String,String> statusIdByName = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,String> statusNameById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,String> loanTypeIdByCode = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,String> loanTypeCodeById = new ConcurrentHashMap<>();

    private final StatusReactiveRepository statusReactiveRepository;
    private final LoanTypeReactiveRepository loanTypeReactiveRepository;
    private final TraceLoggerPort logger;

    @PostConstruct
    public void loadCatalog() {
        // Load statuses and loan types from db
        logger.info("Loading catalog from database");
        statusReactiveRepository.findAll().map(statusEntity -> Map.entry(statusEntity.getStatusId(),statusEntity.getName()))
                .doOnNext(e -> {
                    statusNameById.put(e.getKey().toString(), e.getValue());
                    statusIdByName.put(e.getValue(), e.getKey().toString());
                    logger.info("Loaded status id={} name={}", e.getKey(), e.getValue());
                }).then()
                .thenMany(loanTypeReactiveRepository.findAll())
                .map(typeEntity -> Map.entry(typeEntity.getLoanTypeId(),typeEntity.getCode()))
                .doOnNext(e -> {
                    loanTypeCodeById.put(e.getKey().toString(), e.getValue());
                    loanTypeIdByCode.put(e.getValue(), e.getKey().toString());
                    logger.info("Loaded loan type id={} code={}", e.getKey(), e.getValue());
                }).then().block();
    }


    @Override
    public ApplicationStatus toStatus(String statusId) {
        String name = statusNameById.get(statusId);
        logger.info("Loaded status id={} name={}", statusId, name);
        return name == null ? null : ApplicationStatus.valueOf(name);
    }

    @Override
    public String toStatusId(ApplicationStatus status) {
        logger.info("Loaded status name={} id={}", status.name(), statusIdByName.get(status.name()));
        return statusIdByName.get(status.name());
    }

    @Override
    public LoanType toLoanType(String loanTypeId) {
        String name = loanTypeCodeById.get(loanTypeId);
        logger.info("Loaded loan type id={} code={}", loanTypeId, name);
        return name == null ? null : LoanType.valueOf(name);
    }

    @Override
    public String toLoanTypeId(LoanType type) {
        logger.info("Loaded loan type code={} id={}", type.name(), loanTypeIdByCode.get(type.name()));
        return loanTypeIdByCode.get(type.name());
    }
}

package com.crediya.loan.model.report.gateways;

import com.crediya.loan.model.report.ReportEvent;
import reactor.core.publisher.Mono;

public interface ReportPublisherPort {
    Mono<Void> publish(ReportEvent event);
}

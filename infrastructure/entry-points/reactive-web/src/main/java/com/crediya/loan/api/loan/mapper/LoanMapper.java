package com.crediya.loan.api.loan.mapper;

import com.crediya.loan.api.loan.dto.ApplyForLoanRequest;
import com.crediya.loan.api.loan.dto.ApplyForLoanResponse;
import com.crediya.loan.model.loan.LoanApplication;
import com.crediya.loan.model.loan.parameterobjects.ApplyForLoanCommand;
import com.crediya.loan.model.loan.valueobjects.Amount;
import com.crediya.loan.model.loan.valueobjects.Document;
import com.crediya.loan.model.loan.valueobjects.Email;
import com.crediya.loan.model.loan.valueobjects.TermInMonths;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface LoanMapper {

    @Mapping(target = "type", source = "loanType")
    @Mapping(target = "email", source = "request.email", qualifiedByName = "toEmail")
    @Mapping(target = "document", source = "request.document", qualifiedByName = "toDocument")
    ApplyForLoanCommand toCommand(ApplyForLoanRequest request);


    @Mapping(target = "loanType", source = "type")
    @Mapping(target = "email", source = "command.email", qualifiedByName = "mapEmail")
    @Mapping(target = "document", source = "command.document", qualifiedByName = "mapDocument")
        //@Mapping(target = "email", source = "email", qualifiedByName = "mapEmail")
    ApplyForLoanRequest toRequest(ApplyForLoanCommand command);

    @Mapping(target = "document", source = "identityDocument", qualifiedByName = "mapDocument")
    @Mapping(target = "email", source = "email", qualifiedByName = "mapEmail")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "mapAmount")
    @Mapping(target = "termInMonths", source = "term", qualifiedByName = "mapTerm")
    @Mapping(target = "type", source = "loanType")
    ApplyForLoanResponse toResponse(LoanApplication application);

    @Named("mapEmail")
    default String mapEmail(Email email) {
        return Objects.requireNonNullElse(email.value(), "");
    }

    @Named("toEmail")
    default Email toEmail(String email) {
        return email == null ? null : new Email(email);
    }

    @Named("mapDocument")
    default String mapDocument(Document doc) {
        return doc == null ? null : doc.value();
    }

    @Named("toDocument")
    default Document toDocument(String raw) {
        return raw == null ? null : new Document(raw);
    }

    @Named("mapAmount")
    default BigDecimal mapAmount(Amount a) {
        return (a == null) ? null : a.value().setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Named("toAmount")
    default Amount toAmount(BigDecimal raw) {
        return raw == null ? null : new Amount(raw);
    }

    @Named("mapTerm")
    default int mapTerm(TermInMonths t) {
        return t == null ? 0 : t.value();
    }

    @Named("toTerm")
    default TermInMonths toTerm(Integer raw) {
        return raw == null ? null : new TermInMonths(raw);
    }
}

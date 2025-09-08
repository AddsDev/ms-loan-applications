package com.crediya.loan.model.common.pagers;

import java.util.List;

public record PageResult<T>(List<T> data, int page, int size, Long totalItems) {
    public static <T> PageResult<T> of(List<T> data, int page, int size, Long total) {
        return new PageResult<>(data, page, size, total);
    }
}

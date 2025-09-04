package com.crediya.loan.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HttpError {
    private Integer status = null;
    private String code = null;
    private String message = null;
    private String timestamp = null;
    private String path = null;
}
package com.projectshopbando.shopbandoapi.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ResponseObject<T> {
    @Builder.Default
    private String status = "success";
    private String message;
    private T data;
}

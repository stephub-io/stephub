package io.stephub.server.api.rest;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;

@Data
@ToString
public class PageCriteria {
    @Min(0)
    private int offset = 0;
    @Min(1)
    private int size = 25;
}

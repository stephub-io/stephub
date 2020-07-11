package io.stephub.server.api.rest;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PageResult<T> {
    private int total;
    @Singular
    private List<T> items;
}

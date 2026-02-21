package com.sila.share.pagination;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class EntityResponseHandler<T> {
    private List<T> contents;
    private int page;
    private int pageSize;
    private int totalPages;
    private long total;
    private boolean hasNext;

    public EntityResponseHandler(Page<T> page) {
        this(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }


    public EntityResponseHandler(
            List<T> contents, int page, int pageSize, int totalPages, long total, boolean hasNext) {
        this.contents = new ArrayList<>(contents);
        this.page = page;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.total = total;
        this.hasNext = hasNext;
    }

    public EntityResponseHandler(
            List<T> contents, int page, int pageSize, long total) {
        this.contents = new ArrayList<>(contents);
        this.page = page + 1;
        this.total = total;
        this.pageSize = pageSize;
        this.totalPages = pageSize == 0 ? 1 : (int) Math.ceil((double) total / (double) pageSize);
        this.hasNext = page + 1 < totalPages;
    }
}


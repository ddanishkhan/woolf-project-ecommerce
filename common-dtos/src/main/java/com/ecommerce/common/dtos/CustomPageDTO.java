package com.ecommerce.common.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CustomPageDTO<T> {

    private List<T> data;
    private int pageNumber;         // Current page number (usually 0-indexed)
    private int pageSize;           // Number of items per page
    private long totalElements;     // Total number of items across all pages
    private int totalPages;         // Total number of pages
    private boolean first;          // Is this the first page?
    private boolean last;           // Is this the last page?
    private int numberOfElements;   // Number of elements in the current page

    /**
     *
     * @param data  The list to return data.
     * @param springPage The original Page object to copy metadata from
     */
    public CustomPageDTO(List<T> data, org.springframework.data.domain.Page<?> springPage) {
        this.data = data;
        this.pageNumber = springPage.getNumber();
        this.pageSize = springPage.getSize();
        this.totalElements = springPage.getTotalElements();
        this.totalPages = springPage.getTotalPages();
        this.first = springPage.isFirst();
        this.last = springPage.isLast();
        this.numberOfElements = springPage.getNumberOfElements();
    }
}

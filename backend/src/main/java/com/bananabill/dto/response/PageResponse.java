package com.bananabill.dto.response;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints
 * 
 * @param <T> The type of items in the list
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
    /**
     * Create from Spring Data Page
     */
    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    /**
     * Create simple list response (non-paginated)
     */
    public static <T> PageResponse<T> of(List<T> items) {
        return new PageResponse<>(
                items,
                0,
                items.size(),
                items.size(),
                1,
                true,
                true);
    }
}

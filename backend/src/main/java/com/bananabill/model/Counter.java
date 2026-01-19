package com.bananabill.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Counter document for atomic sequence generation
 * Used to prevent race conditions in bill number generation
 */
@Data
@Document(collection = "counters")
public class Counter {

    @Id
    private String id; // e.g., "bill_number_2601" for Jan 2026

    private long sequence;
}

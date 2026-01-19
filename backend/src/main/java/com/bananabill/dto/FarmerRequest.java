package com.bananabill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerRequest {

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @NotBlank(message = "Name is required")
    private String name;

    private String address;
}

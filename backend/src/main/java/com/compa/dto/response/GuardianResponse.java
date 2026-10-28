package com.compa.dto.response;

import com.compa.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardianResponse {
    private Long id;
    private String name;
    private String lastName;
    private DocumentType documentType;
    private String identityDocument;
    private String relationship;
    private String email;
    private String phoneNumber;
    private Long estudianteId;
    private LocalDate fechaAutorizacion;

}
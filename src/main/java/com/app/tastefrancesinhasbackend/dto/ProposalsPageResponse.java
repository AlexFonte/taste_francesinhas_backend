package com.app.tastefrancesinhasbackend.dto;

import com.app.tastefrancesinhasbackend.dto.ProfileDTO.MyProposalResponse;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({"proposals", "total", "totalPages", "pageNumber", "pageSize"})
public class ProposalsPageResponse extends PageResponse {

    private List<MyProposalResponse> proposals;

    public static ProposalsPageResponse of(Page<MyProposalResponse> page) {
        return ProposalsPageResponse.builder()
                .proposals(page.getContent())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}

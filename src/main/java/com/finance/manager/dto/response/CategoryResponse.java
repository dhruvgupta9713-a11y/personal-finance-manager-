package com.finance.manager.dto.response;

import lombok.Builder;
import lombok.Data;

// response for category info
@Data
@Builder
public class CategoryResponse {

    private String name;
    private String type; // INCOME or EXPENSE
    private boolean isCustom;
}

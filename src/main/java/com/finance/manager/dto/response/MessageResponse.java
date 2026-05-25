package com.finance.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

// generic message response - used for success messages etc
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {

    private String message;

    // sometimes we want to return the user id too (like after registration)
    private Long userId;

    // constructor for when we just need a message
    public MessageResponse(String message) {
        this.message = message;
        this.userId = null;
    }
}

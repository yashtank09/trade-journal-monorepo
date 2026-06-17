package org.tradebook.journal.features.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String residentialAddress;
    private String pan;
    private String currency;
    private String tradingStyle;
}

package org.tradebook.journal.features.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String residentialAddress;
    private String pan;
    private String currency;
    private String tradingStyle;
    private String role;
    private String joinedDate;
}
// 

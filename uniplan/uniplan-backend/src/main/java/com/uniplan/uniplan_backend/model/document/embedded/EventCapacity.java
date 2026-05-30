package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCapacity {

    /*
     * Maximum number of attendees
     */
    private Integer total;

    /*
     * Current number of confirmed registrations
     */
    private Integer registered;

    /*
     * Spots still available
     * available = total - registered
     */
    private Integer available;

    /*
     * Waitlist size (0 = no waitlist)
     */
    private Integer waitlist;

    /*
     * Whether waitlist is enabled
     */
    private Boolean waitlistEnabled;
}

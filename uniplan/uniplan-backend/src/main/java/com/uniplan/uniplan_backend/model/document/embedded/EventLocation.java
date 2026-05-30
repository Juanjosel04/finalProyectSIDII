package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLocation {

    /*
     * Name of the venue
     * Example: Auditorio Principal, Sala de Conferencias B
     */
    private String venue;

    /*
     * Room or specific area
     * Example: Sala 101, Piso 3
     */
    private String room;

    /*
     * Campus or building
     * Example: Campus Norte, Bloque A
     */
    private String campus;

    /*
     * Full address (for off-campus events)
     */
    private String address;

    /*
     * VIRTUAL | IN_PERSON | HYBRID
     */
    private String modality;

    /*
     * Meeting link for virtual events
     */
    private String meetingUrl;
}

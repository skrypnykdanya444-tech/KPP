package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IncidentServiceTest {

    @Test
    void isAllowedTransition_newToConfirmed_returnsTrue() {
        assertTrue(IncidentService.isAllowedTransition(
                IncidentState.NEW, IncidentState.CONFIRMED));
    }

    @Test
    void isAllowedTransition_confirmedToResponse_returnsTrue() {
        assertTrue(IncidentService.isAllowedTransition(
                IncidentState.CONFIRMED, IncidentState.RESPONSE));
    }

    @Test
    void isAllowedTransition_responseToClosed_returnsTrue() {
        assertTrue(IncidentService.isAllowedTransition(
                IncidentState.RESPONSE, IncidentState.CLOSED));
    }

    @Test
    void isAllowedTransition_newToClosed_returnsFalse() {
        assertFalse(IncidentService.isAllowedTransition(
                IncidentState.NEW, IncidentState.CLOSED));
    }

    @Test
    void add_nullIncident_throwsBeforeDb() {
        IncidentService service = new IncidentService();
        assertThrows(IllegalArgumentException.class,
                () -> service.add(null));
    }
}

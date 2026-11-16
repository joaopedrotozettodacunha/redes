package pitiupi.application.presence;

import org.junit.jupiter.api.Test;
import pitiupi.control.HeartbeatManager;
import pitiupi.models.Messages.HeartbeatMessage;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresenceServiceTest {

    @Test
    void listPeerViewsStartsWithBroadcastTarget() {
        PresenceService service = new PresenceService(HeartbeatManager.createForTesting());
        var views = service.listPeerViews();
        assertTrue(views.get(0).isBroadcastTarget());
        assertEquals("Todos", views.get(0).getDisplayName());
    }

    @Test
    void listPeerViewsIncludesLocalUserAndKnownPeers() throws Exception {
        HeartbeatManager heartbeatManager = HeartbeatManager.createForTesting("Ana");
        PresenceService service = new PresenceService(heartbeatManager);
        heartbeatManager.receiveHeartbeat(
                new HeartbeatMessage("Bia", "peer-1"),
                InetAddress.getLoopbackAddress()
        );

        var views = service.listPeerViews();

        assertEquals(3, views.size());
        assertTrue(views.get(1).isSelf());
        assertEquals("Ana", views.get(1).getDisplayName());
        assertEquals(heartbeatManager.getInstanceId(), views.get(1).getInstanceId());
        assertEquals("Bia", views.get(2).getDisplayName());
        assertFalse(views.get(2).isSelf());
    }
}

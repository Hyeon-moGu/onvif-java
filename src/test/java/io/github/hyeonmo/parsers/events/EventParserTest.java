package io.github.hyeonmo.parsers.events;

import io.github.hyeonmo.models.events.EventSubscription;
import io.github.hyeonmo.models.events.EventSubscriptionStatus;
import io.github.hyeonmo.responses.OnvifResponse;
import io.github.hyeonmo.responses.events.PullMessagesResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventParserTest {
    @Test
    public void renewParsesAlternatePrefixesAndTimezoneOffsets() {
        EventSubscriptionStatus status = new EventSubscriptionStatusParser().parse(new OnvifResponse<>(
                "<x:RenewResponse xmlns:x='urn:wsnt'><x:CurrentTime> 2030-01-01T09:00:00+09:00 </x:CurrentTime>"
                        + "<x:TerminationTime>2030-01-01T09:01:00+09:00</x:TerminationTime></x:RenewResponse>"));
        assertEquals("2030-01-01T09:00:00+09:00", status.getCurrentTime());
        assertEquals("2030-01-01T09:01:00+09:00", status.getTerminationTime());
    }

    @Test
    public void missingRenewTimesRemainEmptyForLifecycleFallback() {
        EventSubscriptionStatus status = new EventSubscriptionStatusParser().parse(new OnvifResponse<>("<RenewResponse/>"));
        assertEquals("", status.getCurrentTime());
        assertEquals("", status.getTerminationTime());
    }

    @Test
    public void subscriptionReferencePreservesVendorParametersAndQuery() {
        EventSubscription subscription = new CreatePullPointSubscriptionParser().parse(new OnvifResponse<>(
                "<Response xmlns:a='http://www.w3.org/2005/08/addressing' xmlns:v='urn:vendor'>"
                        + "<SubscriptionReference><a:Address>http://camera.test/sub?id=1&amp;channel=2</a:Address>"
                        + "<a:ReferenceParameters><v:Id>opaque-token</v:Id></a:ReferenceParameters>"
                        + "</SubscriptionReference></Response>"));
        assertEquals("http://camera.test/sub?id=1&channel=2", subscription.getAddress());
        assertTrue(subscription.getReferenceParametersXml().contains("urn:vendor"));
        assertTrue(subscription.getReferenceParametersXml().contains("opaque-token"));
        assertEquals(subscription.getReferenceParametersXml(), subscription.withTimes("now", "later").getReferenceParametersXml());
    }

    @Test
    public void emptyPullResponseHasNoEvents() {
        PullMessagesResponse response = new PullMessagesParser().parse(new OnvifResponse<>("<PullMessagesResponse/>"));
        assertTrue(response.getEvents().isEmpty());
    }

    @Test
    public void malformedXmlAndDoctypeFailAllEventParsers() {
        for (String xml : new String[]{"<broken>", "<!DOCTYPE r [<!ENTITY e 'content'>]><r/>"}) {
            OnvifResponse<?> response = new OnvifResponse<>(xml);
            assertInvalid(() -> new EventSubscriptionStatusParser().parse(response));
            assertInvalid(() -> new CreatePullPointSubscriptionParser().parse(response));
            assertInvalid(() -> new PullMessagesParser().parse(response));
        }
    }

    private static void assertInvalid(Runnable parse) {
        try { parse.run(); fail("Expected parser failure"); }
        catch (IllegalStateException expected) { assertNotNull(expected.getCause()); }
    }
}

package net.jpmchase.smb.ach.component.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.jpmchase.smb.ach.model.ACHPaymentListCancelReversalRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.net.URI;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class ListCancelReversalTest {

    private ACHPaymentListCancelReversalRequest achPaymentListCancelReversalRequest;

    private ResponseEntity<ACHPaymentListCancelReversalRequest> responseObject;

    private URI baseUri;

    private RestTemplate restTemplate;

    private WireMockServer wireMockServer;

    private final int port = TestSocketUtils.findAvailableTcpPort();

    @Before
    public void beforeScenario() {

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
        wireMockServer.start();
        baseUri = UriComponentsBuilder.newInstance().scheme("http").host("localhost")
                .port("8082").path("/ccb/smb/ach/payments/reversal/v1/reversal-requests-details").build().toUri();
    }


    @Given(": A list cancel reversal request is submitted")
    public void aPaymentReversalRequestIsSubmitted() {
        achPaymentListCancelReversalRequest = new ACHPaymentListCancelReversalRequest();
        achPaymentListCancelReversalRequest.setPaymentId(46464L);
    }

    @When(": I call the list cancel reversal API")
    public void iCallTheSubmitReversalAPI() throws IOException {


        String responseBodyString = "{\"payeeAccountNickname\":\"dhruv\",\"accountNumber\":\"464645\",\"paymentId\":66464,\"amount\":4646,\"lastUpdatedDate\":\"2024-07-01 11:10:24\",\"reversalReason\":{\"reversalReasonCode\":50645,\"reversalReasonDesc\":\"Duplicate Payment\"},\"reversalRequestedDate\":\"2024-07-01 11:10:24\",\"issue\":null}";

        configureFor("localhost", port);
        stubFor(post(urlEqualTo("/ccb/payments/payment-instructions/v1/list/cancel/reversal"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withBody(responseBodyString).withHeader("Content-Type", "application/json")));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("online-profile-identifier", "345654");
        httpHeaders.add("segmentId", "CML");

        org.springframework.http.HttpEntity<ACHPaymentListCancelReversalRequest> reqEntity = new HttpEntity<>(achPaymentListCancelReversalRequest, httpHeaders);

        restTemplate = new RestTemplate();
        responseObject = restTemplate.postForEntity(baseUri, reqEntity, ACHPaymentListCancelReversalRequest.class);
    }

    @Then(": I Should receive a success response of {int} for list cancel reversal call")
    public void iShouldReceiveASuccessResponseOf(int statusCode) {
        assertEquals(responseObject.getStatusCode().value(), statusCode);
    }

    @After
    public void wiremockServerIsStopped() {
        wireMockServer.stop();
        System.out.println("Stopped the wire mock server.");
    }
}
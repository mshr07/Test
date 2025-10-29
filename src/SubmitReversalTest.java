package net.jpmchase.smb.ach.component.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.jpmchase.smb.ach.model.ACHPaymentReversalRequest;
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

public class SubmitReversalTest {

    private ACHPaymentReversalRequest achPaymentReversalRequest;

    private ResponseEntity<ACHPaymentReversalRequest> responseObject;

    private URI baseUri;

    private RestTemplate restTemplate;

    private WireMockServer wireMockServer;

    private final int port = TestSocketUtils.findAvailableTcpPort();

    @Before
    public void beforeScenario() {

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
        wireMockServer.start();
        baseUri = UriComponentsBuilder.newInstance().scheme("http").host("localhost")
                .port("8082").path("/ccb/smb/ach/payments/reversals/v1/reversal-requests").build().toUri();
    }


    @Given(": A payment reversal request is submitted")
    public void aPaymentReversalRequestIsSubmitted() {

        achPaymentReversalRequest = new ACHPaymentReversalRequest();
        achPaymentReversalRequest.setPaymentId(65156L);
        achPaymentReversalRequest.setReversalReasonName("Reversal_Duplicate_pymt");
    }

    @When(": I call the submit reversal API")
    public void iCallTheSubmitReversalAPI() throws IOException {


        String responseBodyString = "{\"BusinessException\":\"OK\"}";

        configureFor("localhost", port);
        stubFor(post(urlEqualTo("/ccb/payments/payment-instructions/v1/submit/reversal"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse().withStatus(200).withBody(responseBodyString).withHeader("Content-Type", "application/json")));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("online-profile-identifier", "345654");
        httpHeaders.add("authorization", "authorization");
        httpHeaders.add("trace-id","45469549");
        httpHeaders.add("channel-type","C30");

        org.springframework.http.HttpEntity<ACHPaymentReversalRequest> reqEntity = new HttpEntity<>(achPaymentReversalRequest, httpHeaders);

        restTemplate = new RestTemplate();
        responseObject = restTemplate.postForEntity(baseUri, reqEntity, ACHPaymentReversalRequest.class);
    }

    @Then(": I Should receive a success response of {int} for submit reversal call")
    public void iShouldReceiveASuccessResponseOf(int statusCode) {
        assertEquals(responseObject.getStatusCode().value(), statusCode);
    }

    @After
    public void wiremockServerIsStopped() {
        wireMockServer.stop();
        System.out.println("Stopped the wire mock server.");
    }
}
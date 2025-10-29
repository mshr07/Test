package net.jpmchase.smb.ach.component.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.jpmchase.smb.ach.exception.BusinessException;
import net.jpmchase.smb.ach.model.*;
import net.jpmchase.smb.ach.remote.PaymentProductClient;
import net.jpmchase.smb.ach.remote.model.ACHReversalReason;
import net.jpmchase.smb.ach.service.ACHPaymentCancelReversalSvc;
import net.jpmchase.smb.ach.util.PaymentErrors;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reversal/v1")
@Slf4j
public class ACHPaymentExperienceReversalControllerTest {

    PaymentProductClient paymentProductClient;

    ACHPaymentCancelReversalSvc achPaymentCancelReversalSvc;

    public ACHPaymentExperienceReversalControllerTest(PaymentProductClient paymentProductClient, ACHPaymentCancelReversalSvc achPaymentCancelReversalSvc) {
        this.paymentProductClient = paymentProductClient;
        this.achPaymentCancelReversalSvc = achPaymentCancelReversalSvc;
    }

    /**
     * This method is used to list the reversal transaction details
     *
     * @param achPaymentListReversalRequest
     * @return ACHPaymentListReversalResponse
     * @throws BusinessException
     */
    @Operation(summary = "Retrieve details of a given transaction for reversal")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successful operation", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ACHPaymentListReversalResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Authorization Failure"),
            @ApiResponse(responseCode = "400", description = "Request is missing a required parameter or is malformed"),
            @ApiResponse(responseCode = "404", description = "Requested resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    @PostMapping(value = "/reversal-requests-details", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ACHPaymentListReversalResponse listReversalRequest(@Parameter(required = true) @RequestBody @Valid final ACHPaymentListReversalRequest achPaymentListReversalRequest) throws BusinessException {

        ACHPaymentListReversalResponse achPaymentListReversalResponse = new ACHPaymentListReversalResponse();
        if (achPaymentListReversalRequest != null) {
            try{
                net.jpmchase.smb.ach.remote.model.ACHPaymentListReversalRequest achPaymentProductListReversalRequest = getAchPaymentListReversalRequest(achPaymentListReversalRequest);
                net.jpmchase.smb.ach.remote.model.ACHPaymentListReversalResponse achPaymentProductListReversalResponse;
                achPaymentProductListReversalResponse = paymentProductClient.listReversalRequest(achPaymentProductListReversalRequest).getBody();
                if(achPaymentProductListReversalResponse.getIssue()!=null && !achPaymentProductListReversalResponse.getIssue().getErrorCode().equals(0)){
                    achPaymentListReversalResponse.setErrorCode(achPaymentProductListReversalResponse.getIssue().getErrorCode());


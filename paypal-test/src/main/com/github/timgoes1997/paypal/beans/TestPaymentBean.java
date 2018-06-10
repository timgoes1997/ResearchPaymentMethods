package com.github.timgoes1997.paypal.beans;

import com.paypal.api.payments.Payment;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless
@Path("/payment")
public class TestPaymentBean {

    @Inject
    private PaymentService paymentService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public String getTestMessage(){
        return "Hello";
    }

    @GET
    @Path("/process")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProcessMessage(@QueryParam("paymentId") String paymentId, @QueryParam("token") String token, @QueryParam("PayerID") String payerID){
        return paymentService.processPayment(paymentId, token, payerID).toJSON();
    }

    @GET
    @Path("/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCancelMessage(){
        return "cancel";
    }

    @GET
    @Path("/pay")
    public Response executePayment(){
        return paymentService.executeTestPayment(uriInfo);
    }
}

package com.github.timgoes1997.paypal.beans;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    private static final String clientId = "AakHlLLuWxbCgFFb4NDiiGviXObvpNuHLlet1_A7Z9pcgGa8Wd2wHqtVxMuveu6BgpW4rnr7EELAMJ7r";
    private static final String clientSecret = "EMN6jPjkVrENHh3fd3owpMthlFSVFPJunCG_JVnxC00NuwQz9v05Aiq7Sx_F0MloxiGkseQRYT6YCoFD";

    private APIContext context;

    @PostConstruct
    public void init(){
        context = new APIContext(clientId, clientSecret, "sandbox");
    }

    public Response executeTestPayment(UriInfo uriInfo){
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        //Maken Urls voor redirecten.
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(uriInfo.getBaseUriBuilder()
                .path(TestPaymentBean.class)
                .path(TestPaymentBean.class, "getCancelMessage")
                .build()
                .toString());
        redirectUrls.setReturnUrl(uriInfo.getBaseUriBuilder()
                .path(TestPaymentBean.class)
                .path(TestPaymentBean.class, "getProcessMessage")
                .build()
                .toString());

        //Details van de betaling
        Details details = new Details();
        details.setShipping("1");
        details.setSubtotal("5");
        details.setTax("1");

        //Hoeveel er betaald wordt.
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setTotal("7");
        amount.setDetails(details);

        //Aanmaken transactie
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("Test transactie");

        //Maakt een lijst van transacties
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        //Maakt een betaling aan
        Payment payment = new Payment();
        payment.setIntent("sale"); //Wat je precies met deze betaling wilt doen
        payment.setPayer(payer); //Wie de betaling aanmaakt
        payment.setRedirectUrls(redirectUrls); //Waar naar toe wordt verwezen.
        payment.setTransactions(transactions); //De transacties die bij deze betaling horen.

        try{
            Payment createdPayment = payment.create(context);

            Iterator<Links> links = createdPayment.getLinks().iterator();
            while (links.hasNext()) {
                Links link = links.next();
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    // REDIRECT USER TO link.getHref()
                    URL paypalURL = new URL(link.getHref());
                    return Response.temporaryRedirect(paypalURL.toURI()).build();
                }
            }

        } catch (PayPalRESTException e) {
            LOGGER.severe("Paypal:" + e.getMessage());
        } catch (MalformedURLException e) {
            LOGGER.severe("URL:" + e.getMessage());
        } catch (URISyntaxException e) {
            LOGGER.severe("URI:" + e.getMessage());
        }

        return Response.status(400).build();
    }

    public Payment processPayment(String paymentId, String token, String payerID) {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerID);
        try {
            return payment.execute(context, paymentExecution);
        } catch (PayPalRESTException e) {
            throw new InternalServerErrorException("Paypal threw an exception!");
        }
    }
}

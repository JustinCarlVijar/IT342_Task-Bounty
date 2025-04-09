package edu.cit.taskbounty.controller;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import edu.cit.taskbounty.service.BountyPostService;
import edu.cit.taskbounty.service.ProcessedDonationService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/webhook")
public class StripeWebhookController {
    private final BountyPostService bountyPostService;
    private final ProcessedDonationService processedDonationService;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    public StripeWebhookController(BountyPostService bountyPostService, ProcessedDonationService processedDonationService) {
        this.bountyPostService = bountyPostService;
        this.processedDonationService = processedDonationService;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // Verify the webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    handleSuccessfulDonation(session);
                }
            }

            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook Error: " + e.getMessage());
        }
    }

    private void handleSuccessfulDonation(Session session) throws Exception {
        String sessionId = session.getId();

        if (processedDonationService.isProcessed(sessionId)) {
            return; // Ignore duplicate events
        }

        String bountyPostId = session.getMetadata().get("bountyPostId");
        PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
        BigDecimal donationAmount = BigDecimal.valueOf(paymentIntent.getAmount()).divide(new BigDecimal("100"));

        bountyPostService.addDonation(new ObjectId(bountyPostId), donationAmount);

        // Mark this session as processed
        processedDonationService.markAsProcessed(sessionId);
    }
}

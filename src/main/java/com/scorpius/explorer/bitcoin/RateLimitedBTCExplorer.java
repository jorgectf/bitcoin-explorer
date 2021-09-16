package com.scorpius.explorer.bitcoin;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.scorpius.explorer.RateLimitAvoider;
import com.scorpius.explorer.bitcoin.record.BTCAddress;
import com.scorpius.explorer.bitcoin.record.BTCRecordResponseTransformer;
import com.scorpius.explorer.bitcoin.record.BTCTransaction;
import dev.yasper.rump.config.RequestConfig;
import dev.yasper.rump.response.ResponseTransformer;
import java.time.Duration;

public abstract class RateLimitedBTCExplorer extends MultiRequestBTCExplorer {

    protected final RequestConfig requestConfig;
    protected final RateLimitAvoider rateLimitAvoider;

    public RateLimitedBTCExplorer(Duration durationPerCall) {
        ResponseTransformer responseTransformer = new BTCRecordResponseTransformer(createAddressDeserializer(), createTransactionDeserializer());
        this.requestConfig = new RequestConfig().setResponseTransformer(responseTransformer);
        this.rateLimitAvoider = new RateLimitAvoider(durationPerCall, Duration.ofMillis(200));
    }

    public final RateLimitAvoider getRateLimitAvoider() {
        return rateLimitAvoider;
    }

    protected abstract JsonDeserializer<BTCAddress> createAddressDeserializer();

    protected abstract JsonDeserializer<BTCTransaction> createTransactionDeserializer();
}

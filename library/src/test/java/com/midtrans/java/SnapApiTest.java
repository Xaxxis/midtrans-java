package com.midtrans.java;

import com.midtrans.ConfigBuilder;
import com.midtrans.ConfigFactory;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.java.mockupdata.DataMockup;
import com.midtrans.service.MidtransSnapApi;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.midtrans.java.mockupdata.Constant.*;
import static org.junit.Assert.assertEquals;

public class SnapApiTest {

    private MidtransSnapApi snapApi;
    private DataMockup dataMockup;

    @Before
    public void setUp() {
        dataMockup = new DataMockup();
        ConfigFactory configFactory = new ConfigFactory(new ConfigBuilder()
                .setSERVER_KEY(mainServerKey)
                .setCLIENT_KEY(mainClientKey)
                .setIsProduction(isProduction)
                .build());
        snapApi = configFactory.getSnapApi();
    }

    @Test
    public void createTransactionSimpleParam() {
        JSONObject result = null;
        try {
            result = snapApi.createTransaction(dataMockup.miniDataMockUp());
        } catch (MidtransError midtransError) {
            midtransError.printStackTrace();
        }
        assert result.has("token");
        assert result.has("redirect_url");
    }

    @Test
    public void createTransactionMaxParam() throws IOException, MidtransError {
        JSONObject result = snapApi.createTransaction(dataMockup.maxDataMockUp());

        assert result.has("token");
        assert result.has("redirect_url");
    }

    @Test
    public void createTransactionToken() throws MidtransError {
        String token = snapApi.createTransactionToken(dataMockup.miniDataMockUp());
        assert token.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }

    @Test
    public void createTransactionRedirectUrl() throws MidtransError {
        String redirectURL = snapApi.createTransactionRedirectUrl(dataMockup.miniDataMockUp());
        String expected = "https://app.sandbox.midtrans.com/snap/v2/vtweb/";
        assertEquals(expected, redirectURL.substring(0, 47));
    }

    @Test
    public void badRequestBodyOnSnapTrans() throws MidtransError {
        JSONObject result = snapApi.createTransaction(dataMockup.badDataMockUp());
        assert result.getJSONArray("error_messages").get(0).toString()
                .equals("transaction_details.gross_amount is required");
        assert result.getJSONArray("error_messages").get(1).toString()
                .equals("transaction_details.gross_amount is not a number");
    }

    @Test
    public void errorServerKey() throws MidtransError {
        snapApi.apiConfig().setSERVER_KEY("asdas");
        JSONObject result = snapApi.createTransaction(dataMockup.miniDataMockUp());
        assert result.getJSONArray("error_messages").get(0).toString()
                .equals("Access denied due to unauthorized transaction, please check client or server key");
    }

}
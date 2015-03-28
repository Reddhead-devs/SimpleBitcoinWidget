package com.iisurge.reddcoinwidget;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyStore;

public enum RDDProvider {

    ALLCOIN(R.array.currencies_AllCoin, "acoin") {
        @Override
        public String getValue(String currencyCode) throws Exception {
            JSONObject obj = getJSONObject("https://www.allcoin.com/api2/pair/RDD_BTC");
            return obj.getJSONObject("data").getString("trade_price");
        }
    },
    BITTREX(R.array.currencies_Bittrex, "btrx") {
        @Override
        public String getValue(String currencyCode) throws Exception {
            JSONObject obj = getJSONObject("https://bittrex.com/api/v1.1/public/getticker?market=BTC-RDD");
            return obj.getJSONObject("result").getString("Last");
        }
    },
    BLEUTRADE(R.array.currencies_Bleutrade, "bleu") {
        @Override
        public String getValue(String currencyCode) throws Exception {
            JSONObject obj = getJSONObject(String.format("https://bleutrade.com/api/v2/public/getticker?market=RDD_%s", currencyCode));
            JSONArray jsonMainArr = obj.getJSONArray("result");
            for (int i = 0; i < jsonMainArr.length(); i++) {
                JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
                return childJSONObject.getString("Last");
            }
            return null;
        }
    },
    COMKORT(R.array.currencies_Comkort, "comk") {
        @Override
        public String getValue(String currencyCode) throws Exception {
            JSONObject obj = getJSONObject(String.format("https://api.comkort.com/v1/public/market/summary?market_alias=RDD_%s", currencyCode));
            return obj.getJSONObject("markets").getJSONObject(String.format("RDD/%s", currencyCode)).getString("last_price");
        }
    },
    CRYPTSY(R.array.currencies_Cryptsy, "crpsy") {
        @Override
        public String getValue(String currencyCode) throws Exception {

            if (currencyCode.equals("BTC")) {
                String marketid = "169";
                JSONObject obj = getJSONObject(String.format("http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=%s", marketid));
                return obj.getJSONObject("return").getJSONObject("markets").getJSONObject("RDD").getString("lasttradeprice");
            } else if (currencyCode.equals("USD")) {
                String marketid = "262";
                JSONObject obj = getJSONObject(String.format("http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=%s", marketid));
                return obj.getJSONObject("return").getJSONObject("markets").getJSONObject("RDD").getString("lasttradeprice");
            } else if(currencyCode.equals("XRP")) {
                String marketid = "315";
                JSONObject obj = getJSONObject(String.format("http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=%s", marketid));
                return obj.getJSONObject("return").getJSONObject("markets").getJSONObject("RDD").getString("lasttradeprice");
            }else if (currencyCode.equals("LTC")) {
                String marketid = "212";
                JSONObject obj = getJSONObject(String.format("http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=%s", marketid));
                return obj.getJSONObject("return").getJSONObject("markets").getJSONObject("RDD").getString("lasttradeprice");
            }
            return null;
        }
    },
    POLONIEX(R.array.currencies_Poloniex, "polo") {
        @Override
        public String getValue(String currencyCode) throws Exception {
            JSONObject obj = getJSONObject("https://poloniex.com/public?command=returnTicker");
            return obj.getJSONObject("BTC_RDD").getString("last");
        }
    },
    SHAPESHIFT(R.array.currencies_Shapeshift, "ss") {
        @Override
        public String getValue(String currencyCode) throws Exception {
            JSONObject obj = getJSONObject(String.format("https://shapeshift.io/rate/RDD_%s", currencyCode));
            return obj.getString("rate");
        }

    };




    private final int currencyArrayID;
    private String label;

    RDDProvider(int currencyArrayID, String label) {
        this.currencyArrayID = currencyArrayID;
        this.label = label;
    }

    public abstract String getValue(String currencyCode) throws Exception;

    public int getCurrencies() {
        return currencyArrayID;
    }

    public String getLabel() {
        return label;
    }

    private static String getFromBitcoinCharts(String symbol) throws Exception {
        JSONArray array = getJSONArray("http://api.bitcoincharts.com/v1/markets.json");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if(!symbol.equals(obj.getString("symbol"))) continue;
            return obj.getString("avg");
        }
        return null;
    }

    private static JSONObject getJSONObject(String url) throws Exception {
        return new JSONObject(getString(url));
    }

    private static JSONArray getJSONArray(String url) throws Exception {
        return new JSONArray(getString(url));
    }

    private static String getString(String url) throws Exception {
        HttpGet get = new HttpGet(url);

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setUserAgent(params, "SimpleReddcoinWidget/1.0");

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", sf, 443));

        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient client = new DefaultHttpClient(ccm, params);
        client.setCookieStore(new BasicCookieStore());

        return client.execute(get, new BasicResponseHandler());
    }

}

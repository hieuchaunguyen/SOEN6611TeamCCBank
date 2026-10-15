package cbank;

import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {
    private final Map<String, Double> exchangeRatesToCad = new HashMap<>();

    public CurrencyConverter() {
        exchangeRatesToCad.put("CAD", 1.00);
        exchangeRatesToCad.put("USD", 1.35); // 1 USD = 1.35 CAD
        exchangeRatesToCad.put("EUR", 1.48); // 1 EUR = 1.48 CAD
        exchangeRatesToCad.put("GBP", 1.75); // 1 GBP = 1.75 CAD
    }

    public double convertToCad(double amount, String currencyCode) {
        String upper = currencyCode.toUpperCase();
        if (!exchangeRatesToCad.containsKey(upper)) {
            throw new IllegalArgumentException("Unsupported currency: " + currencyCode);
        }
        return amount * exchangeRatesToCad.get(upper);
    }

    public String[] getSupportedCurrencies() {
        return exchangeRatesToCad.keySet().toArray(new String[0]);
    }
}
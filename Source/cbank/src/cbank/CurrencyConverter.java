package cbank;

import init.CurrencyDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyConverter {
    private final Map<String, Double> exchangeRatesToCad;

    public CurrencyConverter() {
        exchangeRatesToCad = CurrencyDatabase.initializeDatabase();
    }

    public double convertToCad(double amount, String currencyCode) {
        String upper = currencyCode.toUpperCase();
        if (!exchangeRatesToCad.containsKey(upper)) {
            throw new IllegalArgumentException("Unsupported currency: " + currencyCode);
        }
        return amount * exchangeRatesToCad.get(upper);
    }

    public List<String> getSupportedCurrenciesInfo() {
        return exchangeRatesToCad.keySet().stream()
                .filter(k -> k.compareToIgnoreCase("CAD") != 0)
                .map(k -> String.format("%s -> $%2f CAD",k,exchangeRatesToCad.get(k)))
                .toList();
    }

    public String infoToCurrencies(String info){
        return info.substring(0,3);
    }
}
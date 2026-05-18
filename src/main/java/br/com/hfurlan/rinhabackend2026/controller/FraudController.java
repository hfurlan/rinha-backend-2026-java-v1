package br.com.hfurlan.rinhabackend2026.controller;

import br.com.hfurlan.rinhabackend2026.bean.FraudRequest;
import br.com.hfurlan.rinhabackend2026.bean.FraudResponse;
import br.com.hfurlan.rinhabackend2026.enums.LoadStatus;
import br.com.hfurlan.rinhabackend2026.repository.LoadControlRepository;
import br.com.hfurlan.rinhabackend2026.service.FraudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@Slf4j
public class FraudController {

    private final LoadControlRepository loadControlRepository;
    private final FraudService fraudService;

    @PostMapping(value = "/fraud-score", consumes = "application/json", produces = "application/json")
    public FraudResponse postPayments(@RequestBody FraudRequest fraudRequest) {
        return fraudService.processFraud(fraudRequest);
    }

    private static void parseByHand(String body) {
        // Manual parsing using substring/indexOf for each field
        FraudRequest req = new FraudRequest();
        int[] idx = new int[1];
        req.setId(extractString(body, "\"id\"", idx));

        // Transaction
        idx[0] = body.indexOf("\"transaction\"", idx[0]);
        if (idx[0] != -1) {
            FraudRequest.FraudTransaction tx = new FraudRequest.FraudTransaction();
            tx.setAmount(extractFloat(body, "\"amount\"", idx));
            tx.setInstallments(extractInt(body, "\"installments\"", idx));
            tx.setRequestedAt(extractDateTime(body, "\"requested_at\"", idx));
            req.setTransaction(tx);
        }

        // Customer
        idx[0] = body.indexOf("\"customer\"", idx[0]);
        if (idx[0] != -1) {
            FraudRequest.FraudCustomer cust = new FraudRequest.FraudCustomer();
            cust.setAvgAmount(extractFloat(body, "\"avg_amount\"", idx));
            cust.setTxCount24h(extractInt(body, "\"tx_count_24h\"", idx));
            cust.setKnownMerchants(extractStringArray(body, "\"known_merchants\"", idx));
            req.setCustomer(cust);
        }

        // Merchant
        idx[0] = body.indexOf("\"merchant\"", idx[0]);
        if (idx[0] != -1) {
            FraudRequest.FraudMerchant merch = new FraudRequest.FraudMerchant();
            merch.setId(extractString(body, "\"id\"", idx));
            merch.setMcc(extractInt(body, "\"mcc\"", idx));
            merch.setAvgAmount(extractFloat(body, "\"avg_amount\"", idx));
            req.setMerchant(merch);
        }

        // Terminal
        idx[0] = body.indexOf("\"terminal\"", idx[0]);
        if (idx[0] != -1) {
            FraudRequest.FraudTerminal term = new FraudRequest.FraudTerminal();
            term.setOnline(extractBoolean(body, "\"is_online\"", idx));
            term.setCardPresent(extractBoolean(body, "\"card_present\"", idx));
            term.setKmFromHome(extractFloat(body, "\"km_from_home\"", idx));
            req.setTerminal(term);
        }

        // Last Transaction
        idx[0] = body.indexOf("\"last_transaction\"", idx[0]);
        if (idx[0] != -1) {
            FraudRequest.FraudLastTransaction lastTx = new FraudRequest.FraudLastTransaction();
            lastTx.setTimestamp(extractDateTime(body, "\"timestamp\"", idx));
            if (lastTx.getTimestamp() != null) {
                lastTx.setKmFromCurrent(extractFloat(body, "\"km_from_current\"", idx));
                req.setLastTransaction(lastTx);
            }
        }
    }

    // --- Helper methods for manual parsing ---
    private static String extractString(String json, String key, int[] lastIdx) {
        int idx = json.indexOf(key, lastIdx[0]);
        int start = json.indexOf('"', idx + key.length() + 1);
        int end = json.indexOf('"', start + 1);
        lastIdx[0] = end;
        return json.substring(start + 1, end);
    }

    private static Float extractFloat(String json, String key, int[] lastIdx) {
        int idx = json.indexOf(key, lastIdx[0]);
        int colon = json.indexOf(':', idx);
        int comma = json.indexOf(',', colon);
        int end = (comma == -1) ? json.indexOf('}', colon) : comma;
        lastIdx[0] = end;
        String num = json.substring(colon + 1, end).replaceAll("[^0-9.eE-]", "").trim();
        return Float.parseFloat(num);
    }

    private static Integer extractInt(String json, String key, int[] lastIdx) {
        int idx = json.indexOf(key, lastIdx[0]);
        int colon = json.indexOf(':', idx);
        int comma = json.indexOf(',', colon);
        int end = (comma == -1) ? json.indexOf('}', colon) : comma;
        lastIdx[0] = end;
        return Integer.parseInt(json.substring(colon + 1, end).trim());
    }

    private static boolean extractBoolean(String json, String key, int[] lastIdx) {
        int idx = json.indexOf(key, lastIdx[0]);
        int colon = json.indexOf(':', idx);
        int end = json.indexOf(',', colon);
        if (end == -1) end = json.indexOf('}', colon);
        lastIdx[0] = end;
        return json.substring(colon + 1, end).trim().startsWith("true");
    }

    private static String[] extractStringArray(String json, String key, int[] lastIdx) {
        int idx = json.indexOf(key, lastIdx[0]);
        int start = json.indexOf('[', idx);
        int end = json.indexOf(']', start);
        lastIdx[0] = end;
        String arr = json.substring(start + 1, end);
        return arr.replaceAll("\"", "").split(" *, *");
    }

    private static String extractObject(String json, String key, int[] lastIdx) {
        int idx = json.indexOf(key, lastIdx[0]);
        int start = json.indexOf('{', idx);
        int brace = 1, i = start + 1;
        while (brace > 0 && i < json.length()) {
            if (json.charAt(i) == '{') brace++;
            else if (json.charAt(i) == '}') brace--;
            i++;
        }
        return (start != -1 && brace == 0) ? json.substring(start, i) : null;
    }

    private static LocalDateTime extractDateTime(String json, String key, int[] lastIdx) {
        String val = extractString(json, key, lastIdx);
        // Remove Z if present
        if (val.endsWith("Z")) val = val.substring(0, val.length() - 1);
        try {
            return LocalDateTime.parse(val);
        } catch (Exception e) {
            // Try with 'T' replaced by space
            try {
                return java.time.LocalDateTime.parse(val.replace('T', ' '));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @GetMapping("/ready")
    public ResponseEntity<HttpStatus> getReady() {
        LoadStatus loadStatus = loadControlRepository.find();
        if (loadStatus == LoadStatus.COMPLETED) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
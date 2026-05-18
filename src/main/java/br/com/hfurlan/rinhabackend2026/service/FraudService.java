package br.com.hfurlan.rinhabackend2026.service;

import br.com.hfurlan.rinhabackend2026.bean.FraudRequest;
import br.com.hfurlan.rinhabackend2026.bean.FraudResponse;
import br.com.hfurlan.rinhabackend2026.bean.Normalizations;
import br.com.hfurlan.rinhabackend2026.dto.Fraud;
import br.com.hfurlan.rinhabackend2026.property.ApplicationProperties;
import br.com.hfurlan.rinhabackend2026.repository.FraudRepository;
import br.com.hfurlan.rinhabackend2026.repository.LoadControlRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPInputStream;

@AllArgsConstructor
@Service
@Slf4j
public class FraudService {

    private final Map<Integer, Float> mccRisks = new HashMap<>();
    private static final Normalizations normalizations = new Normalizations();
    private final ObjectMapper mapper = new ObjectMapper();

    private final ApplicationProperties applicationProperties;

    private final LoadControlRepository loadControlRepository;

    private final FraudRepository fraudRepository;

    public static final ConcurrentLinkedQueue<Fraud> CONCURRENT_LINKED_QUEUE = new ConcurrentLinkedQueue<>();

    public FraudResponse processFraud(FraudRequest fraudRequest){
        //Transformar o payload em um vetor de 14 dimensões, seguindo as fórmulas de normalização.
        float[] vector = buildVector(fraudRequest);

        //Buscar, no dataset de referência, os 5 vetores mais próximos.
        List<Fraud> fraudNeighbors = fraudRepository.findNeighbors(vector);

        //Calcular fraud_score = número_de_fraudes_entre_os_5 / 5.
        int totalFrauds = 0;
        for (Fraud fraudNeighbor : fraudNeighbors) {
            if (fraudNeighbor.fraud()) {
                totalFrauds++;
            }
        }
        float fraudScore = totalFrauds * 1.0f / 5.0f;

        //Responder com approved = fraud_score < 0.6 e o fraud_score no JSON.
        FraudResponse fraudResponse = new FraudResponse();
        fraudResponse.setApproved(fraudScore < 0.6);
        fraudResponse.setFraudScore(fraudScore);

        // Save the transaction
        if (applicationProperties.isProcessNewFrauds()) {
            Fraud fraud = new Fraud(vector, fraudScore >= 0.6);
            CONCURRENT_LINKED_QUEUE.add(fraud);
        }

        return fraudResponse;
    }

    protected float[] buildVector(FraudRequest fraudRequest) {
        float[] vector = new float[14];
        vector[0] = clamp(fraudRequest.getTransaction().getAmount() / normalizations.getMaxAmount());
        vector[1] = clamp(fraudRequest.getTransaction().getInstallments() * 1.0f / normalizations.getMaxInstallments());
        vector[2] = clamp((fraudRequest.getTransaction().getAmount() / fraudRequest.getCustomer().getAvgAmount()) / normalizations.getAmountVsAvgRatio());
        vector[3] = clamp(fraudRequest.getTransaction().getRequestedAt().getHour() / 23.0f);
        vector[4] = clamp((fraudRequest.getTransaction().getRequestedAt().getDayOfWeek().getValue() - 1) / 6.0f);
        if (fraudRequest.getLastTransaction() == null) {
            vector[5] = -1.0f;
            vector[6] = -1.0f;
        } else {
            vector[5] = clamp(fraudRequest.getLastTransaction().getTimestamp().getMinute() * 1.0f / normalizations.getMaxMinutes());
            vector[6] = clamp(fraudRequest.getLastTransaction().getKmFromCurrent() / normalizations.getMaxKm());
        }
        vector[7] = clamp(fraudRequest.getTerminal().getKmFromHome() / normalizations.getMaxKm());
        vector[8] = clamp(fraudRequest.getCustomer().getTxCount24h() * 1.0f / normalizations.getMaxTxCount24h());
        vector[9] = fraudRequest.getTerminal().isOnline() ? 1.0f : 0.0f;
        vector[10] = fraudRequest.getTerminal().isCardPresent() ? 1.0f : 0.0f;
        vector[11] = isKnownMerchant(fraudRequest.getCustomer().getKnownMerchants(), fraudRequest.getMerchant().getId()) ? 0.0f : 1.0f;

        Float mccRisk = mccRisks.get(fraudRequest.getMerchant().getMcc());
        vector[12] = mccRisk != null ? mccRisk : 0.5f;
        vector[13] = clamp(fraudRequest.getMerchant().getAvgAmount() / normalizations.getMaxMerchantAvgAmount());
        return vector;
    }

    private boolean isKnownMerchant(String[] merchants, String merchantId) {
        for (String merchant : merchants) {
            if (merchantId.equals(merchant)) {
                return true;
            }
        }
        return false;
    }

    private float clamp(float number) {
        if (number < 0) {
            return 0;
        }
        if (number > 1) {
            return 1;
        }
        return number;
    }

    private void loadMccRiskFile() throws IOException {
        JsonNode jsonNode = mapper.readTree(IOUtils.toByteArray(readFile(applicationProperties.getMccRiskFilePath())));
        for (String propertyName : jsonNode.propertyNames()) {
            mccRisks.put(Integer.parseInt(propertyName), jsonNode.get(propertyName).asFloat());
        }
    }

    private void loadNormalizationFile() throws IOException {
        JsonNode jsonNode = mapper.readTree(IOUtils.toByteArray(readFile(applicationProperties.getNormalizationFilePath())));
        if (jsonNode.has("max_amount")) normalizations.setMaxAmount(jsonNode.get("max_amount").asInt());
        if (jsonNode.has("max_installments")) normalizations.setMaxInstallments(jsonNode.get("max_installments").asInt());
        if (jsonNode.has("amount_vs_avg_ratio")) normalizations.setAmountVsAvgRatio(jsonNode.get("amount_vs_avg_ratio").asInt());
        if (jsonNode.has("max_minutes")) normalizations.setMaxMinutes(jsonNode.get("max_minutes").asInt());
        if (jsonNode.has("max_km")) normalizations.setMaxKm(jsonNode.get("max_km").asInt());
        if (jsonNode.has("max_tx_count_24h")) normalizations.setMaxTxCount24h(jsonNode.get("max_tx_count_24h").asInt());
        if (jsonNode.has("max_merchant_avg_amount")) normalizations.setMaxMerchantAvgAmount(jsonNode.get("max_merchant_avg_amount").asInt());
    }

    public void loadFraudReferencesFile() throws IOException {
        //1. Validate if the file is loading or has already being loaded
        int numRows = loadControlRepository.insert();
        if (numRows == 0) {
            return;
        }

        // 2. Read which vector and insert into table frauds
        int countTotal = 0;
        int countVectors = 0;
        List<String> vectors = new ArrayList<>();
        boolean vectorOpen = false;
        StringBuilder vector = new StringBuilder();
        try (InputStream inputStream = readFile(applicationProperties.getFraudReferencesFilePath());
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            int i = bufferedReader.read();
            while (i > -1) {
                char c = (char) i;
                if (c == '{') {
                    vectorOpen = true;
                } else if (c == '}'){
                    countTotal++;
                    vectorOpen = false;
                    vector.append(c);
                    vectors.add(vector.toString());
                    vector.delete(0, vector.length());
                    if (countVectors++ % 10000 == 0) {
                        System.out.println("Vetores inseridos:" + countTotal + " - " + new Date());
                        List<Fraud> frauds = convertToFrauds(vectors);
                        fraudRepository.insert(frauds);
                        vectors.clear();
                    }
                }
                if (vectorOpen) {
                    vector.append(c);
                }
                i = bufferedReader.read();
            }
        }

        if (!vectors.isEmpty()) {
            List<Fraud> frauds = convertToFrauds(vectors);
            fraudRepository.insert(frauds);
        }

        fraudRepository.createIndex();
        loadControlRepository.update();
    }

    // {"vector":[0.01,0.0833,0.05,0.8261,0.1667,-1,-1,0.0432,0.25,0,1,0,0.2,0.0416],"label":"legit"},
    private List<Fraud> convertToFrauds(List<String> vectors) {
        List<Fraud> frauds = new ArrayList<>();
        for (String vector : vectors) {
            JsonNode jsonNode = mapper.readTree(vector);
            ArrayNode arrayNode = jsonNode.get("vector").asArray();
            float[] vectorArray = new float[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                vectorArray[i] =  arrayNode.get(i).asFloat();
            }
            frauds.add(new Fraud(vectorArray, jsonNode.get("label").asString().equals("fraud")));
        }
        return frauds;
    }

    private InputStream readFile(String filePath) throws FileNotFoundException {
        if (filePath.startsWith("classpath:")) {
            return FraudService.class.getResourceAsStream(filePath.substring("classpath:".length()));
        } else {
            return new FileInputStream(filePath);
        }
    }

    @PostConstruct
    public void init() throws IOException {
        loadMccRiskFile();
        loadNormalizationFile();
    }
}
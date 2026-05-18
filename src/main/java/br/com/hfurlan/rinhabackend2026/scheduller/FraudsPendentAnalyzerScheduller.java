package br.com.hfurlan.rinhabackend2026.scheduller;

import br.com.hfurlan.rinhabackend2026.dto.Fraud;
import br.com.hfurlan.rinhabackend2026.repository.FraudRepository;
import br.com.hfurlan.rinhabackend2026.service.FraudService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FraudsPendentAnalyzerScheduller {

    private final FraudRepository fraudRepository;

    @Scheduled(fixedRate = 1000)
    public void process(){
        int qtd = 0;
        List<Fraud> fraudsPendent = new ArrayList<>();
        while(qtd++ < 100 && !FraudService.CONCURRENT_LINKED_QUEUE.isEmpty()) {
            fraudsPendent.add(FraudService.CONCURRENT_LINKED_QUEUE.poll());
        }
        if (!fraudsPendent.isEmpty()){
            System.out.println("process() - qtd: " + fraudsPendent.size());
            fraudRepository.insert(fraudsPendent);
        }
    }
}
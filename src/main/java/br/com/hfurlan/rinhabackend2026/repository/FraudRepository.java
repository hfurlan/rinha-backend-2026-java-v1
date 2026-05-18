package br.com.hfurlan.rinhabackend2026.repository;

import br.com.hfurlan.rinhabackend2026.dto.Fraud;
import com.pgvector.PGvector;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@AllArgsConstructor
public class FraudRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insert(Fraud fraud) {
        jdbcTemplate.update("INSERT INTO frauds (fraud_vector, ic_fraud) VALUES (?, ?)", fraud.vector(), fraud.fraud());
    }

    public void insert(List<Fraud> frauds) {
        jdbcTemplate.batchUpdate("INSERT INTO frauds (fraud_vector, ic_fraud) VALUES (?, ?)",
                frauds,
                100,
                (PreparedStatement ps, Fraud fraud) -> {
                    ps.setObject(1, fraud.vector());
                    ps.setBoolean(2, fraud.fraud());
                });
    }

    public List<Fraud> findNeighbors(float[] fraudVector) {
        List<Fraud> frauds = new ArrayList<>();
        jdbcTemplate.query("SELECT ic_fraud FROM frauds ORDER BY fraud_vector <-> ? LIMIT 5", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                frauds.add(new Fraud(null, rs.getBoolean("ic_fraud")));
            }
        }, new PGvector(fraudVector));
        return frauds;
    }

    public void createIndex() {
        System.out.println("createIndex() - START - " + new Date());
        jdbcTemplate.update("CREATE INDEX ON frauds USING hnsw (fraud_vector vector_l2_ops) WITH (m = 16, ef_construction = 64)");
        System.out.println("createIndex() - END - " + new Date());
    }
}
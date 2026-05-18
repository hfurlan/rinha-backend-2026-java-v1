package br.com.hfurlan.rinhabackend2026.repository;

import br.com.hfurlan.rinhabackend2026.enums.LoadStatus;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@AllArgsConstructor
public class LoadControlRepository {

    private static final int LOAD_CONTROL_ID = 1;

    private final JdbcTemplate jdbcTemplate;

    public int insert() {
        return jdbcTemplate.update("INSERT INTO load_control (id, status) VALUES (?, ?) ON CONFLICT DO NOTHING", LOAD_CONTROL_ID, LoadStatus.INCOMPLETE.name());
    }

    public LoadStatus find() {
        try {
            Map<String, Object> values = jdbcTemplate.queryForMap("SELECT phase, round(100.0 * blocks_done / nullif(blocks_total, 0), 1) AS progress FROM pg_stat_progress_create_index");
            System.out.println("[" + values.get("phase") + ", " + values.get("progress") + "]");
        } catch (EmptyResultDataAccessException e) {
            // Do nothing and just ignore the error
        }
        try {
            return LoadStatus.valueOf(jdbcTemplate.queryForObject("SELECT status FROM load_control WHERE id = ?", String.class, LOAD_CONTROL_ID));
        } catch (EmptyResultDataAccessException e) {
            return LoadStatus.INCOMPLETE;
        }
    }

    public int update() {
        return jdbcTemplate.update("UPDATE load_control SET status = ? WHERE id = ?", LoadStatus.COMPLETED.name(), LOAD_CONTROL_ID);
    }
}
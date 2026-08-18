package com.example.securetenant.payment.infrastructure;

import com.example.securetenant.payment.application.DuePaymentScanner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcDuePaymentScanner implements DuePaymentScanner {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDuePaymentScanner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DuePaymentRef> findDue(Instant now, int maxAttempts) {
        return jdbcTemplate.query(
                """
                        select id, tenant_id
                        from payments
                        where status in ('CREATED', 'AUTHORIZED')
                          and next_retry_at is not null
                          and next_retry_at <= ?
                          and attempt_count < ?
                        """,
                (rs, rowNum) -> new DuePaymentRef(
                        rs.getObject("id", UUID.class),
                        rs.getString("tenant_id")
                ),
                Timestamp.from(now),
                maxAttempts
        );
    }
}

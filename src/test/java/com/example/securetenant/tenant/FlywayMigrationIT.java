package com.example.securetenant.tenant;

import com.example.securetenant.PostgresTestConfiguration;
import com.example.securetenant.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({ PostgresTestConfiguration.class, TestJwtDecoderConfig.class })
class FlywayMigrationIT {

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Test
  void migrationsCreateRequiredTablesAndSeedTenants() {
    Integer tables = jdbcTemplate.queryForObject(
        """
            select count(*) from information_schema.tables
            where table_schema = 'public'
              and table_name in (
                'tenants','users','customers','orders','audit_events',
                'wallets','payments','payment_transactions','ledger_entries',
                'settlements','outbox_events','idempotency_keys'
              )
            """,
        Integer.class);
        assertThat(tables).isEqualTo(12);

    Integer tenants = jdbcTemplate.queryForObject("select count(*) from tenants", Integer.class);
    assertThat(tenants).isGreaterThanOrEqualTo(3);
  }
}

package dev.project.bedtimestory.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniqueValidator implements ConstraintValidator<Unique, String> {
    private final JdbcClient jdbcClient;
    private String tableName;
    private String columnName;

    @Override
    public void initialize(Unique annotation) {
        tableName = annotation.tableName();
        columnName = annotation.columnName();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        log.info("row count: {}", jdbcClient.sql("SELECT COUNT(*) FROM " + tableName).query(Integer.class).single());
        boolean result = jdbcClient.sql("SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?")
                .param(value)
                .query(Integer.class)
                .single() == 0;
        log.info("unique result: {}", result);
        return result;
    }
}

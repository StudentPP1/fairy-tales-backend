package dev.project.bedtimestory.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

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
        return jdbcClient.sql("SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?")
                .param(value)
                .query(Integer.class)
                .single() == 0;
    }
}

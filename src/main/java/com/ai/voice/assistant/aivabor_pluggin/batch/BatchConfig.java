//package com.ai.voice.assistant.aivabor_pluggin.batch;
//
//import com.ai.voice.assistant.aivabor_pluggin.model.DatabaseDialect;
//import com.ai.voice.assistant.aivabor_pluggin.service.DataSourceService;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.database.JdbcCursorItemReader;
//import org.springframework.batch.item.database.JdbcBatchItemWriter;
//import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
//import org.springframework.batch.item.support.ListItemReader;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.ColumnMapRowMapper;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import java.util.Map;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Batch configuration.
// */
//@Configuration
//@EnableBatchProcessing
//public class BatchConfig {
//
//    private final DataSourceService dataService;
//
//    public BatchConfig(DataSourceService dataService) {
//        this.dataService = dataService;
//    }
//
//    @Bean
//    @org.springframework.batch.core.configuration.annotation.StepScope
//    public JdbcCursorItemReader<Map<String, Object>> reader(
//            @Value("#{jobParameters['tableName']}") String tableName,
//            @Value("#{jobParameters['fields']}") String fields) {
//
//        String query = String.format("SELECT %s FROM %s", fields, tableName);
//
//        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
//        reader.setDataSource(dataService.getDataSource());
//        reader.setSql(query);
//        reader.setRowMapper(new ColumnMapRowMapper());
//
//        return reader;
//    }
//
//    @Bean
//    @org.springframework.batch.core.configuration.annotation.StepScope
//    public ItemProcessor<Map<String, Object>, String> processor(
//            @Value("#{jobParameters['fields']}") String fields) {
//
//        String[] fieldArray = fields.split(",");
//
//        return item -> {
//            List<Object> values = new ArrayList<>();
//            for (String field : fieldArray) {
//                Object fieldValue = item.get(field.trim());
//                values.add(fieldValue != null ? fieldValue : "");
//            }
//
//            String formattedValue;
//            DatabaseDialect dialect = dataService.getDialect();
//            switch (dialect) {
//                case MYSQL -> formattedValue = values.toString();
//                case POSTGRESQL -> formattedValue = "(" + values.stream()
//                        .map(Object::toString)
//                        .collect(Collectors.joining(", ")) + ")";
//                case ORACLE, SQLSERVER -> formattedValue = values.stream()
//                        .map(Object::toString)
//                        .collect(Collectors.joining(","));
//                default -> throw new IllegalArgumentException("Unsupported dialect: " + dialect);
//            }
//
//            return formattedValue;
//        };
//    }
//
//    @Bean
//    @org.springframework.batch.core.configuration.annotation.StepScope
//    public JdbcBatchItemWriter<String> writer() {
//        String insertSql;
//        DatabaseDialect dialect = dataService.getDialect();
//
//        switch (dialect) {
//            case MYSQL -> insertSql = "INSERT INTO table_vector (combined_field) VALUES (JSON_ARRAY(:item))";
//            case POSTGRESQL -> insertSql = "INSERT INTO table_vector (combined_field) VALUES (:item::vector)";
//            case ORACLE, SQLSERVER -> insertSql = "INSERT INTO table_vector (combined_field) VALUES (:item)";
//            default -> throw new IllegalArgumentException("Unsupported dialect: " + dialect);
//        }
//
//        return new JdbcBatchItemWriterBuilder<String>()
//                .dataSource(dataService.getDataSource())
//                .sql(insertSql)
//                .itemSqlParameterSourceProvider(item -> new MapSqlParameterSource("item", item))
//                .build();
//    }
//
//    @Bean
//    public Step step(JobRepository jobRepository,
//                     PlatformTransactionManager transactionManager,
//                     JdbcCursorItemReader<Map<String, Object>> reader,
//                     ItemProcessor<Map<String, Object>, String> processor,
//                     JdbcBatchItemWriter<String> writer) {
//        return new StepBuilder("step", jobRepository)
//                .<Map<String, Object>, String>chunk(100, transactionManager)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .build();
//    }
//
//    @Bean
//    public Job job(JobRepository jobRepository, Step step) {
//        return new JobBuilder("job", jobRepository)
//                .start(step)
//                .build();
//    }
//}
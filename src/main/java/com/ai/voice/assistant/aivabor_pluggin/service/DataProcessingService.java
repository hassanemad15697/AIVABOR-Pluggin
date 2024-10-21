package com.ai.voice.assistant.aivabor_pluggin.service;

import com.ai.voice.assistant.aivabor_pluggin.request.ProcessRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Service for data processing and handling embeddings.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataProcessingService {

    private final DataSourceService dataSourceService;

    /**
     * Validates identifiers to prevent SQL injection.
     */
    public void validateIdentifiers(ProcessRequest request) throws Exception {
        validateIdentifier(request.getTableName());
        validateIdentifier(request.getIdName());
        for (String field : request.getFields()) {
            validateIdentifier(field);
        }
    }

    /**
     * Processes data and inserts embeddings into the new table.
     */
    public void processAndInsertData(ProcessRequest request) throws Exception {
        String selectSql = buildSelectSql(request);
        String insertSql = buildInsertSql(request);

        try (Connection conn = dataSourceService.getDataSource().getConnection();
             Statement selectStmt = conn.createStatement();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            ResultSet rs = selectStmt.executeQuery(selectSql);

            while (rs.next()) {
                // Combine the fields into a single text
                String combinedText = request.getFields().stream()
                        .map(field -> {
                            try {
                                return rs.getString(field);
                            } catch (SQLException e) {
                                log.error("Error retrieving field '{}'", field, e);
                                return "";
                            }
                        })
                        .collect(Collectors.joining(" "));

                // Get ID
                int id = rs.getInt(request.getIdName());

                // Get embedding
                float[] embedding = getEmbedding(combinedText);

                // Insert embedding into the new table
                setParameters(insertStmt, id, embedding);
                insertStmt.executeUpdate();
            }
            log.info("Data processing and insertion completed.");
        }
    }

    /**
     * Builds the SELECT SQL query based on the request.
     */
    private String buildSelectSql(ProcessRequest request) {
        String fields = request.getFields().stream()
                .map(this::escapeIdentifier)
                .collect(Collectors.joining(", "));
        String idName = escapeIdentifier(request.getIdName());
        String tableName = escapeIdentifier(request.getTableName());
        return String.format("SELECT %s, %s FROM %s", idName, fields, tableName);
    }

    /**
     * Builds the INSERT SQL query based on the dialect.
     */
    private String buildInsertSql(ProcessRequest request) throws Exception {
        String foreignKeyName = escapeIdentifier("fk_" + request.getTableName() + "_" + request.getIdName());
        String vectorTableName = escapeIdentifier(request.getTableName() + "_vector");
        switch (dataSourceService.getDialect()) {
            case MYSQL:
            case POSTGRESQL:
            case ORACLE:
            case SQLSERVER:
                return "INSERT INTO " + vectorTableName + " (" + foreignKeyName + ", embedding) VALUES (?, ?)";
            default:
                throw new SQLException("Unsupported dialect: " + dataSourceService.getDialect());
        }
    }

    /**
     * Sets the embedding parameter in the PreparedStatement based on the dialect.
     */
    private void setParameters(PreparedStatement stmt, int id, float[] embedding) throws Exception {
        stmt.setInt(1, id);

        switch (dataSourceService.getDialect()) {
            case MYSQL:
                stmt.setString(2, Arrays.toString(embedding));
                break;
            case POSTGRESQL:
                Array array = stmt.getConnection().createArrayOf("float4", toObjectArray(embedding));
                stmt.setArray(2, array);
                break;
            case ORACLE:
                // You might need to handle BLOBs or custom data types
                stmt.setBytes(2, floatArrayToByteArray(embedding));
                break;
            case SQLSERVER:
                stmt.setString(2, Arrays.toString(embedding));
                break;
            default:
                throw new SQLException("Unsupported dialect: " + dataSourceService.getDialect());
        }
    }

    /**
     * Converts a float array to a Float object array (for PostgreSQL array).
     */
    private Float[] toObjectArray(float[] floatArray) {
        Float[] result = new Float[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            result[i] = floatArray[i];
        }
        return result;
    }

    /**
     * Converts a float array to a byte array (for Oracle BLOB).
     */
    private byte[] floatArrayToByteArray(float[] floatArray) throws Exception {
        // Implement conversion logic or use serialization
        throw new UnsupportedOperationException("floatArrayToByteArray not implemented.");
    }

    /**
     * Gets the embedding for the given text using a Java library.
     */
    private float[] getEmbedding(String text) throws Exception {
        // Replace this with actual implementation using a Java library.
        // For demonstration purposes, return a dummy embedding:
        float[] dummyEmbedding = new float[1536];
        Arrays.fill(dummyEmbedding, 0.0f);
        return dummyEmbedding;
    }

    /**
     * Helper method to validate SQL identifiers.
     */
    private void validateIdentifier(String identifier) throws Exception {
        if (identifier == null || !identifier.matches("[A-Za-z0-9_]+")) {
            throw new SQLException("Invalid identifier: " + identifier);
        }
    }

    /**
     * Escapes SQL identifiers to prevent SQL injection.
     */
    private String escapeIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }


    /**
     * Creates a new vector table based on the request.
     */
    public void createVectorTable(ProcessRequest request) throws Exception {
        String createTableSql = getCreateTableSql(request);
        try (Connection conn = dataSourceService.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            log.info("Created table '{}'.", request.getTableName() + "_vector");
        }
    }

    /**
     * Generates the SQL for creating the vector table.
     */
    private String getCreateTableSql(ProcessRequest request) throws Exception {
        String originalTableName = escapeIdentifier(request.getTableName());
        String vectorTableName = escapeIdentifier(request.getTableName() + "_vector");
        String idName = "(" + escapeIdentifier(request.getIdName()) + ")";
        String foreignKeyName = escapeIdentifier("fk_" + request.getTableName() + "_" + request.getIdName());

        switch (dataSourceService.getDialect()) {
            case MYSQL:
                return "CREATE TABLE IF NOT EXISTS " + vectorTableName + " (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL, " +
                        foreignKeyName + " INT NOT NULL, " +
                        "embedding JSON, " +
                        "FOREIGN KEY (" + foreignKeyName + ") REFERENCES " + originalTableName + idName +
                        ")";
            case POSTGRESQL:
                return "CREATE TABLE IF NOT EXISTS " + vectorTableName + " (" +
                        "id SERIAL PRIMARY KEY NOT NULL, " +
                        foreignKeyName + " INT NOT NULL, " +
                        "embedding vector(1536), " +
                        "FOREIGN KEY (" + foreignKeyName + ") REFERENCES " + originalTableName + " " + idName +
                        ")";
            case ORACLE:
                return "CREATE TABLE " + vectorTableName + " (" +
                        "id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL, " +
                        foreignKeyName + " NUMBER NOT NULL, " +
                        "embedding BLOB, " +
                        "FOREIGN KEY (" + foreignKeyName + ") REFERENCES " + originalTableName + idName +
                        ")";
            case SQLSERVER:
                return "CREATE TABLE " + vectorTableName + " (" +
                        "id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, " +
                        foreignKeyName + " INT NOT NULL, " +
                        "embedding NVARCHAR(MAX), " +
                        "FOREIGN KEY (" + foreignKeyName + ") REFERENCES " + originalTableName + idName +
                        ")";
            default:
                throw new SQLException("Unsupported dialect: " + dataSourceService.getDialect());
        }
    }
}

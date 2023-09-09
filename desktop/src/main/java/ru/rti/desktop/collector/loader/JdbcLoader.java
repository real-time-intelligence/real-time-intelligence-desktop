package ru.rti.desktop.collector.loader;

import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public interface JdbcLoader {

    default long getSysdate(String statement, Connection connection, Logger log) {
        long sysdate = 0;

        log.info("Query to get sysdate: " + statement);

        try (PreparedStatement ps = connection.prepareStatement(statement)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = (Timestamp) rs.getObject(1);
                    sysdate = ts.getTime();
                }
            } catch (Exception eRs) {
                log.catching(eRs);
                throw new RuntimeException(eRs);
            }
        } catch (Exception ePs) {
            log.catching(ePs);
            throw new RuntimeException(ePs);
        }

        return sysdate;
    }
}

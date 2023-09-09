package ru.rti.desktop.utility;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.cstype.CSType;
import org.fbase.model.profile.cstype.SType;
import ru.rti.desktop.helper.GsonHelper;
import ru.rti.desktop.model.config.Connection;
import ru.rti.desktop.model.config.Profile;
import ru.rti.desktop.model.config.Query;
import ru.rti.desktop.model.config.Table;
import ru.rti.desktop.model.config.Task;
import ru.rti.desktop.model.sql.GatherDataSql;
import ru.rti.desktop.security.EncryptDecrypt;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
@Log4j2
public class TestData {
    private static boolean testMode = false;

    public static void setTestMode(boolean mode) {
        testMode = mode;
    }

    public void saveConfigToFile(GsonHelper gsonHelper, EncryptDecrypt encryptDecrypt) {
        if (!testMode) {
            return;
        }

        try {
            gsonHelper.createConfigDir(Table.class);
        } catch (IOException e) {
            log.catching(e);
            throw new RuntimeException(e);
        }

        try {
            gsonHelper.createConfigDir(Connection.class);
        } catch (IOException e) {
            log.catching(e);
            throw new RuntimeException(e);
        }

        try {
            gsonHelper.createConfigDir(Query.class);
        } catch (IOException e) {
            log.catching(e);
            throw new RuntimeException(e);
        }

        try {
            gsonHelper.createConfigDir(Task.class);
        } catch (IOException e) {
            log.catching(e);
            throw new RuntimeException(e);
        }

        try {
            gsonHelper.createConfigDir(Profile.class);
        } catch (IOException e) {
            log.catching(e);
            throw new RuntimeException(e);
        }

        getProfileList(encryptDecrypt).forEach(profile -> {
            try {
                gsonHelper.add(profile, Profile.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        getTaskList(encryptDecrypt).forEach(task -> {
            try {
                gsonHelper.add(task, Task.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        getConnectionList(encryptDecrypt).forEach(connection -> {
            try {
                gsonHelper.add(connection, Connection.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        getQueryList().forEach(query -> {
            try {
                gsonHelper.add(query, Query.class);

                Table table = new Table();
                table.setTableName(query.getName());
                gsonHelper.add(table, Table.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static List<Connection> getConnectionList(EncryptDecrypt encryptDecrypt) {
        Connection connection = new Connection();
        connection.setId(1);
        connection.setName("OracleDB");
        connection.setUrl("jdbc:oracle:thin:@localhost:1523:orcl");
        connection.setUserName("system");
        connection.setPassword(encryptDecrypt.encrypt("sys"));
        connection.setDriver("oracle.jdbc.driver.OracleDriver");
        connection.setJar("C:\\Users\\.temp\\ojdbc6.jar");

        Connection connection2 = new Connection();
        connection2.setId(2);
        connection2.setName("OracleDB2");
        connection2.setUrl("jdbc:oracle:thin:@localhost:1523:orcl");
        connection2.setUserName("system");
        connection2.setPassword(encryptDecrypt.encrypt("sys"));
        connection2.setDriver("oracle.jdbc.driver.OracleDriver");
        connection2.setJar("C:\\Users\\.temp\\ojdbc6.jar");

        Connection connection3 = new Connection();
        connection3.setId(3);
        connection3.setName("Postgres");
        connection3.setUrl("jdbc:postgresql://localhost:5432/postgres");
        connection3.setUserName("postgres");
        connection3.setPassword(encryptDecrypt.encrypt("postgres"));
        connection3.setDriver("org.postgresql.Driver");
        connection3.setJar("C:\\Users\\.temp\\postgresql-42.2.20.jar");

        return Stream.of(connection, connection2, connection3).collect(Collectors.toList());
    }

    private static List<Query> getQueryList() {
        Query query = new Query();
        query.setId(1);
        query.setName("Query name 1");
        query.setDescription("Для тестирования СУБД Oracle");
        query.setText("SELECT SYSDATE AS dt, value AS value_histogram, value AS value_enum, value AS value_raw"
                + " FROM (SELECT (MOD(Round(DBMS_RANDOM.Value(1, 99)), 9) + 1) value FROM dual )");
        //query.setCsTypeMap(getCSTypeMap());
        query.setGatherDataSql(GatherDataSql.BY_CLIENT);

        Query query2 = new Query();
        query2.setId(2);
        query2.setName("Query name 2");
        query2.setDescription("Для тестирования СУБД Oracle");
        query2.setText("SELECT SYSDATE AS dt, value AS value_histogram, value AS value_enum, value AS value_raw"
                + " FROM (SELECT (MOD(Round(DBMS_RANDOM.Value(1, 99)), 9) + 1) value FROM dual) ");
        //query2.setCsTypeMap(getCSTypeMap());
        query2.setGatherDataSql(GatherDataSql.BY_CLIENT);

        Query query3 = new Query();
        query3.setId(3);
        query3.setName("Query name 3");
        query3.setDescription("Для тестирования СУБД Postgres");
        query3.setText("SELECT current_timestamp as dt\n"
                + "        ,MIN(CASE WHEN rn = 7 THEN floor(random()*(10-1+1))+1 END) as value_histogram\n"
                + "        ,MIN(CASE WHEN rn = 7 THEN floor(random()*(10-1+1))+1 END) as value_enum\n"
                + "        ,MIN(CASE WHEN rn = 7 THEN floor(random()*(10-1+1))+1 END) as value_raw \n"
                + "  FROM generate_series(1,50) id     -- number of rows\n"
                + "  ,LATERAL( SELECT nr, ROW_NUMBER() OVER (ORDER BY id * random())\n"
                + "             FROM generate_series(1,900) nr\n"
                + "          ) sub(nr, rn)\n"
                + "   GROUP BY id");
        //query3.setCsTypeMap(getCSTypeMap());
        query3.setGatherDataSql(GatherDataSql.BY_CLIENT);

        Query query4 = new Query();
        query4.setId(4);
        query4.setName("Query name 4");
        query4.setDescription("История активных сессий");
        query4.setText("SELECT * FROM v$active_session_history");
        query4.setGatherDataSql(GatherDataSql.BY_SERVER);

        Query query5 = new Query();
        query5.setId(5);
        query5.setName("Query v$transaction");
        query5.setDescription("Транзакции в БД Oracle");
        query5.setText("SELECT sysdate as dt, status, name, start_scn FROM v$transaction");
        //query5.setCsTypeMap(getTransactionCSTypeMap());
        query5.setGatherDataSql(GatherDataSql.BY_CLIENT);

        Query query6 = new Query();
        query6.setId(6);
        query6.setName("Query v$circuit");
        query6.setDescription("Статистика виртуальных соединений в БД Oracle");
        query6.setText("SELECT sysdate as dt, c.* FROM v$circuit c");
        query6.setGatherDataSql(GatherDataSql.BY_CLIENT);

        Query query7 = new Query();
        query7.setId(7);
        query7.setName("select * from v$session;");
        query7.setDescription("Статистика v$session в БД Oracle");
        query7.setText("SELECT sysdate as dt, c.* FROM v$session c");
        query7.setGatherDataSql(GatherDataSql.BY_CLIENT);

        return Stream.of(query, query2, query3, query4, query5, query6, query7).collect(Collectors.toList());
    }

    private static List<Task> getTaskList(EncryptDecrypt encryptDecrypt) {
        List<Connection> connectionList = getConnectionList(encryptDecrypt);
        List<Query> queryList = getQueryList();

        Task task1 = new Task();
        task1.setId(1);
        task1.setName("Task name 1");
        task1.setPullTimeout(1);
        task1.setDescription("Description task1");
        task1.setConnectionId(connectionList.stream().filter(f -> f.getId() == 1).findAny().get().getId());
        task1.setQueryList(queryList.stream()
            .filter(f -> f.getId() == 5 || f.getId() == 6 || f.getId() == 7)
            .map(Query::getId)
            .collect(Collectors.toList()));

        Task task2 = new Task();
        task2.setId(2);
        task2.setName("Task name 2");
        task2.setPullTimeout(1);
        task2.setDescription("Description task2");
        task2.setConnectionId(connectionList.stream().filter(f -> f.getId() == 1).findAny().get().getId());
        task2.setQueryList(Collections.singletonList(queryList.stream().filter(f -> f.getId() == 4).findAny().get().getId()));

        Task task3 = new Task();
        task3.setId(3);
        task3.setName("Task name 3");
        task3.setPullTimeout(1);
        task3.setDescription("Description task3");
        task3.setConnectionId(connectionList.stream().filter(f -> f.getId() == 2).findAny().get().getId());
        task3.setQueryList(queryList.stream()
                .filter(f -> f.getId() == 1 || f.getId() == 2)
                .map(Query::getId)
                .collect(Collectors.toList()));

        Task task4 = new Task();
        task4.setId(4);
        task4.setName("Task name postgres");
        task4.setPullTimeout(3);
        task4.setDescription("Description task4");
        task4.setConnectionId(connectionList.stream().filter(f -> f.getId() == 3).findAny().get().getId());
        task4.setQueryList(queryList.stream()
                .map(Query::getId)
                .filter(id -> id == 3)
                .collect(Collectors.toList()));

        return Stream.of(task1, task2, task3, task4).collect(Collectors.toList());
    }

    private static List<Profile> getProfileList(EncryptDecrypt encryptDecrypt) {
        List<Task> taskList = getTaskList(encryptDecrypt);

        Profile profile = new Profile();
        profile.setId(1);
        profile.setName("Test profile 1");
        profile.setDescription("Description 1");
        profile.setTaskList(taskList.stream()
                .map(Task::getId)
                .filter(id -> id == 1 || id == 3)
                .collect(Collectors.toList()));

        Profile profile2 = new Profile();
        profile2.setId(2);
        profile2.setName("Test profile 2");
        profile2.setDescription("Description 2");
        profile2.setTaskList(taskList.stream()
                .map(Task::getId)
                .filter(id -> id == 2)
                .collect(Collectors.toList()));

        Profile profile3 = new Profile();
        profile3.setId(3);
        profile3.setName("Postgres profile");
        profile3.setDescription("Description 3");
        profile3.setTaskList(taskList.stream()
                .map(Task::getId)
                .filter(id -> id == 4)
                .collect(Collectors.toList()));

        return Arrays.asList(profile, profile2, profile3);
    }

    private Map<String, CSType> getCSTypeMap() {
        Map<String, CSType> csTypeMap = new HashMap<>();

        csTypeMap.put("DT", new CSType().toBuilder().isTimeStamp(true).sType(SType.RAW).build());

        csTypeMap.put("VALUE_HISTOGRAM", new CSType().toBuilder().sType(SType.HISTOGRAM).build());
        csTypeMap.put("VALUE_ENUM", new CSType().toBuilder().sType(SType.HISTOGRAM).build());
        csTypeMap.put("VALUE_RAW", new CSType().toBuilder().sType(SType.HISTOGRAM).build());

        return csTypeMap;
    }

    private Map<String, CSType> getTransactionCSTypeMap() {
        Map<String, CSType> csTypeMap = new HashMap<>();

        csTypeMap.put("DT", new CSType().toBuilder().isTimeStamp(true).sType(SType.RAW).build());

        csTypeMap.put("STATUS", new CSType().toBuilder().sType(SType.RAW).build());
        csTypeMap.put("NAME", new CSType().toBuilder().sType(SType.RAW).build());
        csTypeMap.put("START_SCN", new CSType().toBuilder().sType(SType.RAW).build());

        return csTypeMap;
    }

    /*private Map<String, CSType> getCircuitCSTypeMap() {
        Map<String, CSType> csTypeMap = new HashMap<>();

        *//*csTypeMap.put("DT", new CSType().toBuilder().isTimeStamp(true).sType(SType.RAW).build());

        csTypeMap.put("STATUS", new CSType().toBuilder().sType(SType.RAW).build());
        csTypeMap.put("NAME", new CSType().toBuilder().sType(SType.RAW).build());
        csTypeMap.put("START_SCN", new CSType().toBuilder().sType(SType.RAW).build());*//*

        return csTypeMap;
    }*/
}

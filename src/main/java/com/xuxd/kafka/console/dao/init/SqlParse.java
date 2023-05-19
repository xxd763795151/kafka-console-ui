package com.xuxd.kafka.console.dao.init;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import scala.collection.mutable.StringBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: xuxd
 * @date: 2023/5/17 21:22
 **/
@Slf4j
public class SqlParse {

    private final String FILE = "db/data-h2.sql";

    private final Map<String, List<String>> sqlMap = new HashMap<>();

    public static final String ROLE_TABLE = "t_sys_role";
    public static final String USER_TABLE = "t_sys_user";
    public static final String PERM_TABLE = "t_sys_permission";

    public SqlParse() {
        sqlMap.put(ROLE_TABLE, new ArrayList<>());
        sqlMap.put(USER_TABLE, new ArrayList<>());
        sqlMap.put(PERM_TABLE, new ArrayList<>());

        String table = null;
        try {
            List<String> lines = getSqlLines();
            for (String str : lines) {
                if (StringUtils.isNotEmpty(str)) {
                    if (str.indexOf("start--") > 0) {
                        if (str.indexOf(ROLE_TABLE) > 0) {
                            table = ROLE_TABLE;
                        }
                        if (str.indexOf(USER_TABLE) > 0) {
                            table = USER_TABLE;
                        }
                        if (str.indexOf(PERM_TABLE) > 0) {
                            table = PERM_TABLE;
                        }
                    }
                    if (isSql(str)) {
                        if (table == null) {
                            log.error("Table is null, can not load sql: {}", str);
                            continue;
                        }
                        sqlMap.get(table).add(str);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getSqlList(String table) {
        return sqlMap.get(table);
    }

    public String getMergeSql(String table) {
        List<String> list = getSqlList(table);
        StringBuilder sb = new StringBuilder();
        list.forEach(sql -> sb.append(sql));
        return sb.toString();
    }

    private boolean isSql(String str) {
        return StringUtils.isNotEmpty(str) && str.startsWith("insert");
    }

    private List<String> getSqlLines() throws Exception {
//        File file = ResourceUtils.getFile(FILE);
//        List<String> lines = Files.readLines(file, Charset.forName("UTF-8"));
        List<String> lines = new ArrayList<>();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8")) {
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            }
        }
        return lines;
    }
}

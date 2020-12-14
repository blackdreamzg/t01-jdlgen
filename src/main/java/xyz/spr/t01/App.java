package xyz.spr.t01;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Hello world!
 *
 */
public class App {

    private String url = "jdbc:mysql://mysql.zg.com:30000/information_schema?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false";

    private String defaultSchemaName = "bm03";

    private String username = "root";

    private String password = "pwddevopsBR@2020";

    public static void main(String[] args) {
        App app = new App();
        System.out.println("================开始===================");
        app.executeEntities();
        app.executeRelationship();
        System.out.println("================完毕===================");
    }

    public void executeEntities() {
        StringBuilder sb = new StringBuilder();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            String sql = "select table_name tableName,column_name columnName, data_type dataType,"
                    + " column_comment columnComment, column_key columnKey, extra, "
                    + " is_nullable isNullable, CHARACTER_MAXIMUM_LENGTH maxlength, COLUMN_KEY columnKey"
                    + " from information_schema.columns where table_schema = '" + defaultSchemaName
                    + "' order by table_name,ordinal_position";
            // System.out.println(sql);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            String tableNameFlag = "";
            int i = 0;
            while (rs.next()) {
                String tableName = rs.getString("tableName");
                String columnName = rs.getString("columnName");
                String dataType = rs.getString("dataType");
                // System.out.println(dataType);
                String isNullable = rs.getString("isNullable");
                Long maxlength = rs.getLong("maxlength");
                String columnKey = rs.getString("columnKey");
                if (!"MUL".equals(columnKey)) {
                    if (tableName != null && !tableName.equals(tableNameFlag)) {
                        if (i > 0) {
                            sb.append("}\n");
                        }
                        sb.append("entity " + Util.toUpperFristChar(Util.underlineToHump(tableName)) + " {\n");
                        tableNameFlag = tableName;
                    }
                    sb.append("\t");
                    sb.append(Util.underlineToHump(columnName));
                    if ("bigint".equals(dataType)) {
                        sb.append(" Long");
                    }
                    if ("varchar".equals(dataType) || "char".equals(dataType) || "text".equals(dataType)
                            || "json".equals(dataType)) {
                        sb.append(" String");
                    }
                    if ("tinyint".equals(dataType)) {
                        sb.append(" Integer");
                    }
                    if ("timestamp".equals(dataType) || "date".equals(dataType)) {
                        sb.append(" Instant");
                    }
                    if ("decimal".equals(dataType)) {
                        sb.append(" Double");
                    }
                    if ("int".equals(dataType)) {
                        sb.append(" Integer");
                    }
                    if ("bit".equals(dataType)) {
                        sb.append(" Boolean");
                    }
                    if (maxlength != null && maxlength > 0) {
                        sb.append(" maxlength(" + maxlength + ")");
                    }

                    if ("UNI".equals(columnKey) || "PRI".equals(columnKey)) {
                        sb.append(" unique");
                    }

                    if ("NO".equals(isNullable)) {
                        sb.append(" required");
                    }
                    sb.append("\n");
                }
                // System.out.println(
                // rs.getString("tableName") + " " + rs.getString("columnName") + " " +
                // rs.getString("dataType"));
                i++;
            }
            sb.append("}\n");
            rs.close();
            st.close();
            conn.close();
            System.out.println(sb);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeRelationship() {
        StringBuilder sb = new StringBuilder();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            String sql = "SELECT\r\n" + "	CONSTRAINT_NAME ref_name,\r\n" + "	TABLE_NAME child_table_name,\r\n"
                    + "	COLUMN_NAME child_column_name,\r\n" + "	REFERENCED_TABLE_NAME parent_table_name,\r\n"
                    + "	REFERENCED_COLUMN_NAME parent_column_name \r\n" + "FROM\r\n"
                    + "	information_schema.KEY_COLUMN_USAGE \r\n" + "WHERE\r\n" + "	CONSTRAINT_SCHEMA = '"
                    + this.defaultSchemaName + "' \r\n" + "	AND CONSTRAINT_NAME <> 'PRIMARY'\r\n"
                    + "AND CONSTRAINT_NAME <> 'id' \r\n" + " ORDER BY REFERENCED_TABLE_NAME";
            // System.out.println(sql);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            sb.append("relationship OneToMany {\n");

            while (rs.next()) {
                // String ref_name = rs.getString("ref_name");
                String child_table_name = rs.getString("child_table_name");
                String child_column_name = rs.getString("child_column_name");
                String parent_table_name = rs.getString("parent_table_name");
                // String parent_column_name = rs.getString("parent_column_name");
                sb.append("\t");
                sb.append(Util.toUpperFristChar(Util.underlineToHump(parent_table_name)));
                sb.append("{");
                sb.append(Util.underlineToHump(child_table_name));
                sb.append("} to ");
                sb.append(Util.toUpperFristChar(Util.underlineToHump(child_table_name)));
                sb.append("{");
                sb.append(Util.underlineToHump(parent_table_name));
                sb.append("(");
                sb.append(Util.underlineToHump(child_column_name));
                sb.append(")");
                sb.append("}");
                sb.append("\n");
            }
            rs.close();
            st.close();
            conn.close();
            sb.append("}\n");
            System.out.println(sb);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

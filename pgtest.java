import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.RestoreAction;
import java.sql.*;
import java.util.Random;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;

public class pgtest {
    public static void main(String[] arg) {
        try {
            String url = "jdbc:postgresql://stampy.cs.wisc.edu/cs564instr?sslfactory=org.postgresql.ssl.NonValidatingFactory&ssl";
            Connection conn = DriverManager.getConnection(url);
            Statement st = conn.createStatement();
            ArrayList<String> srcTableList = new ArrayList<String>();
            long seed = 45;
            Random rand = new Random(seed);
            chatbot(st, srcTableList, seed, rand);
            drop_all_table(st, srcTableList);
            st.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * main prompt list
     */
    public static void chatbot(Statement st, ArrayList srcTableList, long seed, Random rand) throws SQLException, Exception {
        char quit = 'y';
        Scanner sc = new Scanner(System.in);

        while (quit != 'q') {
            // table name or query
            System.out.println("Please type in a table name or query: ");
            String source = "";
            String input = "";
            input = sc.nextLine();
            source = source + input;
            while (sc.hasNextLine()) {
                input = sc.nextLine();
                if (input.length() == 0) {
                    break;
                }
                source = source + " " + input;
            }
            
            source = source.replace(';', ' ');
            
            // true table name; false query;
            boolean isTable = true;
            int length = source.length();
        
            // check if it is a query
            int i = 0;
            char c = source.charAt(i);
            while (i < length - 1){
                if (c == ' ') {
                    isTable = false;
                    st.executeUpdate("drop table if exists sample");
                    break;
                }
                i++;
                c = source.charAt(i);
            }

            int numOfRows = get_total_row_number(st, source, isTable);

            // get number of rows
            System.out.println("How many sample rows you desire: ");
            int numberRow = sc.nextInt();
            sc.nextLine();
            
            // reset?
            System.out.println("Do you want to reset the seed? Type Y / N : ");
            String reset = sc.next();
            sc.nextLine();
            if (reset.toLowerCase().charAt(0) == 'y') {
                System.out.println("type in a long number: ");
                seed = sc.nextLong();
                sc.nextLine();
            }
            
            // create a table or not
            System.out.println("Do you want to create a table for the sampled rows, type Y for yes, N for no: ");
            String tableFlag = sc.next();
            sc.nextLine();

            // execute insert/fetch functionality
            if (tableFlag.toLowerCase().charAt(0) == 'n') {
                fetch_output(numberRow, st, source, isTable, numOfRows, seed, rand);
            } else {
                insert_to_table(numberRow, st, source, isTable, srcTableList, numOfRows, seed, rand);
            }

            // more sample or not
            System.out.println("Do you want more samples, type Y for yes, Q for quit: ");
            String quitFlag = sc.next();
            sc.nextLine();
            quit = quitFlag.toLowerCase().charAt(0);
        }

        sc.close();
    }

    /**
     * insert into new / exist table
     * from source (table / query)
     */
    public static void insert_to_table(int numberRow, Statement st, String source, boolean isTable, ArrayList srcTableList, int numOfRows, long seed, Random rand) throws SQLException, Exception{
        boolean isNewTable = true;
        String insertTable = "sample";
        if(isTable){
            if(srcTableList.indexOf(source) != -1){
                isNewTable = false;
            }
            insertTable = update_list(source, srcTableList);
        }
        String schema = ""; 
        if(isNewTable){
            if(isTable){
                schema = getTableSchema(insertTable, st.executeQuery("select * from " + source));
            }else{
                schema = getTableSchema(insertTable, st.executeQuery("select * from (" + source + ") as q"));
            }
            st.executeUpdate(schema);
        }
        String columnName = "";
        if(isTable){
            columnName = getColumnName(st.executeQuery("select * from " + source));
        }
        else{
            columnName = getColumnName(st.executeQuery("select * from (" + source + ") AS tmp"));
        }
        ResultSet rs = get_source_data(numberRow, numOfRows, source, st, isTable, seed, rand);
        put_in_table(rs, st, insertTable, isNewTable, schema, columnName);
    }

    /**
     * fetch from source (table / query) 
     * output on console
     */
    public static void fetch_output(int numberRow, Statement st, String source, boolean isTable, int numOfRows, long seed, Random rand) throws SQLException, Exception{
        ResultSet rs = get_source_data(numberRow, numOfRows, source, st, isTable, seed, rand);
        print_data(rs);
    }

    public static String update_list(String source, ArrayList srcTableList) throws SQLException, Exception{
        String insertTable = "";
        // src table list is empty
        if(srcTableList.size() == 0){
            srcTableList.add(source);
            insertTable = "sample0";
            return insertTable;
        }
        int index = srcTableList.indexOf(source);
        // src table not in the database
        if(index == -1){
            srcTableList.add(source);
            insertTable = "sample" + srcTableList.indexOf(source);
            return insertTable;
        }
        // src table has corresponding sink table
        else{
            insertTable = "sample" + srcTableList.indexOf(source);
            return insertTable;
        }
    }

    /**
     * drop all the created tables
     */
    public static void drop_all_table(Statement st, ArrayList srcTableList) throws SQLException, Exception{
        st.executeQuery("DROP TABLE if EXISTS sample");
        for (int i = 0; i < srcTableList.size(); i++) {
            st.executeQuery("DROP TABLE if EXISTS sample" + i);
        }
    }

    /**
     * get the total number of rows
     */
    public static int get_total_row_number(Statement st, String source, boolean isTable) throws SQLException, Exception{
        ResultSet rs = null;
        if(isTable){
            rs = st.executeQuery("select count(*) from "+ source);
        } else{
            rs = st.executeQuery("select count(*) from ("+ source + ") AS q");
        }
        int N  =0;
        while (rs.next()) {
            N = rs.getInt(1);
        }
        return N;
    }

    /**
     * get the data of source
     */
    public static ResultSet get_source_data(int n, int N, String source, Statement st, boolean isTable, long seed, Random rand) throws SQLException, Exception{
        if(n > N){
            n = N;
        }
        int k = 0;
        ResultSet rs = null;
        if (isTable){
            rs = st.executeQuery("select * from "+ source);
        } else{ 
            rs = st.executeQuery(source);
        }
        ResultSetMetaData rsmd = rs.getMetaData();
        // print col name
        for(int i = 1;i < rsmd.getColumnCount();i++){
            System.out.print(rsmd.getColumnName(i)+"\t");
        }
        System.out.println();
        // get random row number
        int t =0;
        int m = 0;
        int row_number = 1;
        int[] rowArray = new int[n];
        int iterator = 0;
        while(true){
            double U = rand.nextDouble();
            if((N-t)*U < (n-m)){
                m++;
                t++;
                rowArray[iterator] = row_number;
                iterator++;
                row_number++;
                if(m < n) continue;
                else break;
            }
            t++;
            row_number++;
        }
        String rn = "";
        if(isTable){
            rn = "select * from (select row_number() over () AS rn, * from " + source + ") AS tmp where ";
        } else{
            rn = "select * from (select row_number() over () AS rn, * from (" + source + ") AS q) AS tmp where ";
        }
            
        for(int i = 0; i < n; i++){
            if (i == n - 1) {
                rn = rn + "rn = " + rowArray[i];
                break;
            }
            rn = rn + "rn = " + rowArray[i] + " OR ";
        }
        
        rs = st.executeQuery(rn);
        return rs;
    }

    /**
     * print all needed data
     */
    public static void print_data(ResultSet rs) throws SQLException, Exception{
        ResultSetMetaData rsmd = rs.getMetaData();
        for(int i = 1;i <= rsmd.getColumnCount();i++){
            System.out.print(rsmd.getColumnName(i)+"\t");
        }
        System.out.println();
        while(rs.next()){
            for(int i = 1;i <= rsmd.getColumnCount();i++){
               System.out.print(rs.getString(i)+"\t");
            }
            System.out.println();
        }
        return ;
    }

    public static void put_in_table(ResultSet rs, Statement st, String insertTable, boolean isNewTable, String schema, String columnName) throws SQLException, Exception{
        ResultSetMetaData rsmd = rs.getMetaData();
        String sql = "";
        while(rs.next()){
            sql = sql + "INSERT INTO "+insertTable +"("+columnName+") VALUES (";
            int i = 2;
            for(i = 2;i <= rsmd.getColumnCount()-1;i++){
                sql = sql + "'" + rs.getString(i)+"',";
            }
            sql = sql + "'" + rs.getString(i)+"');";
        }
        st.executeUpdate(sql);
        print_data(st.executeQuery("select * from " + insertTable));
    }

    public static String getColumnName(ResultSet rs)throws SQLException, Exception{
        ResultSetMetaData rsmd = rs.getMetaData();
        String k = "";
        for(int i = 1;i <= rsmd.getColumnCount();i++){
            //System.out.print(rsmd.getColumnName(i)+"\t");
            if( i == rsmd.getColumnCount()){
                k = k + " \"" + rsmd.getColumnName(i) + "\" ";
                break;
            }
            k = k + " \"" + rsmd.getColumnName(i) +"\", ";
        }
        return k;
    }

    public static String getTableSchema(String insertTable, ResultSet getTableStream) throws SQLException, Exception{
        String sql = "drop table if exists " + insertTable + "; create table " + insertTable + " (";
        ResultSetMetaData rsmd = getTableStream.getMetaData();
        for(int i = 1; i <= rsmd.getColumnCount(); i++){
            if (i == rsmd.getColumnCount()){
                sql = sql + " \"" + rsmd.getColumnName(i) + "\" " + rsmd.getColumnTypeName(i) + ")";
                break;
            }
            sql = sql + " \"" + rsmd.getColumnName(i) + "\" " + rsmd.getColumnTypeName(i) + ", ";
        }
        System.out.println(sql);
        return sql;
    }
}

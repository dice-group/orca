package org.dice_research.ldcbench.nodes.ckan.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.dice_research.ldcbench.nodes.ckan.PostgresqlInsertQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * PostgresDAO to insert data
 *
 * @author Geraldo de Souza Junior
 *
 */

public class PostgresCkanDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresCkanDAO.class);


    private Connection connection;
    private final String USER = "ckan";
    private final String PASSWORD = "ckan";

    public PostgresCkanDAO(String containerName) {

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://"+containerName+":5432/ckan",
                    USER, PASSWORD);

            LOGGER.debug("CONNECTION CREATED");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Could not connect to CKAN Database",e);
            }

    }


    public void insertData() {
        try {


            Statement stmt = connection.createStatement();

            stmt.executeUpdate(PostgresqlInsertQueries.USER);
            stmt.executeUpdate(PostgresqlInsertQueries.ACTIVITY);
            stmt.executeUpdate(PostgresqlInsertQueries.DASHBOARD);
            stmt.executeUpdate(PostgresqlInsertQueries.REVISION);


        } catch (SQLException e) {
            LOGGER.error("Could not INSERT DATA to CKAN Database",e);
        }
    }

}

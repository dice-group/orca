package org.dice_research.ldcbench.nodes.ckan;

public class PostgresqlInsertQueries {
    
    
    
    
    public static final String ACTIVITY =
            "INSERT INTO ckan.public.activity(id,\"timestamp\",user_id,object_id,activity_type)"
            + "values('bfcd9343-0fe6-4cbf-bae8-8c8006eebb53'"
            + ",'2019-04-04 12:26:55.300380',"
            + "'28857799-0435-4902-9c22-08160260f114',"
            + "'28857799-0435-4902-9c22-08160260f114',"
            + "'new user');";
    
    
    
    
    public static final String DASHBOARD = "INSERT INTO ckan.public.dashboard (\n" + 
            "      user_id\n" + 
            "    , activity_stream_last_viewed\n" + 
            "    , email_last_sent\n" + 
            ") VALUES (\n" + 
            "      '28857799-0435-4902-9c22-08160260f114'" + 
            "    , '2019-04-04 12:26:55.312915'" + 
            "    , '2019-04-04 12:26:55.312925'" + 
            ")";
    
    
    public static final String MIGRATE_VERSION = "INSERT INTO ckan.public.migrate_version (\n" + 
            "      repository_id\n" + 
            "    , repository_path\n" + 
            "    , version\n" + 
            ") VALUES (\n" + 
            "      'Ckan' " + 
            "    , '/usr/lib/ckan/venv/src/ckan/ckan/migration' " + 
            "    , 86 " + 
            ")";
    
    public static final String REVISION = "INSERT INTO ckan.public.revision (\n" + 
            "      id\n" + 
            "    , \"timestamp\"\n" + 
            "    , author\n" + 
            "    , message\n" + 
            "    , state\n" + 
            "    , approved_timestamp\n" + 
            ") VALUES (\n" + 
            "      '1d6a7ad3-53f3-4517-8f15-a035f82adc0b' " + 
            "    , '2019-04-04 12:25:14.065399' " + 
            "    , 'admin' " + 
            "    , 'Admin: make sure every object has a row in a revision table'" + 
            "    , 'active'" + 
            "    , '2019-04-04 12:25:14.065399'" + 
            ");"
            + 
            "INSERT INTO ckan.public.revision (\n" + 
            "      id\n" + 
            "    , \"timestamp\"\n" + 
            "    , author\n" + 
            "    , message\n" + 
            "    , state\n" + 
            "    , approved_timestamp\n" + 
            ") VALUES (\n" + 
            "      '7db20abd-ad1d-4849-9c1c-10dbd57f3e17' " + 
            "    , '2019-04-04 12:25:14.712097' " + 
            "    , 'system' " + 
            "    , 'Add versioning to groups, group_extras and package_groups'" + 
            "    , 'active'" + 
            "    , '2019-04-04 12:25:14.712097'" + 
            ");";
    
    public static final String USER = "INSERT INTO ckan.public.\"user\" (\n" + 
            "      id\n" + 
            "    , name\n" + 
            "    , apikey\n" + 
            "    , created\n" + 
            "    , about\n" + 
            "    , password\n" + 
            "    , fullname\n" + 
            "    , email\n" + 
            "    , reset_key\n" + 
            "    , sysadmin\n" + 
            "    , activity_streams_email_notifications\n" + 
            "    , state\n" + 
            ") VALUES (\n" + 
            "      '28857799-0435-4902-9c22-08160260f114'" + 
            "    , 'admin'" + 
            "    , '77564c39-4f3f-443b-96b1-29baabb93769'" + 
            "    , '2019-04-04 12:26:55.292556'" + 
            "    , ''" + 
            "    , '$pbkdf2-sha512$25000$rHUOoTSG0JozhpCS8n6v1Q$k1jctz.COobw103RqDIjhBzpnUugv7LcOraqBHFb1GPxpJGh0yvSofcNVdkRponGBMECIsvUXO/ye63jiVS2/w'" + 
            "    , ''" + 
            "    , 'admin@example.com'" + 
            "    , ''" + 
            "    , TRUE" + 
            "    , FALSE" + 
            "    , 'active'" + 
            ");";

}

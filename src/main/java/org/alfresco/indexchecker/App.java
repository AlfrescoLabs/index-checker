package org.alfresco.indexchecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

/**
 * Command line program that gets indexed documents from SOLR 
 * and existing nodes in Alfresco DB to apply some validations
 * on the number of existing elements in both systems.
 * 
 * Command line parameters can be used to override application.properties values:
 * 
 * $ java -jar target/index-checker-0.0.1-SNAPSHOT.jar --validation.nodes=false
 * 
 */
@ComponentScan
@SpringBootApplication
public class App implements CommandLineRunner
{

    static final Logger LOG = LoggerFactory.getLogger(App.class);

    @Autowired 
    Environment env;
    
    @Autowired
    NodesValidator nodesValidator;
    
    @Autowired
    PermissionsValidator permissionsValidator;
    
    @Override
    public void run(String... args) throws Exception
    {
        
        if (env.getProperty("validation.nodes").equals("true"))
        {
            nodesValidator.validate(env.getProperty("report.detailed").equals("true"));
        }
        
        if (env.getProperty("validation.permissions").equals("true"))
        {
            permissionsValidator.validate(env.getProperty("report.detailed").equals("true"));
        }

    }
    
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }

}

package com.example;

import kalix.springsdk.annotations.Acl;
import kalix.springsdk.KalixConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

// tag::main[]
@SpringBootApplication // <1>
@Import(KalixConfiguration.class) // <2>
// end::main[]
// Allow all other Kalix services deployed in the same project to access the components of this
// Kalix service, but disallow access from the internet. This can be overridden explicitly
// per component or method using annotations.
// Documentation at https://docs.kalix.io/services/using-acls.html
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
// tag::main[]
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Starting Kalix - Spring SDK");
    SpringApplication.run(Main.class, args); // <3>
  }
}
// end::main[]
package com.example;

import kalix.springsdk.KalixConfiguration;
import kalix.springsdk.annotations.Acl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(KalixConfiguration.class)
// NOTE: This default ACL settings is very permissive as it allows any traffic from the internet.
// Our samples default to this permissive configuration to allow users to easily try it out.
// However, this configuration is not intended to be reproduced in production environments.
// Documentation at https://docs.kalix.io/services/using-acls.html
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Starting Kalix - Spring SDK");
    SpringApplication.run(Main.class, args);
  }
}

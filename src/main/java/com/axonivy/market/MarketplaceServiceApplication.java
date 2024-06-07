package com.axonivy.market;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.axonivy.market.github.GitHubProvider;

import jakarta.validation.ReportAsSingleViolation;
import lombok.extern.log4j.Log4j2;


@SpringBootApplication
@Log4j2
public class MarketplaceServiceApplication {

  public static void main(String[] args) throws IOException {
    SpringApplication.run(MarketplaceServiceApplication.class, args);
//	 var org = GitHubProvider.get().getOrganization("axonivy-market");
//
//	 var repo = org.getRepository("adobe-acrobat-sign-connector");
//
//	 var content = repo.getFileContent("adobe-esign-connector-product/README.md");
//
//	 log.error(content);
	 
  }

}

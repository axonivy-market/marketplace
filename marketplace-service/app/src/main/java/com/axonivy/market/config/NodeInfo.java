package com.axonivy.market.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Component
public class NodeInfo {

  private final String hostName;

  public NodeInfo() throws UnknownHostException {
    this.hostName = InetAddress.getLocalHost().getHostName();
  }

  public int getNodeOffset() {
    return isPrimaryNode() ? 0 : 5;
  }

  private boolean isPrimaryNode() {
    return this.hostName.hashCode() % 2 == 0;
  }

}

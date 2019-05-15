/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.net;

import static org.apache.logging.log4j.LogManager.getLogger;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.CustomNetJsonRpcRequestFactory;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.JsonRequestFactories;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.web3j.protocol.core.Request;

public class NetServicesTransaction implements Transaction<Map<String, Map<String, String>>> {
  private static final Logger LOG = getLogger();

  NetServicesTransaction() {}

  @Override
  public Map<String, Map<String, String>> execute(final JsonRequestFactories requestFactories) {
    Map<String, Map<String, String>> netServicesActive = null;
    try {
      CustomNetJsonRpcRequestFactory netServicesJsonRpcRequestFactory =
          requestFactories.netServices();
      Request<?, CustomNetJsonRpcRequestFactory.NetServicesResponse> request =
          netServicesJsonRpcRequestFactory.customNet();

      System.out.println();

      CustomNetJsonRpcRequestFactory.NetServicesResponse netServicesResponse = request.send();
      netServicesActive = netServicesResponse.getResult();
    } catch (final Exception e) {
      LOG.error("Error parsing response to 'net_services' json-rpc request.", e);
    }
    return netServicesActive;
  }
}
/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.tests.acceptance.dsl.jsonrpc;

import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.AwaitNetPeerCount;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.AwaitNetPeerCountException;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.ExpectNetServicesReturnsAllServicesAsActive;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.ExpectNetServicesReturnsOnlyJsonRpcActive;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.ExpectNetVersionConnectionException;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.ExpectNetVersionConnectionExceptionWithCause;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.ExpectNetVersionIsNotBlank;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.ExpectNetVersionPermissionException;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.net.ExpectNetVersionPermissionJsonRpcUnauthorizedResponse;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.net.NetTransactions;

import java.math.BigInteger;

public class Net {

  private final NetTransactions transactions;

  public Net(final NetTransactions transactions) {
    this.transactions = transactions;
  }

  public Condition netServicesAllActive() {
    return new ExpectNetServicesReturnsAllServicesAsActive(transactions.netServices());
  }

  public Condition netServicesOnlyJsonRpcEnabled() {
    return new ExpectNetServicesReturnsOnlyJsonRpcActive(transactions.netServices());
  }

  public Condition netVersion() {
    return new ExpectNetVersionIsNotBlank(transactions.netVersion());
  }

  public Condition awaitPeerCount(final int awaitPeerCount) {
    return new AwaitNetPeerCount(transactions.peerCount(), BigInteger.valueOf(awaitPeerCount));
  }

  public Condition netVersionExceptional(final String expectedMessage) {
    return new ExpectNetVersionConnectionException(transactions.netVersion(), expectedMessage);
  }

  public Condition netVersionExceptional(final Class<? extends Throwable> cause) {
    return new ExpectNetVersionConnectionExceptionWithCause(transactions.netVersion(), cause);
  }

  public Condition netVersionUnauthorizedExceptional(final String expectedMessage) {
    return new ExpectNetVersionPermissionException(transactions.netVersion(), expectedMessage);
  }

  public Condition netVersionUnauthorizedResponse() {
    return new ExpectNetVersionPermissionJsonRpcUnauthorizedResponse(transactions.netVersion());
  }

  public Condition awaitPeerCountExceptional() {
    return new AwaitNetPeerCountException(transactions.peerCount());
  }
}

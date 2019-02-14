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
package tech.pegasys.pantheon.ethereum.eth.sync.tasks;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.eth.manager.AbstractPeerTask.PeerTaskResult;
import tech.pegasys.pantheon.ethereum.eth.manager.AbstractRetryingPeerTask;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.metrics.LabelledMetric;
import tech.pegasys.pantheon.metrics.OperationTimer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Given a set of headers, repeatedly requests the receipts for those blocks. */
public class GetReceiptsForHeadersTask
    extends AbstractRetryingPeerTask<Map<BlockHeader, List<TransactionReceipt>>> {
  private static final Logger LOG = LogManager.getLogger();
  private static final int DEFAULT_RETRIES = 3;

  private final EthContext ethContext;

  private final List<BlockHeader> headers;
  private final Map<BlockHeader, List<TransactionReceipt>> receipts;

  private GetReceiptsForHeadersTask(
      final EthContext ethContext,
      final List<BlockHeader> headers,
      final int maxRetries,
      final LabelledMetric<OperationTimer> ethTasksTimer) {
    super(ethContext, maxRetries, ethTasksTimer, Map::isEmpty);
    checkArgument(headers.size() > 0, "Must supply a non-empty headers list");
    this.ethContext = ethContext;

    this.headers = headers;
    this.receipts = new HashMap<>();
    completeEmptyReceipts(headers);
  }

  public static GetReceiptsForHeadersTask forHeaders(
      final EthContext ethContext,
      final List<BlockHeader> headers,
      final int maxRetries,
      final LabelledMetric<OperationTimer> ethTasksTimer) {
    return new GetReceiptsForHeadersTask(ethContext, headers, maxRetries, ethTasksTimer);
  }

  public static GetReceiptsForHeadersTask forHeaders(
      final EthContext ethContext,
      final List<BlockHeader> headers,
      final LabelledMetric<OperationTimer> ethTasksTimer) {
    return new GetReceiptsForHeadersTask(ethContext, headers, DEFAULT_RETRIES, ethTasksTimer);
  }

  private void completeEmptyReceipts(final List<BlockHeader> headers) {
    headers.stream()
        .filter(header -> header.getReceiptsRoot().equals(Hash.EMPTY_TRIE_HASH))
        .forEach(header -> receipts.put(header, emptyList()));
  }

  @Override
  protected CompletableFuture<Map<BlockHeader, List<TransactionReceipt>>> executePeerTask(
      final Optional<EthPeer> assignedPeer) {
    return requestReceipts(assignedPeer).thenCompose(this::processResponse);
  }

  private CompletableFuture<Map<BlockHeader, List<TransactionReceipt>>> requestReceipts(
      final Optional<EthPeer> assignedPeer) {
    final List<BlockHeader> incompleteHeaders = incompleteHeaders();
    if (incompleteHeaders.isEmpty()) {
      return CompletableFuture.completedFuture(emptyMap());
    }
    LOG.debug(
        "Requesting bodies to complete {} blocks, starting with {}.",
        incompleteHeaders.size(),
        incompleteHeaders.get(0).getNumber());
    return executeSubTask(
        () -> {
          final GetReceiptsFromPeerTask task =
              GetReceiptsFromPeerTask.forHeaders(ethContext, incompleteHeaders, ethTasksTimer);
          assignedPeer.ifPresent(task::assignPeer);
          return task.run().thenApply(PeerTaskResult::getResult);
        });
  }

  private CompletableFuture<Map<BlockHeader, List<TransactionReceipt>>> processResponse(
      final Map<BlockHeader, List<TransactionReceipt>> responseData) {
    receipts.putAll(responseData);

    if (isComplete()) {
      result.get().complete(receipts);
    }

    return CompletableFuture.completedFuture(responseData);
  }

  private List<BlockHeader> incompleteHeaders() {
    return headers.stream().filter(h -> receipts.get(h) == null).collect(Collectors.toList());
  }

  private boolean isComplete() {
    return headers.stream().allMatch(header -> receipts.get(header) != null);
  }
}

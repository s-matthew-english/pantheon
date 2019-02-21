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
package tech.pegasys.pantheon.ethereum.eth.manager.task;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.ethtaskutils.PeerMessageTaskTest;
import tech.pegasys.pantheon.ethereum.eth.manager.task.AbstractPeerTask.PeerTaskResult;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetNodeDataFromPeerTaskTest extends PeerMessageTaskTest<Map<Hash, BytesValue>> {

  @Override
  protected Map<Hash, BytesValue> generateDataToBeRequested() {
    final Map<Hash, BytesValue> requestedData = new HashMap<>();
    for (int i = 0; i < 3; i++) {
      final BlockHeader blockHeader = blockchain.getBlockHeader(10 + i).get();
      requestedData.put(
          Hash.fromHexStringLenient(Integer.toHexString(i)),
          protocolContext.getWorldStateArchive().getNodeData(blockHeader.getStateRoot()).get());
    }
    return requestedData;
  }

  @Override
  protected EthTask<PeerTaskResult<Map<Hash, BytesValue>>> createTask(
      final Map<Hash, BytesValue> requestedData) {
    final List<Hash> hashes = requestedData.values().stream().map(Hash::hash).collect(toList());
    return GetNodeDataFromPeerTask.forHashes(ethContext, hashes, ethTasksTimer);
  }

  @Override
  protected void assertPartialResultMatchesExpectation(
      final Map<Hash, BytesValue> requestedData, final Map<Hash, BytesValue> partialResponse) {
    assertThat(partialResponse.size()).isLessThanOrEqualTo(requestedData.size());
    assertThat(partialResponse.size()).isGreaterThan(0);
    final List<BytesValue> requestData = new ArrayList<>(requestedData.values());
    final List<BytesValue> resultData = new ArrayList<>(partialResponse.values());
    assertThat(requestData).containsAll(resultData);
  }

  @Override
  protected void assertResultMatchesExpectation(
      final Map<Hash, BytesValue> requestedData,
      final PeerTaskResult<Map<Hash, BytesValue>> response,
      final EthPeer respondingPeer) {
    final List<BytesValue> requestData = new ArrayList<>(requestedData.values());
    final List<BytesValue> resultData = new ArrayList<>(response.getResult().values());
    assertThat(resultData).containsExactlyInAnyOrderElementsOf(requestData);
    assertThat(response.getPeer()).isEqualTo(respondingPeer);
  }
}

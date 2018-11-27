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
package tech.pegasys.pantheon.ethereum.p2p.discovery.internal;

import tech.pegasys.pantheon.ethereum.p2p.discovery.PeerDiscoveryStatus;
import tech.pegasys.pantheon.ethereum.p2p.peers.Endpoint;
import tech.pegasys.pantheon.ethereum.p2p.peers.Peer;
import tech.pegasys.pantheon.ethereum.p2p.peers.PeerId;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class RecursivePeerRefreshStateTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final List<TestPeer> aggregatePeerList = new ArrayList<>();

  private NeighborsPacketData neighborsPacketData_bootstrap;
  private NeighborsPacketData neighborsPacketData_000;
  private NeighborsPacketData neighborsPacketData_010;
  private NeighborsPacketData neighborsPacketData_011;
  private NeighborsPacketData neighborsPacketData_012;
  private NeighborsPacketData neighborsPacketData_013;

  private List<Peer> peerTable_000 = new ArrayList<>();
  private List<Peer> peerTable_010 = new ArrayList<>();
  private List<Peer> peerTable_011 = new ArrayList<>();
  private List<Peer> peerTable_012 = new ArrayList<>();
  private List<Peer> peerTable_013 = new ArrayList<>();

  private TestPeer peer_000;
  private TestPeer peer_010;
  private TestPeer peer_020;
  private TestPeer peer_021;
  private TestPeer peer_022;
  private TestPeer peer_023;
  private TestPeer peer_011;
  private TestPeer peer_120;
  private TestPeer peer_121;
  private TestPeer peer_122;
  private TestPeer peer_123;
  private TestPeer peer_012;
  private TestPeer peer_220;
  private TestPeer peer_221;
  private TestPeer peer_222;
  private TestPeer peer_223;
  private TestPeer peer_013;
  private TestPeer peer_320;
  private TestPeer peer_321;
  private TestPeer peer_322;
  private TestPeer peer_323;

  @Test
  public void yayaya() throws Exception {

    JsonNode peers =
        MAPPER.readTree(RecursivePeerRefreshStateTest.class.getResource("/peers.json"));

    peer_000 = generatePeer(peers);

    peer_010 = (TestPeer) peer_000.getPeerTable().get(0);

    peer_020 = (TestPeer) peer_010.getPeerTable().get(0);
    peer_021 = (TestPeer) peer_010.getPeerTable().get(1);
    peer_022 = (TestPeer) peer_010.getPeerTable().get(2);
    peer_023 = (TestPeer) peer_010.getPeerTable().get(3);

    peer_011 = (TestPeer) peer_000.getPeerTable().get(1);

    peer_120 = (TestPeer) peer_011.getPeerTable().get(0);
    peer_121 = (TestPeer) peer_011.getPeerTable().get(1);
    peer_122 = (TestPeer) peer_011.getPeerTable().get(2);
    peer_123 = (TestPeer) peer_011.getPeerTable().get(3);

    peer_012 = (TestPeer) peer_000.getPeerTable().get(2);

    peer_220 = (TestPeer) peer_012.getPeerTable().get(0);
    peer_221 = (TestPeer) peer_012.getPeerTable().get(1);
    peer_222 = (TestPeer) peer_012.getPeerTable().get(2);
    peer_223 = (TestPeer) peer_012.getPeerTable().get(3);

    peer_013 = (TestPeer) peer_000.getPeerTable().get(3);

    peer_320 = (TestPeer) peer_012.getPeerTable().get(0);
    peer_321 = (TestPeer) peer_012.getPeerTable().get(1);
    peer_322 = (TestPeer) peer_012.getPeerTable().get(2);
    peer_323 = (TestPeer) peer_012.getPeerTable().get(3);

    neighborsPacketData_bootstrap = NeighborsPacketData.create(Collections.singletonList(peer_000));
    neighborsPacketData_000 = NeighborsPacketData.create(peer_000.getPeerTable());
    neighborsPacketData_010 = NeighborsPacketData.create(peer_010.getPeerTable());
    neighborsPacketData_011 = NeighborsPacketData.create(peer_011.getPeerTable());
    neighborsPacketData_012 = NeighborsPacketData.create(peer_012.getPeerTable());
    neighborsPacketData_013 = NeighborsPacketData.create(peer_013.getPeerTable());

    addPeersToAggregateListByOrdinalRank();
  }

  private void addPeersToAggregateListByOrdinalRank() {
    aggregatePeerList.add(peer_323); // 1
    aggregatePeerList.add(peer_011); // 2
    aggregatePeerList.add(peer_012); // 3
    aggregatePeerList.add(peer_013); // 4
    aggregatePeerList.add(peer_020); // 5
    aggregatePeerList.add(peer_021); // 6
    aggregatePeerList.add(peer_022); // 7
    aggregatePeerList.add(peer_023); // 8
    aggregatePeerList.add(peer_120); // 9
    aggregatePeerList.add(peer_121); // 10
    aggregatePeerList.add(peer_122); // 11
    aggregatePeerList.add(peer_123); // 12
    aggregatePeerList.add(peer_220); // 13
    aggregatePeerList.add(peer_221); // 14
    aggregatePeerList.add(peer_222); // 15
    aggregatePeerList.add(peer_223); // 16
    aggregatePeerList.add(peer_320); // 17
    aggregatePeerList.add(peer_321); // 18
    aggregatePeerList.add(peer_322); // 19
    aggregatePeerList.add(peer_010); // 20
    aggregatePeerList.add(peer_000); // 21
  }

  private TestPeer generatePeer(final JsonNode peer) {
    int parent = peer.get("parent").asInt();
    int tier = peer.get("tier").asInt();
    int identifier = peer.get("identifier").asInt();
    int ordinalRank = peer.get("ordinalRank").asInt();
    BytesValue id = BytesValue.fromHexString(peer.get("id").asText());
    List<Peer> peerTable = new ArrayList<>();
    if (peer.get("peerTable") != null) {
      JsonNode peers = peer.get("peerTable");
      for (JsonNode element : peers) {
        peerTable.add(generatePeer(element));
      }
    } else {
      peerTable = Collections.emptyList();
    }
    return new TestPeer(parent, tier, identifier, ordinalRank, id, peerTable);
  }

  static class TestPeer implements Peer {
    int parent;
    int tier;
    int identifier;
    int ordinalRank;
    BytesValue id;
    List<Peer> peerTable;

    TestPeer(
        final int parent,
        final int tier,
        final int identifier,
        final int ordinalRank,
        final BytesValue id,
        final List<Peer> peerTable) {
      this.parent = parent;
      this.tier = tier;
      this.identifier = identifier;
      this.ordinalRank = ordinalRank;
      this.id = id;
      this.peerTable = peerTable;
    }

    int getOrdinalRank() {
      return ordinalRank;
    }

    @Override
    public List<Peer> getPeerTable() {
      return peerTable;
    }

    @Override
    public BytesValue getId() {
      return this.id;
    }

    @Override
    public Bytes32 keccak256() {
      return null;
    }

    @Override
    public Endpoint getEndpoint() {
      return null;
    }

    @Override
    public PeerId setFirstDiscovered(long firstDiscovered) {
      return null;
    }

    @Override
    public void setStatus(PeerDiscoveryStatus status) {}

    @Override
    public void setLastContacted(long lastContacted) {}

    @Override
    public String toString() {
      return parent + "." + tier + "." + identifier;
    }
  }
}
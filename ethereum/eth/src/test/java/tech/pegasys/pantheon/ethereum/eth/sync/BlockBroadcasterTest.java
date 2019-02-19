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
package tech.pegasys.pantheon.ethereum.eth.sync;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockBody;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeer;
import tech.pegasys.pantheon.ethereum.eth.manager.EthPeers;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.Test;

public class BlockBroadcasterTest {

  @Test
  public void blockPropagationUnitTest() {
    final EthPeer ethPeer = mock(EthPeer.class);
    final EthPeers ethPeers = mock(EthPeers.class);
    when(ethPeers.availablePeers()).thenReturn(Stream.of(ethPeer));

    final EthContext ethContext = mock(EthContext.class);
    when(ethContext.getEthPeers()).thenReturn(ethPeers);

    final BlockBroadcaster blockBroadcaster = new BlockBroadcaster(ethContext);
    final Block block = generateBlock();

    blockBroadcaster.propagate(block, UInt256.ZERO);

    verify(ethPeer, times(1)).propagateBlock(any(), any());
  }

  @Test
  public void blockPropagationUnitTestSeenUnseen() {
    final EthPeer ethPeer0 = mock(EthPeer.class);
    when(ethPeer0.hasSeenBlock(any())).thenReturn(true);

    final EthPeer ethPeer1 = mock(EthPeer.class);

    final EthPeers ethPeers = mock(EthPeers.class);
    when(ethPeers.availablePeers()).thenReturn(Stream.of(ethPeer0, ethPeer1));

    final EthContext ethContext = mock(EthContext.class);
    when(ethContext.getEthPeers()).thenReturn(ethPeers);

    final BlockBroadcaster blockBroadcaster = new BlockBroadcaster(ethContext);
    final Block block = generateBlock();
    blockBroadcaster.propagate(block, UInt256.ZERO);

    verify(ethPeer0, never()).propagateBlock(any(), any());
    verify(ethPeer1, times(1)).propagateBlock(any(), any());
  }

  @Test
  public void blockPropagationRejectOnHasSeenBlock() {
    final EthPeer ethPeer = mock(EthPeer.class);
    when(ethPeer.hasSeenBlock(any())).thenReturn(true);

    final EthPeers ethPeers = mock(EthPeers.class);
    when(ethPeers.availablePeers()).thenReturn(Stream.of(ethPeer));

    final EthContext ethContext = mock(EthContext.class);
    when(ethContext.getEthPeers()).thenReturn(ethPeers);

    final BlockBroadcaster blockBroadcaster = new BlockBroadcaster(ethContext);
    final Block block = generateBlock();
    blockBroadcaster.propagate(block, UInt256.ZERO);

    verify(ethPeer, never()).propagateBlock(any(), any());
  }

  private Block generateBlock() {
    final BlockBody body = new BlockBody(Collections.emptyList(), Collections.emptyList());
    return new Block(new BlockHeaderTestFixture().buildHeader(), body);
  }
}

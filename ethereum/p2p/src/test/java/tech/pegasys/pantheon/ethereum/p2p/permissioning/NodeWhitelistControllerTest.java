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
package tech.pegasys.pantheon.ethereum.p2p.permissioning;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static tech.pegasys.pantheon.ethereum.p2p.permissioning.NodeWhitelistController.NodesWhitelistResult;

import tech.pegasys.pantheon.ethereum.p2p.peers.DefaultPeer;
import tech.pegasys.pantheon.ethereum.p2p.peers.Peer;
import tech.pegasys.pantheon.ethereum.permissioning.PermissioningConfiguration;
import tech.pegasys.pantheon.ethereum.permissioning.WhitelistFileSyncException;
import tech.pegasys.pantheon.ethereum.permissioning.WhitelistOperationResult;
import tech.pegasys.pantheon.ethereum.permissioning.WhitelistPersistor;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodeWhitelistControllerTest {

  @Mock private WhitelistPersistor whitelistPersistor;
  private NodeWhitelistController controller;

  private final String enode1 =
      "enode://6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.168.0.10:4567";
  private final String enode2 =
      "enode://5f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.168.0.10:4567";

  @Before
  public void setUp() {
    controller =
        new NodeWhitelistController(PermissioningConfiguration.createDefault(), whitelistPersistor);
  }

  @Test
  public void whenAddNodesInputHasExistingNodeShouldReturnAddErrorExistingEntry() {
    controller.addNode(DefaultPeer.fromURI(enode1));

    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_EXISTING_ENTRY);
    NodesWhitelistResult actualResult =
        controller.addNodes(
            Lists.newArrayList(DefaultPeer.fromURI(enode1), DefaultPeer.fromURI(enode2)));

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenAddNodesInputHasDuplicatedNodesShouldReturnDuplicatedEntryError() {
    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_DUPLICATED_ENTRY);

    NodesWhitelistResult actualResult =
        controller.addNodes(
            Arrays.asList(DefaultPeer.fromURI(enode1), DefaultPeer.fromURI(enode1)));

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenAddNodesInputHasEmptyListOfNodesShouldReturnErrorEmptyEntry() {
    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_EMPTY_ENTRY);
    NodesWhitelistResult actualResult = controller.removeNodes(new ArrayList<>());

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenAddNodesInputHasNullListOfNodesShouldReturnErrorEmptyEntry() {
    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_EMPTY_ENTRY);
    NodesWhitelistResult actualResult = controller.removeNodes(null);

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenRemoveNodesInputHasAbsentNodeShouldReturnRemoveErrorAbsentEntry() {
    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_ABSENT_ENTRY);
    NodesWhitelistResult actualResult =
        controller.removeNodes(
            Lists.newArrayList(DefaultPeer.fromURI(enode1), DefaultPeer.fromURI(enode2)));

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenRemoveNodesInputHasDuplicateNodesShouldReturnErrorDuplicatedEntry() {
    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_DUPLICATED_ENTRY);
    NodesWhitelistResult actualResult =
        controller.removeNodes(
            Lists.newArrayList(DefaultPeer.fromURI(enode1), DefaultPeer.fromURI(enode1)));

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenRemoveNodesInputHasEmptyListOfNodesShouldReturnErrorEmptyEntry() {
    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_EMPTY_ENTRY);
    NodesWhitelistResult actualResult = controller.removeNodes(new ArrayList<>());

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenRemoveNodesInputHasNullListOfNodesShouldReturnErrorEmptyEntry() {
    NodesWhitelistResult expected =
        new NodesWhitelistResult(WhitelistOperationResult.ERROR_EMPTY_ENTRY);
    NodesWhitelistResult actualResult = controller.removeNodes(null);

    assertThat(actualResult).isEqualToComparingOnlyGivenFields(expected, "result");
  }

  @Test
  public void whenNodeIdsAreDifferentItShouldNotBePermitted() {
    Peer peer1 =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0xaaaa80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30303);
    Peer peer2 =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0xbbba80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30303);

    controller.addNode(peer1);

    assertThat(controller.isPermitted(peer2)).isFalse();
  }

  @Test
  public void whenNodesHostsAreDifferentItShouldNotBePermitted() {
    Peer peer1 =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0xaaaa80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30303);
    Peer peer2 =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0xaaaa80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.2",
            30303);

    controller.addNode(peer1);

    assertThat(controller.isPermitted(peer2)).isFalse();
  }

  @Test
  public void whenNodesUdpPortsAreDifferentItShouldNotBePermitted() {
    Peer peer1 =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0xaaaa80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30301);
    Peer peer2 =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0xaaaa80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30302);

    controller.addNode(peer1);

    assertThat(controller.isPermitted(peer2)).isFalse();
  }

  @Test
  public void whenCheckingIfNodeIsPermittedTcpPortShouldNotBeConsideredIfAbsent() {
    Peer peerWithTcpPortSet =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0x6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30303,
            10001);
    Peer peerWithoutTcpPortSet =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0x6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30303);

    controller.addNode(peerWithoutTcpPortSet);

    assertThat(controller.isPermitted(peerWithTcpPortSet)).isTrue();
  }

  @Test
  public void whenCheckingIfNodeIsPermittedTcpPortShouldBeConsideredIfPresent() {
    Peer peerWithTcpPortSet =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0x6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30303,
            10001);
    Peer peerWithDifferentTcpPortSet =
        new DefaultPeer(
            BytesValue.fromHexString(
                "0x6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0"),
            "127.0.0.1",
            30303,
            10002);

    controller.addNode(peerWithDifferentTcpPortSet);

    assertThat(controller.isPermitted(peerWithTcpPortSet)).isFalse();
  }

  @Test
  public void stateShouldRevertIfWhitelistPersistFails()
      throws IOException, WhitelistFileSyncException {
    List<Peer> newNode1 = singletonList(DefaultPeer.fromURI(enode1));
    List<Peer> newNode2 = singletonList(DefaultPeer.fromURI(enode2));

    assertThat(controller.getNodesWhitelist().size()).isEqualTo(0);

    controller.addNodes(newNode1);
    assertThat(controller.getNodesWhitelist().size()).isEqualTo(1);

    doThrow(new IOException()).when(whitelistPersistor).updateConfig(any(), any());
    controller.addNodes(newNode2);

    assertThat(controller.getNodesWhitelist().size()).isEqualTo(1);
    assertThat(controller.getNodesWhitelist()).isEqualTo(newNode1);

    verify(whitelistPersistor, times(3)).verifyConfigFileMatchesState(any(), any());
    verify(whitelistPersistor, times(2)).updateConfig(any(), any());
    verifyNoMoreInteractions(whitelistPersistor);
  }

  @Test
  public void reloadNodeWhitelistWithValidConfigFileShouldUpdateWhitelist() throws Exception {
    final String expectedEnodeURL =
        "enode://6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.168.0.9:4567";
    final Path permissionsFile = createPermissionsFileWithNode(expectedEnodeURL);
    final PermissioningConfiguration permissioningConfig = mock(PermissioningConfiguration.class);

    when(permissioningConfig.getConfigurationFilePath())
        .thenReturn(permissionsFile.toAbsolutePath().toString());
    when(permissioningConfig.isNodeWhitelistEnabled()).thenReturn(true);
    when(permissioningConfig.getNodeWhitelist())
        .thenReturn(Arrays.asList(URI.create(expectedEnodeURL)));
    controller = new NodeWhitelistController(permissioningConfig);

    controller.reload();

    assertThat(controller.getNodesWhitelist())
        .containsExactly(DefaultPeer.fromURI(expectedEnodeURL));
  }

  @Test
  public void reloadNodeWhitelistWithErrorReadingConfigFileShouldKeepOldWhitelist() {
    final String expectedEnodeURI =
        "enode://aaaa80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.168.0.9:4567";
    final PermissioningConfiguration permissioningConfig = mock(PermissioningConfiguration.class);

    when(permissioningConfig.getConfigurationFilePath()).thenReturn("foo");
    when(permissioningConfig.isNodeWhitelistEnabled()).thenReturn(true);
    when(permissioningConfig.getNodeWhitelist())
        .thenReturn(Arrays.asList(URI.create(expectedEnodeURI)));
    controller = new NodeWhitelistController(permissioningConfig);

    final Throwable thrown = catchThrowable(() -> controller.reload());

    assertThat(thrown)
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Unable to read permissions TOML config file");

    assertThat(controller.getNodesWhitelist())
        .containsExactly(DefaultPeer.fromURI(expectedEnodeURI));
  }

  private Path createPermissionsFileWithNode(final String node) throws IOException {
    final String nodePermissionsFileContent = "nodes-whitelist=[\"" + node + "\"]";
    final Path permissionsFile = Files.createTempFile("node_permissions", "");
    Files.write(permissionsFile, nodePermissionsFileContent.getBytes(StandardCharsets.UTF_8));
    return permissionsFile;
  }
}

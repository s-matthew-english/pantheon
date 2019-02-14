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
package tech.pegasys.pantheon.ethereum.eth.sync.fastsync;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.pantheon.ethereum.eth.sync.fastsync.FastSyncState.EMPTY_SYNC_STATE;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderTestFixture;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHashFunction;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FastSyncStateStorageTest {
  @Rule public final TemporaryFolder tempDirRule = new TemporaryFolder();

  private FastSyncStateStorage storage;
  private final BlockHeader pivotBlockHeader = new BlockHeaderTestFixture().buildHeader();
  private final FastSyncState syncStateWithHeader = new FastSyncState(pivotBlockHeader);
  private File tempDir;

  @Before
  public void setUp() throws Exception {
    tempDir = tempDirRule.newFolder();
    storage = new FastSyncStateStorage(tempDir.toPath());
  }

  @Test
  public void shouldNotConsiderFastSyncInProgressWhenFileDoesNotExist() {
    assertThat(storage.isFastSyncInProgress()).isFalse();
  }

  @Test
  public void shouldConsiderFastSyncInProgressWhenFileExists() {
    storage.storeState(syncStateWithHeader);
    assertThat(storage.isFastSyncInProgress()).isTrue();

    final FastSyncStateStorage newStorage = new FastSyncStateStorage(tempDir.toPath());
    assertThat(newStorage.isFastSyncInProgress()).isTrue();
  }

  @Test
  public void shouldRoundTripHeader() {
    storage.storeState(syncStateWithHeader);
    assertThat(storage.loadState(MainnetBlockHashFunction::createHash))
        .isEqualTo(syncStateWithHeader);

    final FastSyncStateStorage newStorage = new FastSyncStateStorage(tempDir.toPath());
    assertThat(newStorage.loadState(MainnetBlockHashFunction::createHash))
        .isEqualTo(syncStateWithHeader);
  }

  @Test
  public void shouldReturnEmptyWhenLoadingHeaderAndFileDoesNotExist() {
    assertThat(storage.loadState(MainnetBlockHashFunction::createHash)).isEqualTo(EMPTY_SYNC_STATE);
  }

  @Test
  public void shouldRemoveStateFileWhenStoringFastSyncWithoutBlockHeader() {
    storage.storeState(syncStateWithHeader);
    assertThat(storage.loadState(MainnetBlockHashFunction::createHash))
        .isEqualTo(syncStateWithHeader);

    storage.storeState(EMPTY_SYNC_STATE);
    assertThat(storage.loadState(MainnetBlockHashFunction::createHash)).isEqualTo(EMPTY_SYNC_STATE);
  }
}

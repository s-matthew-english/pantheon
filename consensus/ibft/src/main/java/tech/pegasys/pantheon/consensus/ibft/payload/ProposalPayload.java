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
package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.messagedata.IbftV2;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;

import java.util.Objects;
import java.util.StringJoiner;

public class ProposalPayload implements Payload {
  private static final int TYPE = IbftV2.PROPOSAL;
  private final ConsensusRoundIdentifier roundIdentifier;
  private final Hash digest;

  public ProposalPayload(final ConsensusRoundIdentifier roundIdentifier, final Hash digest) {
    this.roundIdentifier = roundIdentifier;
    this.digest = digest;
  }

  public static ProposalPayload readFrom(final RLPInput rlpInput) {
    rlpInput.enterList();
    final ConsensusRoundIdentifier roundIdentifier = ConsensusRoundIdentifier.readFrom(rlpInput);
    final Hash digest = Hash.wrap(rlpInput.readBytes32());
    rlpInput.leaveList();

    return new ProposalPayload(roundIdentifier, digest);
  }

  @Override
  public void writeTo(final RLPOutput rlpOutput) {
    rlpOutput.startList();
    roundIdentifier.writeTo(rlpOutput);
    rlpOutput.writeBytesValue(digest);
    rlpOutput.endList();
  }

  public Hash getDigest() {
    return digest;
  }

  @Override
  public int getMessageType() {
    return TYPE;
  }

  @Override
  public ConsensusRoundIdentifier getRoundIdentifier() {
    return roundIdentifier;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ProposalPayload that = (ProposalPayload) o;
    return Objects.equals(roundIdentifier, that.roundIdentifier)
        && Objects.equals(digest, that.digest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roundIdentifier, digest);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ProposalPayload.class.getSimpleName() + "[", "]")
        .add("roundIdentifier=" + roundIdentifier)
        .add("digest=" + digest)
        .toString();
  }
}

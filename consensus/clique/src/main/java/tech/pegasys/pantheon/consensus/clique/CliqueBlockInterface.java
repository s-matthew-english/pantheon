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
package tech.pegasys.pantheon.consensus.clique;

import tech.pegasys.pantheon.consensus.common.BlockInterface;
import tech.pegasys.pantheon.consensus.common.ValidatorVote;
import tech.pegasys.pantheon.consensus.common.VoteType;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderBuilder;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collection;
import java.util.Optional;

import com.google.common.collect.ImmutableBiMap;

public class CliqueBlockInterface implements BlockInterface {

  public static final Address NO_VOTE_SUBJECT =
      Address.wrap(BytesValue.wrap(new byte[Address.SIZE]));

  public static final long ADD_NONCE = 0xFFFFFFFFFFFFFFFFL;
  public static final long DROP_NONCE = 0x0L;

  private static final ImmutableBiMap<VoteType, Long> voteToValue =
      ImmutableBiMap.of(
          VoteType.ADD, ADD_NONCE,
          VoteType.DROP, DROP_NONCE);

  @Override
  public Address getProposerOfBlock(final BlockHeader header) {
    final CliqueExtraData cliqueExtraData = CliqueExtraData.decode(header.getExtraData());
    return CliqueBlockHashing.recoverProposerAddress(header, cliqueExtraData);
  }

  @Override
  public Optional<ValidatorVote> extractVoteFromHeader(final BlockHeader header) {
    final Address candidate = header.getCoinbase();
    if (!candidate.equals(NO_VOTE_SUBJECT)) {
      final Address proposer = getProposerOfBlock(header);
      final VoteType votePolarity = voteToValue.inverse().get(header.getNonce());
      final Address recipient = header.getCoinbase();

      return Optional.of(new ValidatorVote(votePolarity, proposer, recipient));
    }
    return Optional.empty();
  }

  public static BlockHeaderBuilder createHeaderBuilderWithVoteHeaders(
      final BlockHeaderBuilder builder, final Optional<ValidatorVote> vote) {
    final BlockHeaderBuilder voteHeaderBuilder = BlockHeaderBuilder.fromBuilder(builder);
    if (vote.isPresent()) {
      final ValidatorVote voteToCast = vote.get();
      voteHeaderBuilder.nonce(voteToValue.get(voteToCast.getVotePolarity()));
      voteHeaderBuilder.coinbase(voteToCast.getRecipient());
    } else {
      voteHeaderBuilder.nonce(voteToValue.get(VoteType.DROP));
      voteHeaderBuilder.coinbase(NO_VOTE_SUBJECT);
    }
    return voteHeaderBuilder;
  }

  @Override
  public Collection<Address> validatorsInBlock(final BlockHeader header) {
    return CliqueExtraData.decode(header.getExtraData()).getValidators();
  }

  public static boolean isValidVoteValue(final long value) {
    return voteToValue.values().contains(value);
  }
}

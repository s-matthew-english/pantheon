package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.exception.InvalidJsonRpcParameters;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonCallParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.Quantity;
import tech.pegasys.pantheon.ethereum.mainnet.ValidationResult;
import tech.pegasys.pantheon.ethereum.transaction.CallParameter;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulatorResult;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TraceCallTest {

    private TraceCall method;

    @Mock
    private BlockchainQueries blockchainQueries;
    @Mock private TransactionSimulator transactionSimulator;

    @Before
    public void setUp() {
        method = new TraceCall(blockchainQueries, transactionSimulator, new JsonRpcParameter());
    }

    @Test
    public void shouldReturnCorrectMethodName() {
        assertThat(method.getName()).isEqualTo("trace_call");
    }

    @Test
    public void shouldThrowInvalidJsonRpcParametersExceptionWhenMissingToField() {
        final CallParameter callParameter = new JsonCallParameter("0x0", null, "0x0", "0x0", "0x0", "");
        final JsonRpcRequest request = traceCallRequest(callParameter, "latest");

        final Throwable thrown = catchThrowable(() -> method.response(request));

        assertThat(thrown)
                .isInstanceOf(InvalidJsonRpcParameters.class)
                .hasNoCause()
                .hasMessage("Missing \"to\" field in call arguments");
    }

    @Test
    public void shouldReturnNullWhenProcessorReturnsEmpty() {
        final JsonRpcRequest request = traceCallRequest(callParameter(), "latest");
        final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(null, null);

        when(transactionSimulator.process(any(), anyLong())).thenReturn(Optional.empty());

        final JsonRpcResponse response = method.response(request);

        assertThat(response).isEqualToComparingFieldByField(expectedResponse);
        verify(transactionSimulator).process(any(), anyLong());
    }

    @Test
    public void shouldAcceptRequestWhenMissingOptionalFields() {
        final CallParameter callParameter = new JsonCallParameter(null, "0x0", null, null, null, null);
        final JsonRpcRequest request = traceCallRequest(callParameter, "latest");
        final JsonRpcResponse expectedResponse =
                new JsonRpcSuccessResponse(null, BytesValue.of().toString());

        mockTransactionProcessorSuccessResult(BytesValue.of());

        final JsonRpcResponse response = method.response(request);

        assertThat(response).isEqualToComparingFieldByFieldRecursively(expectedResponse);
        verify(transactionSimulator).process(eq(callParameter), anyLong());
    }

    @Test
    public void shouldReturnExecutionResultWhenExecutionIsSuccessful() {
        final JsonRpcRequest request = traceCallRequest(callParameter(), "latest");
        final JsonRpcResponse expectedResponse =
                new JsonRpcSuccessResponse(null, BytesValue.of(1).toString());
        mockTransactionProcessorSuccessResult(BytesValue.of(1));

        final JsonRpcResponse response = method.response(request);

        assertThat(response).isEqualToComparingFieldByFieldRecursively(expectedResponse);
        verify(transactionSimulator).process(eq(callParameter()), anyLong());
    }

    @Test
    public void shouldUseCorrectBlockNumberWhenLatest() {
        final JsonRpcRequest request = traceCallRequest(callParameter(), "latest");
        when(blockchainQueries.headBlockNumber()).thenReturn(11L);
        when(transactionSimulator.process(any(), anyLong())).thenReturn(Optional.empty());

        method.response(request);

        verify(transactionSimulator).process(any(), eq(11L));
    }

    @Test
    public void shouldUseCorrectBlockNumberWhenEarliest() {
        final JsonRpcRequest request = traceCallRequest(callParameter(), "earliest");
        when(transactionSimulator.process(any(), anyLong())).thenReturn(Optional.empty());
        method.response(request);

        verify(transactionSimulator).process(any(), eq(0L));
    }

    @Test
    public void shouldUseCorrectBlockNumberWhenSpecified() {
        final JsonRpcRequest request = traceCallRequest(callParameter(), Quantity.create(13L));
        when(transactionSimulator.process(any(), anyLong())).thenReturn(Optional.empty());

        method.response(request);

        verify(transactionSimulator).process(any(), eq(13L));
    }

    private CallParameter callParameter() {
        return new JsonCallParameter("0x0", "0x0", "0x0", "0x0", "0x0", "");
    }

    private JsonRpcRequest traceCallRequest(
            final CallParameter callParameter, final String blockNumberInHex) {
        return new JsonRpcRequest("2.0", "trace_call", new Object[] {callParameter, blockNumberInHex});
    }

    private void mockTransactionProcessorSuccessResult(final BytesValue output) {
        final TransactionSimulatorResult result = mock(TransactionSimulatorResult.class);

        when(result.getValidationResult()).thenReturn(ValidationResult.valid());
        when(result.getOutput()).thenReturn(output);
        when(transactionSimulator.process(any(), anyLong())).thenReturn(Optional.of(result));
    }
}

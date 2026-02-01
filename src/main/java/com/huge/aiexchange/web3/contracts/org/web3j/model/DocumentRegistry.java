package com.huge.aiexchange.web3.contracts.org.web3j.model;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.9.8.
 */
@SuppressWarnings("rawtypes")
public class DocumentRegistry extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b506103a88061001c5f395ff3fe608060405234801561000f575f5ffd5b5060043610610034575f3560e01c80636a33bf871461003857806373ca4a5b1461005f575b5f5ffd5b61004b610046366004610175565b610072565b604051901515815260200160405180910390f35b61004b61006d366004610175565b61011e565b5f5f83836040516020016100879291906101e3565b60408051601f1981840301815291815281516020928301205f81815292839052912080546001600160a01b031916331781554260018201559091506002016100d084868361028a565b50336001600160a01b03167f4208034e4449bb439a4b1ba16ff193bec2c83ec39cd969a629aa475c7f0b8047858560405161010c929190610344565b60405180910390a25060019392505050565b5f5f6001600160a01b03165f5f858560405160200161013e9291906101e3565b60408051808303601f190181529181528151602092830120835290820192909252015f20546001600160a01b031614159392505050565b5f5f60208385031215610186575f5ffd5b823567ffffffffffffffff81111561019c575f5ffd5b8301601f810185136101ac575f5ffd5b803567ffffffffffffffff8111156101c2575f5ffd5b8560208284010111156101d3575f5ffd5b6020919091019590945092505050565b818382375f9101908152919050565b634e487b7160e01b5f52604160045260245ffd5b600181811c9082168061021a57607f821691505b60208210810361023857634e487b7160e01b5f52602260045260245ffd5b50919050565b601f82111561028557805f5260205f20601f840160051c810160208510156102635750805b601f840160051c820191505b81811015610282575f815560010161026f565b50505b505050565b67ffffffffffffffff8311156102a2576102a26101f2565b6102b6836102b08354610206565b8361023e565b5f601f8411600181146102e7575f85156102d05750838201355b5f19600387901b1c1916600186901b178355610282565b5f83815260208120601f198716915b8281101561031657868501358255602094850194600190920191016102f6565b5086821015610332575f1960f88860031b161c19848701351681555b505060018560011b0183555050505050565b60208152816020820152818360408301375f818301604090810191909152601f909201601f1916010191905056fea26469706673582212209857d1d70604cb7c89b080b08ee54a818f96891dadf408df1f15aefd97d7caac64736f6c634300081d0033";

    public static final String FUNC_ISNOTARIZED = "isNotarized";

    public static final String FUNC_NOTARIZEDOCUMENT = "notarizeDocument";

    public static final Event NOTARIZED_EVENT = new Event("Notarized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    @Deprecated
    protected DocumentRegistry(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected DocumentRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected DocumentRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected DocumentRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

//    public static List<NotarizedEventResponse> getNotarizedEvents(TransactionReceipt transactionReceipt) {
//        Log log=transactionReceipt.getLogs().get(0);
//        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(NOTARIZED_EVENT, transactionReceipt);
//        ArrayList<NotarizedEventResponse> responses = new ArrayList<NotarizedEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            NotarizedEventResponse typedResponse = new NotarizedEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse._signer = (String) eventValues.getIndexedValues().get(0).getValue();
//            typedResponse._documentHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static NotarizedEventResponse getNotarizedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(NOTARIZED_EVENT, log);
        NotarizedEventResponse typedResponse = new NotarizedEventResponse();
        typedResponse.log = log;
        typedResponse._signer = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse._documentHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<NotarizedEventResponse> notarizedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getNotarizedEventFromLog(log));
    }

    public Flowable<NotarizedEventResponse> notarizedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NOTARIZED_EVENT));
        return notarizedEventFlowable(filter);
    }

    public RemoteFunctionCall<Boolean> isNotarized(String _documentHash) {
        final Function function = new Function(FUNC_ISNOTARIZED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_documentHash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> notarizeDocument(String _documentHash) {
        final Function function = new Function(
                FUNC_NOTARIZEDOCUMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_documentHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static DocumentRegistry load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new DocumentRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static DocumentRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new DocumentRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static DocumentRegistry load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new DocumentRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static DocumentRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new DocumentRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<DocumentRegistry> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(DocumentRegistry.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<DocumentRegistry> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(DocumentRegistry.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<DocumentRegistry> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(DocumentRegistry.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<DocumentRegistry> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(DocumentRegistry.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class NotarizedEventResponse extends BaseEventResponse {
        public String _signer;

        public String _documentHash;
    }
}

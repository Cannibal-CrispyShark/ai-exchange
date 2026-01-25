package com.huge.aiexchange.web3;

import java.util.List;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.AlphaVantageException;
import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import org.springframework.stereotype.Component;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import com.huge.aiexchange.web3.contracts.org.web3j.model.DocumentRegistry;

import static java.lang.Thread.sleep;

@Component
public class Exmaple {

    public static void main(String[] args) {
    }

    public void testAlphaVantage() {
        AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol("BTC")
                .outputSize(OutputSize.COMPACT)
                .onSuccess(e->handleSuccess((TimeSeriesResponse)e))
                .onFailure(e->handleFailure(e))
                .fetch();

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void handleSuccess(TimeSeriesResponse response) {
        // 修复：更正变量名并添加简单的打印逻辑替代plotGraph
        List<StockUnit> stockUnits = response.getStockUnits();
        System.out.println("Retrieved " + stockUnits.size() + " stock units");
        // 如果需要绘图功能，请添加相应的绘图库依赖和实现
        for (StockUnit stockUnit : stockUnits) {
            System.out.println("Date: " + stockUnit.getDate());
            System.out.println("Open: " + stockUnit.getOpen());
            System.out.println("High: " + stockUnit.getHigh());
            System.out.println("Low: " + stockUnit.getLow());
            System.out.println("Close: " + stockUnit.getClose());
            System.out.println("Adj Close: " + stockUnit.getAdjustedClose());
            System.out.println("Volume: " + stockUnit.getVolume());
            System.out.println("Dividend Amount: " + stockUnit.getDividendAmount());
            System.out.println("Split Coefficient: " + stockUnit.getSplitCoefficient());
            System.out.println();
        }
    }

    public static void handleFailure(AlphaVantageException error) {
        System.out.println("API request failed: " + error.getMessage());
    }




//    public static void main(String[] args) {
//        try {
//
//            Web3j web3 = Web3j.build(new HttpService("http://localhost:8545"));
//
//            Credentials credentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d");
//
//            DocumentRegistry documentRegistry = DocumentRegistry.deploy(web3, credentials, new DefaultGasProvider()).send();
//
//            String contractAddress = documentRegistry.getContractAddress();
//
//            DocumentRegistry registryContract = documentRegistry.load(contractAddress, web3, credentials, new DefaultGasProvider());
//
//            EthFilter filter=new EthFilter(
//                    DefaultBlockParameterName.LATEST,
//                    DefaultBlockParameterName.LATEST,
//                    List.of(contractAddress)
//            );
//
//            registryContract.notarizedEventFlowable(filter).subscribe(event ->{
//                String signer =event._signer;
//                String hash = event._documentHash;
//                System.out.println("signer : "+ signer  +"hash :" + hash );
//            });
//
//
//            TransactionReceipt transactionReceipt = registryContract.notarizeDocument("你好我是hugeeeeee").send();
//
//            System.out.println(transactionReceipt.getBlockNumber());
//
//            sleep(20000);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }

//    public static void main(String[] args) {
//
//        Web3j web3 = Web3j.build(new HttpService("http://localhost:8545"));
//
//        String privateKey = "secr3t";
//
//        String path = "src/main/resources/wallet/" + "UTC--2026-01-07T07-13-23.575459300Z--141efa98f80098f0abafd10b1bc3ab937a8b1229.json";
//
//        try {
//
//            final Credentials recipient = Credentials.create("0x6cbed15c793ce57650b9877cf6fa156fbef513c4e6134f022a85b1ffdd59b2a1");
//
//            System.out.println("recipient address: "+recipient.getAddress());
//            System.out.println("recipient private key: "+recipient.getEcKeyPair().getPrivateKey());
//            System.out.println("recipient balance: "+ web3.ethGetBalance(recipient.getAddress(), new DefaultBlockParameterNumber(0)).send().getBalance().toString());
//
//            String senderAddress = "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1";
//            final Credentials senderCredentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d");
//            EthGetBalance balance = web3.ethGetBalance(senderAddress,  new DefaultBlockParameterNumber(0)).send();
//
//            System.out.println("sender balance: "+ balance.getBalance().toString());
//
//            final BigInteger nonce = web3.ethGetTransactionCount(senderAddress,  new DefaultBlockParameterNumber(0)).send().getTransactionCount();
//
//            BigInteger gasLimit= BigInteger.valueOf(21000);
//            BigInteger gasPrice = Convert .toWei("1", Convert.Unit.GWEI).toBigInteger();
//            BigInteger value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger();
//
//            final RawTransaction transaction = RawTransaction.createEtherTransaction(
//                    nonce,
//                    gasPrice,
//                    gasLimit,
//                    recipient.getAddress(),
//                    value
//            );
//            byte[] bytes = TransactionEncoder.signMessage(transaction, senderCredentials);
//            String hexString = Numeric.toHexString(bytes);
//
//            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexString).send();
//            final String transactionHash = ethSendTransaction.getTransactionHash();
//
//
//            Optional<TransactionReceipt> transactionReceipt = null;
//            do{
//                System.out.println("Waiting for transaction  "+ transactionHash +" to be mined...");
//                EthGetTransactionReceipt receipt = web3.ethGetTransactionReceipt(transactionHash).send();
//                transactionReceipt = receipt.getTransactionReceipt();
//            }while(transactionReceipt.isEmpty());
//
//            System.out.println("recipient balance: "+ web3.ethGetBalance(recipient.getAddress(),  new DefaultBlockParameterNumber(0)).send().getBalance().toString());
//            System.out.println("sender balance: "+ web3.ethGetBalance(senderAddress,  new DefaultBlockParameterNumber(0)).send().getBalance().toString());
//
//
//        } catch (Exception e) {
//            System.out.println("Error: Fail to Transfer" + e.getMessage());
//            e.printStackTrace();
//        }

//    }




}

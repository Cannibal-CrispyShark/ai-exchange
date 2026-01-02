package com.huge.aiexchange.web3;

import java.io.IOException;

import lombok.val;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.utils.Convert;


public class Exmaple {

    public static void main(String[] args) {

        Web3j web3 = Web3j.build(new HttpService("http://localhost:8545"));

        try {

            Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();

            System.out.println(web3ClientVersion.getWeb3ClientVersion());

            EthGetBalance balance = web3.ethGetBalance("0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1", DefaultBlockParameterName.LATEST).send();

            System.out.println(balance.getBalance());

            System.out.println(Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER));

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}

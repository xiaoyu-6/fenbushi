package com.nonRepudiation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.NonRepudiation;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.xml.bind.DatatypeConverter;
//智能合约的调用
public class ContractCall {

    private String file; // file to be outsourced
    private BigInteger data_s2; // the file data of S2

    private int messageBitLength; // the bit length of a data block; it determines the underlying finite field of the message.
    private long fileSize; // the size of the file in bytes

    private int blocks; // total number of data blocks, which is equal to

    private int randomBlock = 5;

    private static final Logger log = LoggerFactory.getLogger(ContractCall.class);

    public static void main(String[] args) throws Exception {
        ContractCall file = new ContractCall("C:\\Users\\sobieski\\Desktop\\Blockchain_Based_Non-repudiable_IoT_Data_Trading_Simpler_Faster_and_Cheaper.pdf");
        String hash_s1 = file.getHashOfS1();
        log.info("hash of s1: " + hash_s1);
        String s2 = file.getS2() + "";
        log.info("s2: " + s2);
        String hash_s2 = SHA256Util.getSHA256StrJava(s2);
        log.info("hash of s2: " + hash_s2);
        String verifyResult = file.getVerifyResultOfFile(hash_s1, hash_s2);
        log.info("verify result of s: " + verifyResult);
        file.run(hash_s1, s2, verifyResult);
    }

    //构造函数，将文件分为两部分，大部分为 S1，小部分为 S2
    public ContractCall(String file)
    {
        // 1. Divide the file into two parts, Large S1 and Small S2
        this.file = file;
        java.io.File f = new java.io.File(file);

        this.fileSize = f.length();
        this.messageBitLength = 128; // each blocks sizes

        this.blocks = (int) Math.ceil(this.fileSize * 8 / (double) messageBitLength);

        byte[] block = new byte[this.messageBitLength / 8];


        try
        {
            FileOutputStream file_s1 = new FileOutputStream("S1");
            FileOutputStream file_s2 = new FileOutputStream("S2");

            FileInputStream source = new FileInputStream(this.file);

            for (int i = 0; i < this.blocks; i++)
            {
                source.read(block);

                // write S2
                if (i == randomBlock) {
                    file_s2.write(block);
                    this.data_s2 = new BigInteger(1, block);
                }
                // write S1
                else {
                    file_s1.write(block);
                }
                // fill zero
                Arrays.fill(block, (byte)0);
            }
            source.close();
            file_s1.close();
            file_s2.close();
        } catch (Exception e)
        {
            System.out.println("Exception in InnerProductBasedVS when reading file into memory: " + e);
        }

    }

    //获取 S1 的哈希值
    public String getHashOfS1() {
        String res = getFileSHA1("S1");
        int len = res.length();
        if (res.length() < 64) {
            for (int i=0; i<64-len; i++) {
                res = "0" + res;
            }
        }
        return res;
    }

    //获取 S2 的数据
    public BigInteger getS2() {
        return this.data_s2;
    }

    //获取文件验证结果
    public String getVerifyResultOfFile(String s1, String s2) {
        String res = "";
        for (int i=0; i<s1.length(); i++) {
            int a = getIntByChar(s1.charAt(i));
            int b = getIntByChar(s2.charAt(i));
            String c = getCharByInt(a ^ b);
            res += c;
        }
        return res;
    }

    // 字符转整数
    public int getIntByChar(char c) {
        int res = 0;
        if (c == 'f') {
            res = 15;
        }
        else if (c == 'e') {
            res = 14;
        }
        else if (c == 'd') {
            res = 13;
        }
        else if (c == 'c') {
            res = 12;
        }
        else if (c == 'b') {
            res = 11;
        }
        else if (c == 'a') {
            res = 10;
        }
        else {
            res = Integer.parseInt(String.valueOf(c));
        }
        return res;
    }
    // 整数转字符
    public String getCharByInt(int num) {
        String res = "";
        if (num>=0 && num <=9) {
            res = num+"";
        }
        else if (num == 10) {
            res = "a";
        }
        else if (num == 11) {
            res = "b";
        }
        else if (num == 12) {
            res = "c";
        }
        else if (num == 13) {
            res = "d";
        }
        else if (num == 14) {
            res = "e";
        }
        else if (num == 15) {
            res = "f";
        }
        return res;
    }

    // 大整数转换为字节数组
    public static byte[] toByteArray(BigInteger bi, int length) {
        byte[] array = bi.toByteArray();
        // 这种情况是转换的array超过25位
        if (array[0] == 0) {
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }
        // 假如转换的byte数组少于24位，则在前面补齐0
        if (array.length < length) {
            byte[] tmp = new byte[length];
            System.arraycopy(array, 0, tmp, length - array.length, array.length);
            array = tmp;
        }
        return array;
    }

    // 获取文件的 SHA1 哈希值
    private static String getFileSHA1(String file) {
        String str = "";
        try {
            str = getHash(file, "SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
    // 计算哈希值
    private static String getHash(String file, String hashType) throws Exception {
        InputStream fis = new FileInputStream(file);
        byte buffer[] = new byte[1024];
        MessageDigest md5 = MessageDigest.getInstance(hashType);
        for (int numRead = 0; (numRead = fis.read(buffer)) > 0; ) {
            md5.update(buffer, 0, numRead);
        }
        fis.close();
        return toHexString(md5.digest());
    }
    // 转换为十六进制字符串
    private static String toHexString(byte b[]) {
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) {
            sb.append(Integer.toHexString(aB & 0xFF));
        }

        return sb.toString();
    }

    // 将数组写入 Excel 文件
    public void writeArrayToExcel(double[][] data, String string) {
        int rowNum = data.length;
        int columnNum = data[0].length;
        try {
            FileWriter fw = new FileWriter(string);
            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < columnNum; j++)
                    fw.write(data[i][j]+ "\t"); // tab 间隔
                fw.write("\n"); // 换行
            }
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public void run(String hash_s1, String s2, String verifyResult) throws Exception {
        // 从配置文件加载属性
        FileInputStream propertiesFIS = new FileInputStream("./properties");
        Properties properties = new Properties();
        properties.load(propertiesFIS);
        propertiesFIS.close();

        // 获取配置属性
        String arbitratorAddress = properties.getProperty("arbitratorAddress");
        String cloudAddress = properties.getProperty("dataOwnerAddress");
        String clientAddress = properties.getProperty("dataBuyerAddress");
        BigInteger serviceFee = new BigInteger(properties.getProperty("serviceFee"));
        BigInteger penaltyFee = new BigInteger(properties.getProperty("penaltyFee"));
        BigInteger duringTime = new BigInteger(properties.getProperty("duringTime"));
        boolean isFirst = true;

        // 1. 创建与以太坊节点连接的 web3j 实例
        Web3j web3j = Web3j.build(new HttpService(properties.getProperty("httpService")));
        log.info("Connected to Ethereum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());

        // 2. 加载以太坊钱包
        Credentials arbitratorCredentials = WalletUtils.loadCredentials(properties.getProperty("arbitratorWalletPassword"), "./wallet/"+arbitratorAddress+".json");
        Credentials cloudCredentials = WalletUtils.loadCredentials(properties.getProperty("cloudWalletPassword"), "./wallet/"+cloudAddress+".json");
        Credentials clientCredentials = WalletUtils.loadCredentials(properties.getProperty("clientWalletPassword"), "./wallet/"+clientAddress+".json");
        log.info("Credentials loaded");

        // 3. 设置 Gas 提供者
        ContractGasProvider provider = new StaticGasProvider(BigInteger.valueOf(20000000000L), BigInteger.valueOf(12409988L));  // 20 Gwei, GasLimit 1000000

        // 部署智能合约
        NonRepudiation nonRepudiation = NonRepudiation.deploy(web3j, arbitratorCredentials, provider, cloudAddress, clientAddress, penaltyFee, serviceFee, duringTime).send();
        String contractAddress = nonRepudiation.getContractAddress();
        log.info("Smart contract: " + contractAddress);

        // 4. 调用智能合约
        log.info("Call smart contract");
        NonRepudiation client = NonRepudiation.load(contractAddress, web3j, clientCredentials, provider);
        NonRepudiation cloud = NonRepudiation.load(contractAddress, web3j, cloudCredentials, provider);
        NonRepudiation arbitrator = NonRepudiation.load(contractAddress, web3j, arbitratorCredentials, provider);

        // 如果不是第一次，重新启动合约
        if (!isFirst) {
            arbitrator.restart().send();
        }

        // 1. Client 请求服务
        client.requestService("service1").send().getGasUsed();
        log.info("Client request service");

        // 2. Cloud 执行 Round1
        byte[] verify_result = DatatypeConverter.parseHexBinary(verifyResult);
        byte[] hashS1 = DatatypeConverter.parseHexBinary(hash_s1);
        //创建一个名为 "cloudDoRound1" 的智能合约函数调用对象，并将verify_result和hashS1作为参数传送
        Function function = new Function("cloudDoRound1",
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(verify_result),
                        new org.web3j.abi.datatypes.generated.Bytes32(hashS1)),
                Collections.<TypeReference<?>>emptyList()
        );
        String encodedFunction = FunctionEncoder.encode(function);
        //获得发送者（Cloud）的账户在以太坊网络上的交易数量
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                cloudCredentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //创建一个原始的交易对象，包含、gas 价格、gas 限制、目标合约地址、函数调用数据等信息。
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, BigInteger.valueOf(20000000000L), BigInteger.valueOf(10000000L), contractAddress, penaltyFee, encodedFunction);
        //使用Cloud的私钥对原始交易进行签名，生成一个已签名的消息
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, cloudCredentials);
        //将已签名的消息转换为十六进制字符串
        String hexValue = Numeric.toHexString(signedMessage);
        //将签名后的交易消息发送到区块链网络中
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        //获取发送交易后的交易哈希
        String transactionHash = ethSendTransaction.getTransactionHash();
        EthGetTransactionReceipt transactionReceipt;

        // 等待区块被确认
        while (true) {
            transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
            if (transactionReceipt.getResult() != null) {
                break;
            }
            Thread.sleep(15000);
        }

        log.info("Cloud do Round1");

        // 3. Client 确认
        //创建一个名为 "clientConfirm" 的智能合约函数调用对象
        Function confirmFunction = new Function(
                "clientConfirm",
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList()
        );
        String encodedConfirmFunction = FunctionEncoder.encode(confirmFunction);
       //获得发送者（Client）的账户在以太坊网络上的交易数量
        EthGetTransactionCount ethGetConfirmTransactionCount = web3j.ethGetTransactionCount(
                clientCredentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger confirmNonce = ethGetConfirmTransactionCount.getTransactionCount();
        //创建一个原始的交易对象
        RawTransaction rawConfirmTransaction = RawTransaction.createTransaction(confirmNonce, BigInteger.valueOf(20000000000L), BigInteger.valueOf(10000000L), contractAddress, serviceFee, encodedConfirmFunction);
        //通过client私钥生成一个已签名的消息
        byte[] signedConfirmMessage = TransactionEncoder.signMessage(rawConfirmTransaction, clientCredentials);
        String hexConfirmValue = Numeric.toHexString(signedConfirmMessage);
        //将签名后的交易消息发送到区块链网络中
        ethSendTransaction = web3j.ethSendRawTransaction(hexConfirmValue).sendAsync().get();
        //获取交易哈希
        transactionHash = ethSendTransaction.getTransactionHash();


        // 等待区块被确认-通过等待交易收据的方式确保交易已被区块链确认
        while (true) {
            transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
            if (transactionReceipt.getResult() != null) {
                break;
            }
            Thread.sleep(15000);
        }
        log.info("Client confirm");

        // 4. Cloud 执行 Round2
        cloud.cloudDoRound2(s2, new BigInteger(randomBlock+"")).send().getGasUsed();
        log.info("Cloud do round2");

        // 5. Client 仲裁
        client.arbitrate().send().getGasUsed();
        log.info("Client arbitrate");

        // 6. Client 启动链下仲裁
        client.lauchoffChainArbitrate().send().getGasUsed();
        log.info("Client lauch off-chain arbitrate");

        // 7. Arbitrator 执行链下仲裁
        arbitrator.offChainArbitrate(new BigInteger("0")).send().getGasUsed();
        log.info("Arbitrator do off-chain arbitration");

        web3j.shutdown();
        System.exit(0);
    }

}

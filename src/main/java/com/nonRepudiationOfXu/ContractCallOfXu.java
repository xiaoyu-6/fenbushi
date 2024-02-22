package com.nonRepudiationOfXu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.NonRepudiationOfXu;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Properties;

public class ContractCallOfXu {

    private String file; // file to be outsourced
    private BigInteger[] data; // the file data

    private int messageBitLength; // the bit length of a data block; it determines the underlying finite field of the message.
    private long fileSize; // the size of the file in bytes

    private int blocks; // total number of data blocks, which is equal to

    public BigInteger p;
    public BigInteger g;
    public BigInteger q;
    private int randomBlock = 10;
    private BigInteger[] gasUsed = new BigInteger[5];

    BigInteger[] hashOfBlock;

    private String gasFile = "gasXu.xls";

    private static final Logger log = LoggerFactory.getLogger(ContractCallOfXu.class);

    public static void main(String[] args)
    {
        int blocksize;
        ContractCallOfXu file = new ContractCallOfXu();
        for (int i=1; i<=1; i++) {
            blocksize = 16;
            String p = "60899";
            String q = "30449";
            String g = "29573";
            file.init("C:\\Users\\sobieski\\Desktop\\Blockchain_Based_Non-repudiable_IoT_Data_Trading_Simpler_Faster_and_Cheaper.pdf", p, g, q, blocksize);
            BigInteger verifyResult = file.getHashOfFile();
            log.info("verify result of file = " + verifyResult);
            BigInteger hash_s1 = file.getHashOfS1();
            log.info("hash of S1 = " + hash_s1);
            BigInteger s2 = file.getS2();
            log.info("S2 = " + s2);
            file.arbitrate(s2, hash_s1);
            file.run(verifyResult, hash_s1, s2);
        }

//        file.arbitrate(s2, hash_s1);
    }

    public ContractCallOfXu() {}

    //初始化文件和相关参数
    public void init(String file, String p, String g, String q, int blocksize) {
        // 将输入的字符串参数转换为 BigInteger 类型
        this.p = new BigInteger(p);
        this.g = new BigInteger(g);
        this.q = new BigInteger(q);

        // 记录文件路径
        this.file = file;
        java.io.File f = new java.io.File(file);

        // 获取文件大小
        this.fileSize = f.length();
        // 每个数据块的位长度（每个数据块是 1024 位；它将始终是 8 的倍数）
        this.messageBitLength = blocksize;

        // 计算文件中数据块的总数
        this.blocks = (int) Math.ceil(this.fileSize * 8 / (double) messageBitLength);
        // 创建 BigInteger 数组来存储文件数据块
        this.data = new BigInteger[this.blocks];

        // 用于读取文件的字节数组
        byte[] block = new byte[this.messageBitLength / 8];

        try {
            // 创建文件输入流
            FileInputStream source = new FileInputStream(this.file);
            for (int i = 0; i < data.length; i++) {
                // 从文件中读取数据块到字节数组
                source.read(block);

                // 将字节数组转换为 BigInteger 类型，并存储到 data 数组中
                this.data[i] = new BigInteger(1, block);

                // 将字节数组填充为零，以便下一次读取
                Arrays.fill(block, (byte) 0);
            }
            // 关闭文件输入流
            source.close();
        } catch (Exception e) {
            System.out.println("Exception in InnerProductBasedVS when reading file into memory: " + e);
        }
    }

    //计算文件哈希值
    public BigInteger getHashOfFile() {
        BigInteger hash = null;
        BigInteger data1;

        BigInteger sumOfData = new BigInteger("0");

        for (int i=0; i<data.length; i++) {
            sumOfData = sumOfData.add(this.data[i]);
        }

        if (sumOfData.compareTo(this.q) == -1) {
            hash = this.g.modPow(sumOfData, this.p);
        }
        else {
            hash = this.g.modPow(sumOfData.mod(this.q), this.p);
        }
        return hash;
    }

    //获取 S2 的 BigInteger 表示
    public BigInteger getS2() {
        // Choose the first block of the file as S1
        return this.data[this.randomBlock];
    }

    //计算S1的哈希值
    public BigInteger getHashOfS1() {
        hashOfBlock = new BigInteger[data.length-1]; // 每一个块的哈希

        int j = 0;
        for (int i=0; i<data.length-1; i++) {
            if (j == this.randomBlock) {
                j++;
            }
            if (this.data[i+1].compareTo(this.q) == -1) {
                hashOfBlock[i] = this.g.modPow(this.data[j], this.p);
            }
            else {
                hashOfBlock[i] = this.g.modPow(this.data[j].mod(this.q), this.p);
            }
            j++;

        }

        BigInteger tempMultiValue = new BigInteger("1");
        for (int i=0; i<data.length-1; i++) {
            tempMultiValue = tempMultiValue.multiply(hashOfBlock[i]).mod(this.p);
        }

        return tempMultiValue;
    }

    //仲裁
    public void arbitrate(BigInteger s1, BigInteger hash_s2) {
        BigInteger hash_s1 = this.g.modPow(s1, this.p);
//        log.info("hash of s1 = " + hash_s1);
        BigInteger hash_s1_multiply_s2 = hash_s1.multiply(hash_s2).mod(this.p);
        //打印计算结果
        log.info("hash_s1 * hash_s2 = " + hash_s1_multiply_s2);
    }

    //把data写入到Excel 文件中
    public void writeArrayToExcel(BigInteger[] data, String string) {
        int rowNum = data.length;
        try {
            File file = new File(string);
            FileWriter fw;
            if(!file.exists()){
                fw = new FileWriter(string);//首次写入获取
                fw.write( "blocksize" + "\t");
                fw.write( "requestService" + "\t");
                fw.write( "cloudDoRoun1" + "\t");
                fw.write( "clientConfim" + "\t");
                fw.write( "cloudDoRoun2" + "\t");
                fw.write( "onchain-arbitrate" + "\t");
                fw.write("\n");
            }else{
                //如果文件已存在，那么就在文件末尾追加写入
                fw = new FileWriter(string, true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
            }

            fw.write(this.messageBitLength + "\t");
            for (int i = 0; i < rowNum; i++) {
                fw.write(data[i]+ "\t"); // tab 间
            }
            fw.write("\n"); // 换行
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    public void run(BigInteger verifyResult, BigInteger hash_s1, BigInteger s2) {
        try {
            FileInputStream propertiesFIS = new FileInputStream("./properties");
            Properties properties = new Properties();
            properties.load(propertiesFIS);
            propertiesFIS.close();
            String arbitratorAddress = properties.getProperty("arbitratorAddress");
            String cloudAddress = properties.getProperty("dataOwnerAddress");
            String clientAddress = properties.getProperty("dataBuyerAddress");
            BigInteger duringTime = new BigInteger(properties.getProperty("duringTime"));

            // 1. We start by creating a new web3j instance to connect to remote nodes on the network.
            Web3j web3j = Web3j.build(new HttpService(properties.getProperty("httpService")));
            log.info("Connected to Etherum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());

            // 2. We then need to load our Ethereum wallet file
            Credentials arbitratorCredentials = WalletUtils.loadCredentials(properties.getProperty("arbitratorWalletPassword"), "./wallet/"+arbitratorAddress+".json");
            Credentials cloudCredentials = WalletUtils.loadCredentials(properties.getProperty("cloudWalletPassword"), "./wallet/"+cloudAddress+".json");
            Credentials clientCredentials = WalletUtils.loadCredentials(properties.getProperty("clientWalletPassword"), "./wallet/"+clientAddress+".json");
            log.info("Credentials loaded");

            ContractGasProvider provider = new StaticGasProvider(BigInteger.valueOf(20000000000L), BigInteger.valueOf(12409988L)); // 20 Gwei, GasLimit 1000000

            // deploy small contract
            NonRepudiationOfXu nonRepudiation = NonRepudiationOfXu.deploy(web3j, clientCredentials, provider, cloudAddress, clientAddress, duringTime, this.g, this.p, this.q).send();
            String contractAddress = nonRepudiation.getContractAddress();
            log.info("Smart contract: " + contractAddress);

            // call smart contract
            log.info("Call smart contract");
            NonRepudiationOfXu client = NonRepudiationOfXu.load(contractAddress, web3j, clientCredentials, provider);
            NonRepudiationOfXu cloud = NonRepudiationOfXu.load(contractAddress, web3j, cloudCredentials, provider);


            int i = 0;
            // 1. Client request service
            gasUsed[i++] = client.requestService("service1").send().getGasUsed();
            log.info("Client request service");

            // 2. Cloud do round1
            gasUsed[i++] = cloud.cloudDoRound1(verifyResult, hash_s1).send().getGasUsed();
            log.info("Cloud do Round1");

            // 3. Client confirm
            gasUsed[i++] = client.clientConfirm().send().getGasUsed();
            log.info("Client confirm");

            // 4. Cloud do round2
            gasUsed[i++] = cloud.cloudDoRound2(s2, new BigInteger(randomBlock+"")).send().getGasUsed();
            log.info("Cloud do round2");

            // 5. Client arbitrate
            gasUsed[i++] = client.arbitrate().send().getGasUsed();
            log.info("Client arbitrate");

            writeArrayToExcel(gasUsed, gasFile);
            web3j.shutdown();
        }
        catch (Exception e) {
            System.out.println(e);
            run(verifyResult, hash_s1, s2);
        }

    }
}

package com.ruoyi.virtual.security;

import java.math.BigInteger;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

/**
 * 钱包签名验证器
 *
 * 验证 EIP-191 personal_sign 签名，用于钱包登录和绑定
 *
 * @author ruoyi
 */
@Component
public class WalletSignatureVerifier
{
    private static final Logger log = LoggerFactory.getLogger(WalletSignatureVerifier.class);

    /** Ethereum 签名消息前缀 */
    private static final String ETH_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    /**
     * 验证 EIP-191 personal_sign 签名
     *
     * @param message         原始消息
     * @param signature       十六进制签名（0x 前缀）
     * @param expectedAddress 期望的钱包地址（0x 前缀）
     * @return 签名是否有效
     */
    public boolean verifySignature(String message, String signature, String expectedAddress)
    {
        try
        {
            // 1. 构建 EIP-191 消息哈希
            byte[] messageBytes = message.getBytes();
            String prefix = ETH_MESSAGE_PREFIX + messageBytes.length;
            byte[] msgHash = Hash.sha3((prefix + message).getBytes());

            // 2. 解析签名 (r, s, v)
            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            if (signatureBytes.length != 65)
            {
                log.warn("签名长度无效: {}", signatureBytes.length);
                return false;
            }

            byte v = signatureBytes[64];
            if (v < 27)
            {
                v += 27;
            }

            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);

            ECDSASignature ecdsaSignature = new ECDSASignature(
                    new BigInteger(1, r),
                    new BigInteger(1, s));

            // 3. 恢复公钥并推导地址
            BigInteger publicKey = Sign.recoverFromSignature(v - 27, ecdsaSignature, msgHash);
            if (publicKey == null)
            {
                log.warn("无法从签名恢复公钥");
                return false;
            }

            String recoveredAddress = "0x" + Keys.getAddress(publicKey);

            // 4. 比较地址（不区分大小写）
            boolean valid = recoveredAddress.equalsIgnoreCase(expectedAddress);
            if (!valid)
            {
                log.warn("签名地址不匹配: expected={}, recovered={}", expectedAddress, recoveredAddress);
            }
            return valid;
        }
        catch (Exception e)
        {
            log.error("签名验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成登录签名消息
     *
     * @param nonce 随机数
     * @return 签名消息
     */
    public String buildLoginMessage(String nonce)
    {
        return "Welcome to Pet AI!\n\nPlease sign this message to verify your wallet ownership.\n\nNonce: " + nonce;
    }
}

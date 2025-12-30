package com.nexavault.service.impl;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nexavault.service.SolanaService;

@Service
public class SolanaServiceImpl implements SolanaService {

    @Value("${solana.rpc.url}")
    private String solanaRpcUrl;

    @Value("${wallet.secret.key}")
    private String walletSecretKey; // Path to JSON key file

    @Override
    public String mintNFT(String ipfsHash) throws Exception {
        RpcClient client = new RpcClient(solanaRpcUrl);

        // Read JSON keypair file
        ObjectMapper mapper = new ObjectMapper();
        int[] keyArray = mapper.readValue(new File(walletSecretKey), int[].class);

        // Convert to byte[]
        byte[] secretKey = new byte[keyArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            secretKey[i] = (byte) keyArray[i];
        }

        Account wallet = new Account(secretKey);
        PublicKey mint = wallet.getPublicKey();

        return client.getApi().requestAirdrop(mint, 1_000_000);
    }
}

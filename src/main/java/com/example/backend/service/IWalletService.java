package com.example.backend.service;

import com.example.backend.dto.WalletDto;
import com.example.backend.dto.WalletInfoDto;
import com.example.backend.model.WalletRole;
import com.example.backend.model.entity.Wallet;

import java.math.BigDecimal;
import java.util.Set;

public interface IWalletService {
    Wallet saveWallet(Long ownerId, WalletDto walletDto);

    Wallet updateWallet(Long walletId, Long userId, WalletDto walletDto);

    WalletDto findWalletById(Long id);

    void deleteWalletById(Long id);

    WalletInfoDto getWalletWithPermission(Long walletId, Long userId);

    boolean isOwner(Long walletId, Long userId);

    void updateWalletAmount(Long id, BigDecimal newAmount);


    Set<WalletInfoDto> findAllWalletByUserId(Long id);

    void shareWallet(Long walletId, String email, String walletRoleName);
    void removeWalletShare(Long walletId, String email);
    void updateWalletRole(Long walletId, Long userId, WalletRole walletRoleName);

    void transferMoney(Long fromWalletId, Long toWalletId, BigDecimal amount, Long userId);
    BigDecimal getTotalAmount(Long userId);
}

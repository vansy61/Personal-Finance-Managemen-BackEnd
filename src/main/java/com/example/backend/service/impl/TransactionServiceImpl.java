package com.example.backend.service.impl;

import com.example.backend.dto.TransactionDto;
import com.example.backend.dto.TransactionInfoDto;
import com.example.backend.dto.TransactionSimpleDto;
import com.example.backend.model.entity.Category;
import com.example.backend.model.entity.Transaction;
import com.example.backend.model.entity.User;
import com.example.backend.model.entity.Wallet;
import com.example.backend.repository.ICategoryRepo;
import com.example.backend.repository.ITransactionRepo;
import com.example.backend.repository.IUserRepo;
import com.example.backend.repository.IWalletRepo;
import com.example.backend.service.ITransactionService;
import com.example.backend.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements ITransactionService {

    private final ITransactionRepo transactionRepository;
    private final ICategoryRepo categoryRepository;
    private final IWalletRepo walletRepository;
    private final IUserRepo userRepository;
    private final EmailUtil emailUtil;


    @Override
    public Page<TransactionInfoDto> findAllTransactionByUserId(Long userId, Long categoryId, Integer categoryType, Long walletId, String startDate, String endDate, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return transactionRepository.findAllTransactionByUserId(userId, categoryId, categoryType, walletId, startDate, endDate, pageable);
    }


    @Override
    public TransactionDto findTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
        return convertToDTO(transaction);
    }

    @Override
    public TransactionDto save(Long userId, TransactionDto transactionDto) {
        Category category = categoryRepository.findById(transactionDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Wallet wallet = walletRepository.findById(transactionDto.getWalletId()).orElseThrow(() -> new RuntimeException("Wallet not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Transaction transaction = Transaction.builder()
                .amount(transactionDto.getAmount())
                .note(transactionDto.getNote())
                .datetime(transactionDto.getDatetime())
                .category(category)
                .wallet(wallet)
                .user(user)
                .build();
        if (transaction.getCategory().getCategoryType() == 1) {
            wallet.setAmount(wallet.getAmount().add(transactionDto.getAmount()));
        } else if (transaction.getCategory().getCategoryType() == 0) {
            if(transaction.getWallet().getAmount().compareTo(transaction.getAmount()) < 0) {
                throw new RuntimeException("Ví không đủ số dư để thực hiện chi tiền!");
            }
            wallet.setAmount(wallet.getAmount().subtract(transactionDto.getAmount()));
        }
        walletRepository.save(wallet);
        return convertToDTO(transactionRepository.save(transaction));
    }

    public TransactionDto convertToDTO(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setWalletId(transaction.getWallet().getId());
        dto.setAmount(transaction.getAmount());
        dto.setNote(transaction.getNote());
        dto.setDatetime(transaction.getDatetime());
        dto.setCategoryType(transaction.getCategory().getCategoryType());
        return dto;
    }

    @Override
    public void deleteById(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
        Wallet wallet = transaction.getWallet();

        if (transaction.getCategory().getCategoryType() == 1) {
            if(transaction.getWallet().getAmount().compareTo(transaction.getAmount()) < 0) {
                    throw new RuntimeException("Ví không đủ số dư (xóa khoản thu sẽ trừ 1 khoản tương ứng trong ví)");
            }
            wallet.setAmount(wallet.getAmount().subtract(transaction.getAmount()));
        }else {
            wallet.setAmount(wallet.getAmount().add(transaction.getAmount()));
        }
        walletRepository.save(wallet);
        transactionRepository.deleteById(id);
    }

    @Override
    public void updateTransaction(Long id, TransactionDto transactionDto) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
        Wallet oldWallet = walletRepository.findById(transaction.getWallet().getId()).orElseThrow(() -> new RuntimeException("Wallet not found"));

        Category category = categoryRepository.findById(transactionDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
//        Long a = transaction.getWallet().getId();
//        Long b = transactionDto.getWalletId();
        Wallet wallet = walletRepository.findById(transactionDto.getWalletId()).orElseThrow(() -> new RuntimeException("Wallet not found"));



//        if (wallet.getId() == oldWallet.getId()) {
//            // old la thu - new la chi
//            if (transaction.getCategory().getCategoryType() == 1 && category.getCategoryType() == 0) {
//                BigDecimal newAmount = oldWallet.getAmount().subtract(transaction.getAmount()).subtract(transactionDto.getAmount());
//                wallet.setAmount(newAmount);
//
//            }
//            // old la thu - new la thu
//            else if (transaction.getCategory().getCategoryType() == 0 && category.getCategoryType() == 1) {
//                BigDecimal newAmount = oldWallet.getAmount().subtract(transaction.getAmount()).add(transactionDto.getAmount());
//                wallet.setAmount(newAmount);
//            }
//            // old la chi - new la chi
//            else if (transaction.getCategory().getCategoryType() == 0 && category.getCategoryType() == 0) {
//                BigDecimal newAmount = oldWallet.getAmount().add(transaction.getAmount()).subtract(transactionDto.getAmount());
//                wallet.setAmount(newAmount);
//            }
//            // old la chi - new la thu
//            else if (transaction.getCategory().getCategoryType() == 0 && category.getCategoryType() == 1) {
//                BigDecimal newAmount = oldWallet.getAmount().add(transaction.getAmount()).add(transactionDto.getAmount());
//                wallet.setAmount(newAmount);
//            }
//            walletRepository.save(wallet);
//
//        } else {
//            // old la thu - new la chi
//            if (transaction.getCategory().getCategoryType() == 1 && category.getCategoryType() == 0) {
//                BigDecimal oldAmount = oldWallet.getAmount().subtract(transaction.getAmount());
//                BigDecimal newAmount = wallet.getAmount().subtract(transactionDto.getAmount());
//                oldWallet.setAmount(oldAmount);
//                wallet.setAmount(newAmount);
//
//            }
//            // old la thu - new la thu
//            else if (transaction.getCategory().getCategoryType() == 0 && category.getCategoryType() == 1) {
//                BigDecimal oldAmount = oldWallet.getAmount().subtract(transaction.getAmount());
//                BigDecimal newAmount = wallet.getAmount().add(transactionDto.getAmount());
//                oldWallet.setAmount(oldAmount);
//                wallet.setAmount(newAmount);
//
//            }
//            // old la chi - new la chi
//            else if (transaction.getCategory().getCategoryType() == 0 && category.getCategoryType() == 0) {
//                BigDecimal oldAmount = oldWallet.getAmount().add(transaction.getAmount());
//                BigDecimal newAmount = wallet.getAmount().subtract(transactionDto.getAmount());
//                oldWallet.setAmount(oldAmount);
//                wallet.setAmount(newAmount);
//
//            }
//            // old la chi - new la thu
//            else if (transaction.getCategory().getCategoryType() == 0 && category.getCategoryType() == 1) {
//                BigDecimal oldAmount = oldWallet.getAmount().add(transaction.getAmount());
//                BigDecimal newAmount = wallet.getAmount().add(transactionDto.getAmount());
//                oldWallet.setAmount(oldAmount);
//                wallet.setAmount(newAmount);
//
//            }
//            walletRepository.save(oldWallet);
//            walletRepository.save(wallet);
//
//        }


        if(transaction.getCategory().getCategoryType() == 1) {
            oldWallet.setAmount(oldWallet.getAmount().subtract(transaction.getAmount()));
        }else {
            oldWallet.setAmount(oldWallet.getAmount().add(transaction.getAmount()));
        }

        if(category.getCategoryType() == 1) {
            wallet.setAmount(wallet.getAmount().add(transactionDto.getAmount()));
        }else {
            wallet.setAmount(wallet.getAmount().subtract(transactionDto.getAmount()));
        }

        if(wallet.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Ví không đủ số dư để chỉnh sửa giao dịch này");
        }
        if(!Objects.equals(oldWallet.getId(), wallet.getId()) && oldWallet.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Ví không đủ số dư để chỉnh sửa giao dịch này");
        }


        transaction.setNote(transactionDto.getNote());
        transaction.setDatetime(transactionDto.getDatetime());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setCategory(category);
        transaction.setWallet(wallet);
        walletRepository.save(oldWallet);
        walletRepository.save(wallet);
        transactionRepository.save(transaction);

    }

    @Override
    public List<TransactionSimpleDto> searchTransactionWithUserId(Long userId, Long categoryId, Long walletId, String startDate, String endDate) {
        System.out.println("=========================");
        System.out.println(startDate);
        System.out.println(endDate);
        return transactionRepository.searchAllTransaction(userId, categoryId, walletId, startDate, endDate);
    }


    @Override
    public BigDecimal statisticalTotalAmountTodayByCategoryType(Long userId, Integer categoryType) {

        Optional<BigDecimal> amount = transactionRepository.getTotalAmountTodayByCategoryType(userId, categoryType, LocalDate.now());

        if (amount.isEmpty()) {

            return BigDecimal.ZERO;
        }
        return amount.get();
    }

    @Override
    public BigDecimal statisticalTotalAmountByTypeAndTime(Integer type, LocalDate fromDate, LocalDate toDate, Long userId) {
        Optional<BigDecimal> amount = transactionRepository.getTotalAmountByTypeAndTime(type, fromDate, toDate, userId);
        if (amount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return amount.get();
    }

    @Override
    public BigDecimal statisticalAmountTodayByWalletId(Integer categoryType, Long walletId) {
        Optional<BigDecimal> amount = transactionRepository.statisticalAmountTodayByWalletId(categoryType, walletId, LocalDate.now());
        if (amount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return amount.get();
    }

    @Override
    public BigDecimal statisticalAmountByWalletIdAndTime(Integer categoryType, Long walletId, LocalDate fromDate, LocalDate toDate) {
        Optional<BigDecimal> amount = transactionRepository.statisticalAmountByWalletIdAndTime(categoryType, walletId, fromDate, toDate);
        if (amount.isEmpty()) return BigDecimal.ZERO;
        return amount.get();

    }

}

package com.example.backend.repository;

import com.example.backend.dto.TransactionInfoDto;
import com.example.backend.dto.TransactionSimpleDto;
import com.example.backend.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITransactionRepo extends JpaRepository<Transaction, Long> {
//    @Query(value = "select tr_w.transaction_id from wallet w join  transaction tr on tr_w.wallets_id = :walletId",nativeQuery = true)
//    List<Transaction> getTransactionsByWalletId(Long walletId);

    @Query(value = "delete from transaction where user_id = :userId", nativeQuery = true)
    void deleteTransactionByUserId(Long userId);
//    @Query(value = "SELECT t.id, t.note, t.amount, t.datetime,c.icon, c.category_type, c.category_name, w.wallet_name  FROM transaction t join category c on t.category_id = c.id join wallet w on t.wallet_id = w.id   WHERE t.user_id = :userId" ,nativeQuery = true)
//    List<TransactionInfoDto> findAllTransactionByUserId(Long userId);
    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.user.id = :userId) AND " +
            "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
            "(:categoryType IS NULL OR :categoryType = 2 OR t.category.categoryType = :categoryType) " +
            "ORDER BY t.datetime DESC")
    Page<TransactionInfoDto> findAllTransactionByUserId(@Param("userId") Long userId,
                                                        @Param("categoryId") Long categoryId,
                                                        @Param("categoryType") Integer categoryType,
                                                        Pageable pageable);

    List<TransactionSimpleDto> findAllByUserIdAndCategoryId(Long categoryId, Long userId);
}

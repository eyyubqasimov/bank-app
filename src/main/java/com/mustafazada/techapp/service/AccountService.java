package com.mustafazada.techapp.service;
import com.mustafazada.techapp.dto.request.AccountToAccountRequestDTO;
import com.mustafazada.techapp.dto.response.AccountResponseDTOList;
import com.mustafazada.techapp.dto.response.CommonResponseDTO;
import com.mustafazada.techapp.dto.response.Status;
import com.mustafazada.techapp.dto.response.StatusCode;
import com.mustafazada.techapp.entity.Account;
import com.mustafazada.techapp.entity.TechUser;
import com.mustafazada.techapp.exception.InvalidDTO;
import com.mustafazada.techapp.repository.AccountRepository;
import com.mustafazada.techapp.repository.UserRepository;
import com.mustafazada.techapp.util.CurrentUser;
import com.mustafazada.techapp.util.DTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private DTOUtil dtoUtil;
    @Autowired
    private AccountRepository accountRepository;
    public CommonResponseDTO<?> getAccount(){
      Optional<TechUser> user = userRepository.findByPin(currentUser.getCurrentUser().getUsername());

        return CommonResponseDTO.builder()
                .data(AccountResponseDTOList.entityToDTO(user.get().getAccountList()))
                .status(Status.builder()
                        .statusCode(StatusCode.SUCCESS)
                        .message("Accounts Successfully fetched!")
                .build()).build();

    }
    public CommonResponseDTO<?> account2account(AccountToAccountRequestDTO accountToAccountRequestDTO) {
        dtoUtil.isValid(accountToAccountRequestDTO);

        if (accountToAccountRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidDTO.builder()
                    .responseDTO(CommonResponseDTO.builder().status(Status.builder()
                            .statusCode(StatusCode.INVALID_DTO)
                            .message("Amount is not correct")
                            .build()).build()).build();
        }else if (accountToAccountRequestDTO.getDebitAccount().equals(accountToAccountRequestDTO.getCreditAccount())) {
            throw InvalidDTO.builder()
                    .responseDTO(CommonResponseDTO.builder()
                            .status(Status.builder()
                                    .statusCode(StatusCode.INVALID_DTO)
                                    .build()).build()).build();
        }
        Optional<Account> byDebitAccount = accountRepository.findByAccountNo(accountToAccountRequestDTO.getDebitAccount());

        Account debitAccount;
        Account creditAccount;
        if (byDebitAccount.isPresent()) {
            debitAccount = byDebitAccount.get();
            if (!debitAccount.getIsActive()) {
                throw InvalidDTO.builder()
                        .responseDTO(CommonResponseDTO.builder()
                                .status(Status.builder()
                                        .statusCode(StatusCode.INVALID_DTO)
                                        .message("Debit Account is not active")
                                        .build()).build()).build();
            }
            if (debitAccount.getBalance().compareTo(accountToAccountRequestDTO.getAmount()) <= 0) {
                throw InvalidDTO.builder()
                        .responseDTO(CommonResponseDTO.builder()
                                .status(Status.builder()
                                        .statusCode(StatusCode.INVALID_DTO)
                                        .message("Balance is not enough")
                                        .build()).build()).build();
            }
        }
        return null;
    }
}

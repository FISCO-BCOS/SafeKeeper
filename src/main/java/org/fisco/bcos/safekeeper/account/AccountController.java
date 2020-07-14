/**
 * Copyright 2014-2020 the original author or authors.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.safekeeper.account;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.safekeeper.account.entity.*;
import org.fisco.bcos.safekeeper.base.code.ConstantCode;
import org.fisco.bcos.safekeeper.base.controller.BaseController;
import org.fisco.bcos.safekeeper.base.entity.BasePageResponse;
import org.fisco.bcos.safekeeper.base.entity.BaseResponse;
import org.fisco.bcos.safekeeper.base.enums.SqlSortType;
import org.fisco.bcos.safekeeper.base.exception.SafeKeeperException;
import org.fisco.bcos.safekeeper.base.properties.ConstantProperties;
import org.fisco.bcos.safekeeper.base.tools.JacksonUtils;
import org.fisco.bcos.safekeeper.base.tools.SafeKeeperTools;
import org.fisco.bcos.safekeeper.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping(value = "accounts")
public class AccountController extends BaseController {

    @Autowired private AccountService accountService;
    @Autowired private TokenService tokenService;

    /** add account info. */
    @PostMapping(value = "/v1")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse addAccountInfo(@RequestBody @Valid AccountInfo info, BindingResult result)
            throws SafeKeeperException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
                "start addAccountInfo. startTime:{} accountInfo:{}",
                startTime.toEpochMilli(),
                JacksonUtils.objToString(info));

        // current
        String currentAccount = getCurrentAccount(request);

        // add account row
        accountService.addAccountRow(info, currentAccount);

        // query row
        TbAccountInfo tbAccount = accountService.queryByAccount(info.getAccount());
        tbAccount.setAccountPwd(null);
        baseResponse.setData(tbAccount);

        log.info(
                "end addAccountInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /** query account list. */
    @GetMapping(value = "/v1")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BasePageResponse queryAccountList(
            @RequestParam(value = "pageNumber") Integer pageNumber,
            @RequestParam(value = "pageSize") Integer pageSize)
            throws SafeKeeperException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();

        String account = getCurrentAccount(request);
        log.info(
                "start queryAccountList.  startTime:{} pageNumber:{} pageSize:{} account:{} ",
                startTime.toEpochMilli(),
                pageNumber,
                pageSize,
                account);

        int count = accountService.countOfAccount(account);
        if (count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(0);
            AccountListParam param =
                    new AccountListParam(start, pageSize, account, SqlSortType.DESC.getValue());
            List<TbAccountInfo> listOfAccount = accountService.listOfAccount(param);
            listOfAccount.stream().forEach(accountData -> accountData.setAccountPwd(null));
            pagesponse.setData(listOfAccount);
            pagesponse.setTotalCount(count);
        }

        log.info(
                "end queryAccountList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(pagesponse));
        return pagesponse;
    }

    /** get public key. */
    @GetMapping(value = "/v1/publicKey")
    @PreAuthorize(ConstantProperties.HAS_ROLE_VISITOR)
    public BaseResponse getPublicKey() throws SafeKeeperException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getPublicKey. startTime:{}", startTime.toEpochMilli());

        TbAccountInfo tbCurAccount = accountService.queryByAccount(getCurrentAccount(request));
        TbAccountInfo tbCreatorAccount = accountService.queryByAccount(tbCurAccount.getCreator());
        PublicKeyInfo publicKeyInfo =
                new PublicKeyInfo(tbCreatorAccount.getAccount(), tbCreatorAccount.getPublicKey());
        baseResponse.setData(publicKeyInfo);

        log.info(
                "end getPublicKey. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /** delete account by id. */
    @DeleteMapping(value = "/v1/{account}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public BaseResponse deleteAccount(@PathVariable("account") String account)
            throws SafeKeeperException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start delete. startTime:{} account:{}", startTime.toEpochMilli(), account);

        String currentAccount = getCurrentAccount(request);
        TbAccountInfo tbCurAccount = accountService.queryByAccount(account);
        if (tbCurAccount == null) {
            throw new SafeKeeperException(ConstantCode.ACCOUNT_NOT_EXISTS);
        }
        if (currentAccount == account || !currentAccount.equals(tbCurAccount.getCreator())) {
            log.info("lack of access to delete account");
            throw new SafeKeeperException(ConstantCode.ACCOUNT_ACCESS_DENIAL);
        }

        accountService.deleteAccountRow(account);

        log.info(
                "end deleteAccount. useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /** update password. */
    @PatchMapping(value = "/v1/password")
    public BaseResponse updatePassword(
            @RequestBody @Valid PasswordInfo info, HttpServletRequest request, BindingResult result)
            throws SafeKeeperException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
                "start updatePassword startTime:{} passwordInfo:{}",
                startTime.toEpochMilli(),
                JacksonUtils.objToString(info));

        String targetAccount = getCurrentAccount(request);

        // update account row
        accountService.updatePassword(
                targetAccount, info.getOldAccountPwd(), info.getNewAccountPwd());

        log.info(
                "end updatePassword useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JacksonUtils.objToString(baseResponse));
        return baseResponse;
    }

    /** get current account. */
    private String getCurrentAccount(HttpServletRequest request) {
        String token = SafeKeeperTools.getToken(request);
        log.debug("getCurrentAccount account:{}", token);
        return tokenService.getAccountFromToken(token);
    }
}

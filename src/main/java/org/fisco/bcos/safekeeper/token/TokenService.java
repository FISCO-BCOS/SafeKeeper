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
package org.fisco.bcos.safekeeper.token;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.Assert;
import org.fisco.bcos.safekeeper.base.code.ConstantCode;
import org.fisco.bcos.safekeeper.base.enums.TokenType;
import org.fisco.bcos.safekeeper.base.exception.SafeKeeperException;
import org.fisco.bcos.safekeeper.base.properties.ConstantProperties;
import org.fisco.bcos.safekeeper.base.tools.SafeKeeperTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** token service. */
@Log4j2
@Service
public class TokenService {
    @Autowired private ConstantProperties properties;
    @Autowired private TokenMapper tokenMapper;

    /** create token. */
    public String createToken(String account, int type) {
        if (StringUtils.isBlank(account)) {
            log.error("fail createToken. param is null");
            return null;
        }
        String token = SafeKeeperTools.shaEncode(UUID.randomUUID() + account);
        // save token
        TbToken tbToken = new TbToken();
        tbToken.setToken(token);
        tbToken.setAccount(account);
        if (type == TokenType.USER.getValue()) {
            tbToken.setExpireTime(
                    LocalDateTime.now(ZoneId.of("UTC"))
                            .plusSeconds(properties.getAuthTokenMaxAge()));
        } else {
            log.error("fail createToken. type:{} not support", type);
            return null;
        }
        tokenMapper.add(tbToken);
        return token;
    }

    /** get account from token. */
    public String getAccountFromToken(String token) {
        Assert.requireNonEmpty(token, "token is empty");

        // query by token
        TbToken tbToken = tokenMapper.queryToken(token);
        if (Objects.isNull(tbToken)) {
            log.warn("fail getAccountFromToken. tbToken is null");
            throw new SafeKeeperException(ConstantCode.INVALID_TOKEN);
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        if (now.isAfter(tbToken.getExpireTime())) {
            log.warn("fail getAccountFromToken. token has expire at:{}", tbToken.getExpireTime());
            // delete token
            this.deleteToken(token, null);
            throw new SafeKeeperException(ConstantCode.EXPIRED_TOKEN);
        }
        return tbToken.getAccount();
    }

    /** update token expire time. */
    public void updateExpireTime(String token) {
        Assert.requireNonEmpty(token, "token is empty");
        tokenMapper.update(
                token,
                LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(properties.getAuthTokenMaxAge()));
    }

    /** delete token. */
    public void deleteToken(String token, String account) {
        tokenMapper.delete(token, account);
    }

    /** get token from account. */
    public String getTokenFromAccount(String account) {
        Assert.requireNonEmpty(account, "account is empty");

        // query by account
        TbToken tbToken = tokenMapper.queryAccount(account);
        if (Objects.isNull(tbToken)) {
            log.warn("fail getTokenFromAccount. no related account");
            return null;
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        if (now.isAfter(tbToken.getExpireTime())) {
            log.warn("fail getTokenFromAccount. token has expire at:{}", tbToken.getExpireTime());
            // delete token
            this.deleteToken(null, tbToken.getAccount());
            return null;
        }

        updateExpireTime(tbToken.getToken());
        return tbToken.getToken();
    }
}

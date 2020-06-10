/**
 * Copyright 2014-2020  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.key.mgr.role;

import org.fisco.bcos.key.mgr.base.code.ConstantCode;
import org.fisco.bcos.key.mgr.base.exception.KeyMgrException;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for role data.
 */
@Log4j2
@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * check roleId.
     */
    public void roleIdExist(Integer roleId) throws KeyMgrException {
        log.debug("start roleIdExist. roleId:{} ", roleId);
        if (roleId == null) {
            log.info("fail roleIdExist. roleId is null ");
            throw new KeyMgrException(ConstantCode.ROLE_ID_EMPTY);
        }
        TbRole roleInfo = roleMapper.queryRoleById(roleId);
        if (roleInfo == null) {
            log.info("fail roleIdExist. did not found role row by id");
            throw new KeyMgrException(ConstantCode.INVALID_ROLE_ID);
        }
        log.debug("end roleIdExist. ");
    }
}

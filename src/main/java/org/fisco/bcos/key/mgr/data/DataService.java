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
package org.fisco.bcos.key.mgr.data;

import org.fisco.bcos.key.mgr.base.code.ConstantCode;
import org.fisco.bcos.key.mgr.base.exception.KeyMgrException;
import org.fisco.bcos.key.mgr.base.tools.JacksonUtils;
import org.fisco.bcos.key.mgr.data.entity.DataListParam;
import org.fisco.bcos.key.mgr.data.entity.TbDataInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * services for data.
 */
@Log4j2
@Service
public class DataService {

    @Autowired
    private DataMapper dataMapper;

    /**
     * add data row.
     */
    public void addDataRow(TbDataInfo dataInfo) throws KeyMgrException {
        log.debug("start addDataRow. data info:{}", JacksonUtils.objToString(dataInfo));

        // check data
        dataNotExist(dataInfo.getDataID(), dataInfo.getDataSubID());

        // add data row
        Integer affectRow = dataMapper.addDataRow(dataInfo);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end addDataRow. affectRow:{}", affectRow);
    }

    /**
     * query the data.
     */
    public TbDataInfo queryData(String dataID, String dataSubID) {
        log.debug("start queryData. dataID:{}, dataSubID:{} ", dataID, dataSubID);
        TbDataInfo dataRow = dataMapper.queryData(dataID, dataSubID);
        log.debug("end queryData. accountRow:{} ", JacksonUtils.objToString(dataRow));
        return dataRow;
    }

    /**
     * query count of data.
     */
    public int countOfData(DataListParam param) {
        log.debug("start countOfData. account:{} ", JacksonUtils.objToString(param));
        Integer keyCount = dataMapper.countOfData(param);
        int count = keyCount == null ? 0 : keyCount.intValue();
        log.debug("end countOfData. count:{} ", count);
        return count;
    }

    /**
     * query key list by account.
     */
    public List<TbDataInfo> listOfData(DataListParam param) {
        log.debug("start listOfData. param:{} ", JacksonUtils.objToString(param));
        List<TbDataInfo> list = dataMapper.listOfData(param);
        log.debug("end listOfData. list:{} ", JacksonUtils.objToString(list));
        return list;
    }

    /**
     * delete data info.
     */
    public void deleteDataRow(String dataID, String dataSubID) throws KeyMgrException {
        log.debug("start deleteDataRow. dataID:{}, dataSubID:{} ", dataID, dataSubID);

        // check data
        dataExist(dataID, dataSubID);

        // delete data row
        Integer affectRow = dataMapper.deleteDataRow(dataID, dataSubID);

        // check result
        checkDbAffectRow(affectRow);

        log.debug("end deleteDataRow. affectRow:{} ", affectRow);
    }

    /**
     * query existence of data.
     */
    public int existOfData(String dataID, String dataSubID) {
        log.debug("start existOfData. dataID:{} dataSubID:{} ", dataID, dataSubID);
        Integer dataCount = dataMapper.existOfData(dataID, dataSubID);
        int count = dataCount == null ? 0 : dataCount.intValue();
        log.debug("end existOfData. count:{} ", count);
        return count;
    }

    /**
     * boolean the key data is exist.
     */
    public void dataExist(String dataID, String dataSubID) throws KeyMgrException {
        if (StringUtils.isBlank(dataID)) {
            throw new KeyMgrException(ConstantCode.ACCOUNT_NAME_EMPTY);
        }
        if (StringUtils.isBlank(dataSubID)) {
            throw new KeyMgrException(ConstantCode.KEY_ALIASES_EMPTY);
        }
        int count = existOfData(dataID, dataSubID);
        if (count == 0) {
            throw new KeyMgrException(ConstantCode.KEY_NOT_EXISTS);
        }
    }

    /**
     * boolean the key of data is not exist.
     */
    public void dataNotExist(String dataID, String dataSubID) throws KeyMgrException {
        if (StringUtils.isBlank(dataID)) {
            throw new KeyMgrException(ConstantCode.ACCOUNT_NAME_EMPTY);
        }
        if (StringUtils.isBlank(dataSubID)) {
            throw new KeyMgrException(ConstantCode.KEY_ALIASES_EMPTY);
        }
        int count = existOfData(dataID, dataSubID);
        if (count > 0) {
            throw new KeyMgrException(ConstantCode.KEY_EXISTS);
        }
    }

    /**
     * check db affect row.
     */
    private void checkDbAffectRow(Integer affectRow) throws KeyMgrException {
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_data_info");
            throw new KeyMgrException(ConstantCode.DB_EXCEPTION);
        }
    }
}
